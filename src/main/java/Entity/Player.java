package Entity;

import Effects.DamageResult;
import GameState.Level1State;
import TileMap.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Player extends MapObject{

    private Level1State levelState;

    // player stuff
    private int health;
    private int maxHealth;
    private int defence;
    private int strength;
    private double CC;
    private double critDMG;
    private double regen;
    private double finalDMG;
    private double regenAmount;
    private double lastRegenTime;
    private double lastIntelRegenTime;
    private double intelRegenAmount;
    private double intelRegen;
    private double abilityDMG;
    private int intelligence;
    private int maxIntelligence;
    private boolean dead;
    private boolean flinching;
    private long flinchTimer;
    private boolean flying;

    // fireball
    private boolean firing;
    private int fireCost;
    private int fireBallDamage;
    private ArrayList<FireBall> fireBalls;

    // scratch
    private boolean scratching;
    private int scratchDamage;
    private int scratchRange;
    private boolean scratchDamageDealt = false;

    // gliding
    private boolean gliding;

    // animations
    private ArrayList<BufferedImage[]> sprites;
    private final int[] numFrames = { 2, 8, 1, 2, 4, 2, 5};

    // animation actions
    private static final int IDLE = 0;
    private static final int WALKING = 1;
    private static final int JUMPING = 2;
    private static final int FALLING = 3;
    private static final int GLIDING = 4;
    private static final int FIREBALL = 5;
    private static final int SCRATCHING = 6;

    public Player(TileMap tm, Level1State levelState) {

        super(tm);
        this.levelState = levelState;

        width = 30;
        height = 30;
        cwidth = 20;
        cheight = 20;

        moveSpeed = 0.3;
        maxSpeed = 1.6;
        stopSpeed = 0.4;
        fallSpeed = 0.15;
        maxFallSpeed = 4.0;
        jumpStart = -4.8;
        stopJumpSpeed = 0.3;

        facingRight = true;



        lastRegenTime = System.nanoTime();
        health = maxHealth = 100;
        defence = 10;

        lastIntelRegenTime = System.nanoTime();
        intelligence = maxIntelligence = 100;

        fireCost = 10;
        fireBallDamage = 20;
        abilityDMG = 30.0;
        fireBalls = new ArrayList<FireBall>();

        scratchDamage = 10;
        scratchRange = 40;
        strength = 20;
        CC = 15.0;
        critDMG = 50.0;
        regen = 2.0;
        intelRegen = 1.0;

        // load sprites
        try {

            BufferedImage spritesheet = ImageIO.read(getClass().getResourceAsStream("/Sprites/Player/playersprites.gif"));

            sprites = new ArrayList<BufferedImage[]>();
            for(int i = 0; i < 7; i++) {
                BufferedImage[] bi = new BufferedImage[numFrames[i]];
                for(int j = 0; j < numFrames[i]; j++) {
                    if(i != SCRATCHING) {
                        bi[j] = spritesheet.getSubimage(j * width, i * height, width, height);
                    } else {
                        bi[j] = spritesheet.getSubimage(j * width * 2, i * height, width * 2, height);
                    }
                }
                sprites.add(bi);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        animation = new Animation();
        currentAction = IDLE;
        animation.setFrames(sprites.get(IDLE));
        animation.setDelay(400);
    }

    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public int getMaxIntelligence() { return maxIntelligence; }

    public void setFiring(boolean b) {
        firing = true;
    }

    public void setScratching(boolean b) {
        if (b && !scratching) {
            scratching = true;
            scratchDamageDealt = false;
        }
    }

    public void setGliding(boolean b) {
        gliding = b;
    }

    public void checkAttack(ArrayList<Enemy> enemies) {
        for (int i = 0; i < enemies.size(); i++) {
            Enemy e = enemies.get(i);

            if (scratching && !scratchDamageDealt) {
                boolean hitEnemy = false;
                if (facingRight) {
                    if (e.getx() > x && e.getx() < x + scratchRange && e.gety() > y - height / 2 && e.gety() < y + height / 2) {
                        hitEnemy = true;
                    }
                } else {
                    if (e.getx() < x && e.getx() > x - scratchRange && e.gety() > y - height / 2 && e.gety() < y + height / 2) {
                        hitEnemy = true;
                    }
                }
                if (hitEnemy) {
                    DamageResult result = calculateDamage(scratchDamage, strength, CC, critDMG, null); // Pass enemy defence
                    e.hit(result.damage);
                    if (levelState != null) { // Check if levelState is set
                        levelState.addDamageNumber(result.damage, e.getx(), e.gety() - e.getHeight() / 2.0, result.isCrit);
                    }
                    scratchDamageDealt = true; // Ensure damage is dealt only once per scratch animation
                }
            }

            for (int j = 0; j < fireBalls.size(); j++) {
                FireBall fb = fireBalls.get(j);

                if (fb.intersects(e)) {
                    DamageResult result = calculateMagicDamage(fireBallDamage, intelligence, abilityDMG, null);
                    e.hit(result.damage);

                    if (levelState != null) {
                        levelState.addDamageNumber(result.damage, e.getx(), e.gety() - e.getHeight() / 2.0, result.isCrit);
                    }
                    fireBalls.remove(j);
                    break;
                }
            }

            if (intersects(e)) {
                hit(e.getDamage());
            }
        }
    }

    public void hit(int damage) {
        if(flinching) return;
        dead = false;
        finalDMG = damage * 100 / (100 + defence);
        health -= finalDMG;
        if(health < 0) health = 0;
        if(health == 0) {
            dead = true;
            levelState.recordPlayerDeath();
        }
        respawn(dead);
        flinching = true;
        flinchTimer = System.nanoTime();

    }

    private void getNextPosition() {

        if(flying) {
            dx = 0;
            dy = 0;
            if(left) dx = -maxSpeed;
            if(right) dx = maxSpeed;
            if(up) dy = -maxSpeed;
            if(down) dy = maxSpeed;
            return; // Don't process fall/jump physics
        }

        // movement
        if(left) {
            dx -= moveSpeed;
            if(dx < -maxSpeed) {
                dx = -maxSpeed;
            }
        } else if(right) {
            dx += moveSpeed;
            if(dx > maxSpeed) {
                dx = maxSpeed;
            }
        } else {
            if(dx > 0) {
                dx -= stopSpeed;
                if(dx < 0) {
                    dx = 0;
                }
            } else if (dx < 0) {
                dx += stopSpeed;
                if(dx > 0) {
                    dx = 0;
                }
            }
        }

        // cannot move while attacking, except in air
        if((currentAction == SCRATCHING || currentAction == FIREBALL) && !(jumping || falling)) {
            dx = 0;
        }

        // jumping
        if(jumping && !falling) {
            dy = jumpStart;
            falling = true;
        }

        // falling
        if(falling) {

            if(dy > 0 && gliding) dy += fallSpeed * 0.1;
            else dy += fallSpeed;

            if(dy > 0) jumping = false;
            if(dy < 0 && !jumping) dy += stopJumpSpeed;

            if(dy > maxFallSpeed) dy = maxFallSpeed;

        }

    }

    public void update() {

        // update position
        if(outOfMap) respawn(true);
        getNextPosition();
        checkTileMapCollision();
        setPosition(xtemp, ytemp);

        // check attack has stopped
        if(currentAction == SCRATCHING) {
            if(animation.hasPlayedOnce()) scratching = false;
        }
        if(currentAction == FIREBALL) {
            if(animation.hasPlayedOnce()) firing = false;
        }

        double now = System.nanoTime();
        double elapsedMillis = (now - lastRegenTime) / 1000000;

        // fireball attack
        if(elapsedMillis >= 1000) {
            regenAmount = maxIntelligence * (intelRegen/100);
            intelligence += regenAmount;
            if(intelligence > maxIntelligence) intelligence = maxIntelligence;
            lastIntelRegenTime = now;
        }

        if(intelligence > maxIntelligence) intelligence = maxIntelligence;

        if(firing && currentAction != FIREBALL) {
            if(intelligence > fireCost) {
                intelligence -= fireCost;
                FireBall fb = new FireBall(tileMap, facingRight);
                fb.setPosition(x, y);
                fireBalls.add(fb);
            }
        }

        // update fireballs
        for(int i = 0; i < fireBalls.size(); i++){
            fireBalls.get(i).update();
            if(fireBalls.get(i).shouldRemove()) {
                fireBalls.remove(i);
                i--;
            }
        }

        // check done flinching
        if(flinching) {
            long elapsed = (System.nanoTime() - flinchTimer) / 1000000;
            if(elapsed > 1000) {
                flinching = false;
            }
        }

        if(elapsedMillis >= 1000) {
            regenAmount = maxHealth * (regen/100);
            health += regenAmount;
            if(health > maxHealth) health = maxHealth;
            lastRegenTime = now;
        }

        if(health > maxHealth) health = maxHealth;

        // set animation
        if(scratching) {
            if(currentAction != SCRATCHING) {
                currentAction = SCRATCHING;
                animation.setFrames(sprites.get(SCRATCHING));
                animation.setDelay(50);
                width = 60;
            }
        } else if(firing) {
            if(currentAction != FIREBALL) {
                currentAction = FIREBALL;
                animation.setFrames(sprites.get(FIREBALL));
                animation.setDelay(100);
                width = 30;
            }
        } else if(dy > 0) {
            if(gliding) {
                if(currentAction != GLIDING) {
                    currentAction = GLIDING;
                    animation.setFrames(sprites.get(GLIDING));
                    animation.setDelay(100);
                    width = 30;
                }
            } else if(currentAction != FALLING) {
                currentAction = FALLING;
                animation.setFrames(sprites.get(FALLING));
                animation.setDelay(100);
                width = 30;
            }
        } else if(dy < 0) {
            if(currentAction != JUMPING) {
                currentAction = JUMPING;
                animation.setFrames(sprites.get(JUMPING));
                animation.setDelay(-1);
                width = 30;
            }
        } else if(left || right) {
            if(currentAction != WALKING) {
                currentAction = WALKING;
                animation.setFrames(sprites.get(WALKING));
                animation.setDelay(40);
                width = 30;
            }
        }
        else {
            if(currentAction != IDLE) {
                currentAction = IDLE;
                animation.setFrames(sprites.get(IDLE));
                animation.setDelay(400);
                width = 30;
            }
        }

        animation.update();

        // set direction
        if(currentAction != SCRATCHING && currentAction != FIREBALL) {
            if(right) facingRight = true;
            if(left) facingRight = false;
        }
    }

    public void draw(Graphics2D g) {

        setMapPosition();

        // draw fireballs
        for(int i = 0; i < fireBalls.size(); i++) {
            fireBalls.get(i).draw(g);
        }

        // draw player
        if(flinching) {
            long elapsed = (System.nanoTime() - flinchTimer) / 1000000;
            if(elapsed / 100 % 2 == 0) {
                return;
            }
        }
        super.draw(g);
    }

    public void respawn(boolean dead) {
        if(!dead) return;
        setPosition(100, 100);
        health = maxHealth;
        intelligence = maxIntelligence;
    }

    public void setSpeed(double speed) {
        maxSpeed = speed;
        moveSpeed = speed;
    }

    public void godMode(boolean god) {
        if(god) {
            maxHealth = Integer.MAX_VALUE - 1;
            maxIntelligence = Integer.MAX_VALUE - 1;
            scratchDamage = Integer.MAX_VALUE - 1;
            fireBallDamage = Integer.MAX_VALUE - 1;
            maxSpeed *= 3;
            intelligence = maxIntelligence;
            health = maxHealth;
            moveSpeed = maxSpeed;
            flying = true;
            falling = false;
            jumping = false;
        } else {
            maxHealth = 100;
            maxIntelligence = 100;
            moveSpeed = 0.3;
            maxSpeed = 1.6;
            fireBallDamage = 20;
            scratchDamage = 8;
            intelligence = maxIntelligence;
            health = maxHealth;
            flying = false;
            falling = true;
            jumping = true;
        }
    }

    public void fly(boolean fly) {
        flying = fly;
        if(flying) {
            falling = false;
            jumping = false;
            dy = 0;
        } else {
            falling = true;
            jumping = true;
        }
    }

    public DamageResult calculateDamage(int baseScratchDamage, int strength, double critChance, double critMultiplier, Integer targetDefence) {

        int defenceValue = (targetDefence == null) ? 0 : targetDefence;
        double rawDamage = baseScratchDamage + strength;
        boolean crit = false;

        if (Math.random() * 100 < critChance) {
            rawDamage *= (1.0 + (critMultiplier / 100.0));
            crit = true;

        }

        double finalDamageDouble = rawDamage * 100.0 / (100.0 + defenceValue);
        return new DamageResult((int) finalDamageDouble, crit, rawDamage);
    }

    public DamageResult calculateMagicDamage(int baseDamage, int intelligence, double abilityDamagePercent, Integer targetDefence) {

        int defenceValue = (targetDefence == null) ? 0 : targetDefence;
        double rawDamage = baseDamage + (intelligence * 1.5);
        rawDamage *= (1.0 + (abilityDamagePercent / 100.0));
        boolean crit = false;

        double finalDamageDouble = rawDamage * 100.0 / (100.0 + defenceValue);
        return new DamageResult((int) finalDamageDouble, crit, rawDamage);
    }
    public double getPositionX() {return xtemp;}
    public int getDefence() { return defence; }
    public int getStrength() { return strength; }
    public double getCritChance() { return CC; }
    public double getCritDamageMultiplier() { return critDMG; }
    public double getRegenRate() { return regen; }
    public int getIntelligence() { return intelligence; }
    public double getAbilityDamageBonus() { return abilityDMG; }

}
