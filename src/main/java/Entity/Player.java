package Entity;

import Effects.DamageResult;
import GameState.GameStateManager;
import GameState.Level1State;
import TileMap.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Player extends MapObject {

    private Level1State levelState;
    private GameStateManager gsm;


    public enum PlayerClass {
        NONE, MAGE, BERSERKER, ARCHER
    }
    private PlayerClass chosenClass;

    private static class ClassProgress {
        int level;
        int xp;
        int xpToNextLevel;
        double xpCurveMultiplier;

        ClassProgress(int initialLevel, int initialXp, int initialXpToNextLevel, double curveMultiplier) {
            this.level = initialLevel;
            this.xp = initialXp;
            this.xpToNextLevel = initialXpToNextLevel;
            this.xpCurveMultiplier = curveMultiplier;
        }
    }
    private Map<PlayerClass, ClassProgress> classProgressData;

    // Base Stats
    private static final int TRUE_BASE_MAX_HEALTH = 100;
    private static final int TRUE_BASE_DEFENSE = 10;
    private static final int TRUE_BASE_STRENGTH = 10;
    private static final int TRUE_BASE_MAX_INTELLIGENCE = 100;
    private static final double TRUE_BASE_ABILITY_DMG = 0.0;
    private static final double TRUE_BASE_MOVESPEED = 0.3;
    private static final double TRUE_BASE_MAXSPEED = 1.6;
    private static final int TRUE_BASE_SCRATCH_DAMAGE_VALUE = 8;
    private static final int TRUE_BASE_SCRATCH_RANGE = 35;
    private static final double TRUE_BASE_CC = 15.0;
    private static final double TRUE_BASE_CRIT_DAMAGE = 50.0;
    private static final int TRUE_BASE_ARROW_DMG = 10;

    // Mage Stats
    public static final int MAGE_STARTING_LEVEL = 1;
    public static final int MAGE_INITIAL_XP_TO_NEXT_LEVEL = 100;
    public static final double MAGE_XP_CURVE_MULTIPLIER = 1.5;
    public static final int MAGE_INTELLIGENCE_GAIN_PER_LEVEL = 5;
    public static final double MAGE_ABILITY_DMG_GAIN_PER_LEVEL = 2.5;

    // Berserk Stats
    public static final int BERSERKER_STARTING_LEVEL = 1;
    public static final int BERSERKER_INITIAL_XP_TO_NEXT_LEVEL = 120;

    public static final double BERSERKER_XP_CURVE_MULTIPLIER = 1.55;
    public static final int BERSERKER_STRENGTH_GAIN_PER_LEVEL = 4;
    public static final int BERSERKER_DEFENSE_GAIN_PER_LEVEL = 2;
    public static final double BERSERKER_MOVESPEED_GAIN_PER_LEVEL = 0.03;
    public static final int BERSERKER_SCRATCH_DAMAGE_GAIN_PER_LEVEL = 2;
    public static final double BERSERKER_MELEE_RANGE_GAIN_PER_LEVEL = 0.75;
    public static final int BERSERKER_HEALTH_GAIN_PER_LEVEL = 5;

    // Archer Stats
    public static final int ARCHER_STARTING_LEVEL = 1;
    public static final int ARCHER_INITIAL_XP_TO_NEXT_LEVEL = 110;
    public static final double ARCHER_XP_CURVE_MULTIPLIER = 1.5;
    public static final int ARCHER_PROJECTILE_DMG_BASE_BOOST = 5;
    public static final int ARCHER_MAX_JUMPS = 2;
    public static final int ARCHER_ARROW_DMG_GAIN_PER_LEVEL = 2;
    public static final double ARCHER_CC_GAIN_PER_LEVEL = 1.0;
    public static final double ARCHER_CD_GAIN_PER_LEVEL = 5.0;

    // Player Stats
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

    // Abilities
    private boolean firing;
    private int fireCost;
    private int fireBallDamage;
    private ArrayList<FireBall> fireBalls;

    private boolean scratching;
    private int scratchDamage;
    private double scratchRange;
    private boolean scratchDamageDealt = false;

    private boolean shootingArrow;
    private int arrowCost;
    private ArrayList<Arrow> arrows;
    private int arrowDMG;

    // Movement
    private boolean gliding;
    private int jumpsAvailable;
    private int maxJumps;

    // Animations
    private String spriteFilePath;
    private ArrayList<BufferedImage[]> sprites;
    private final int[] numFrames = {1, 2, 3, 4, 8};
    private static final int IDLE = 0;
    private static final int WALKING = 3;
    private static final int JUMPING = 1;
    private static final int FALLING = 2;
    private static final int GLIDING = 2;
    private static final int FIREBALL = 4;
    private static final int FIRING = 4;
    private static final int SCRATCHING = 4;

    public Player(TileMap tm, Level1State levelState, PlayerClass selectedClass, GameStateManager gsm) {
        super(tm);
        this.levelState = levelState;
        this.gsm = gsm;
        this.chosenClass = selectedClass != null ? selectedClass : PlayerClass.NONE;

        width = 30;
        height = 30;
        cwidth = 20;
        cheight = 20;

        stopSpeed = 0.4;
        fallSpeed = 0.15;
        maxFallSpeed = 4.0;
        jumpStart = -4.8;
        stopJumpSpeed = 0.3;
        facingRight = true;

        this.maxHealth = TRUE_BASE_MAX_HEALTH;
        this.health = this.maxHealth;
        this.defence = TRUE_BASE_DEFENSE;
        this.strength = TRUE_BASE_STRENGTH;
        this.maxIntelligence = TRUE_BASE_MAX_INTELLIGENCE;
        this.intelligence = this.maxIntelligence;
        this.abilityDMG = TRUE_BASE_ABILITY_DMG;
        this.moveSpeed = TRUE_BASE_MOVESPEED;
        this.maxSpeed = TRUE_BASE_MAXSPEED;
        this.scratchDamage = TRUE_BASE_SCRATCH_DAMAGE_VALUE;
        this.scratchRange = TRUE_BASE_SCRATCH_RANGE;

        lastRegenTime = System.nanoTime();
        lastIntelRegenTime = System.nanoTime();
        this.CC = TRUE_BASE_CC;
        this.critDMG = TRUE_BASE_CRIT_DAMAGE;
        regen = 2.0;
        intelRegen = 1.0;
        fireCost = 10;
        fireBallDamage = 20;
        arrowCost = 8;
        this.arrowDMG = TRUE_BASE_ARROW_DMG;

        fireBalls = new ArrayList<>();
        arrows = new ArrayList<>();
        maxJumps = (chosenClass == PlayerClass.ARCHER) ? ARCHER_MAX_JUMPS : 1;
        jumpsAvailable = maxJumps;

        classProgressData = new HashMap<>();
        classProgressData.put(PlayerClass.MAGE, new ClassProgress(MAGE_STARTING_LEVEL, 0, MAGE_INITIAL_XP_TO_NEXT_LEVEL, MAGE_XP_CURVE_MULTIPLIER));
        classProgressData.put(PlayerClass.BERSERKER, new ClassProgress(BERSERKER_STARTING_LEVEL, 0, BERSERKER_INITIAL_XP_TO_NEXT_LEVEL, BERSERKER_XP_CURVE_MULTIPLIER));
        classProgressData.put(PlayerClass.ARCHER, new ClassProgress(ARCHER_STARTING_LEVEL, 0, ARCHER_INITIAL_XP_TO_NEXT_LEVEL, ARCHER_XP_CURVE_MULTIPLIER));

        loadAllClassData();
        applyCurrentClassLevelBonuses();

        if(gsm.getSelectedPlayerClass() == PlayerClass.MAGE) {
            spriteFilePath = "/Sprites/Player/playersprites_mage.gif";
        } else if(gsm.getSelectedPlayerClass() == PlayerClass.BERSERKER) {
            spriteFilePath = "/Sprites/Player/playersprites_bers.gif";
            numFrames[4] = 6;
        } else if (gsm.getSelectedPlayerClass() == PlayerClass.ARCHER) {
            spriteFilePath = "/Sprites/Player/playersprites_archer.gif";
            numFrames[4] = 4;
        } else {
            spriteFilePath = "/Sprites/Player/playersprites.gif";
        }

        // Load sprites
        try {
            BufferedImage spritesheet = ImageIO.read(getClass().getResourceAsStream(spriteFilePath));

            sprites = new ArrayList<>();

            for (int i = 0; i < numFrames.length; i++) {
                BufferedImage[] bi = new BufferedImage[numFrames[i]];
                for (int j = 0; j < numFrames[i]; j++) {
                    bi[j] = spritesheet.getSubimage(j * width, i * height, width, height);
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

    public void setFiring(boolean shooting) {
        if(shooting) {

            if (chosenClass == PlayerClass.MAGE) {
                firing = true;
            } else if (chosenClass == PlayerClass.ARCHER) {
                shootingArrow = true;
            }
        }
    }

    public void setScratching(boolean b) {
        if(gsm.getSelectedPlayerClass() == PlayerClass.BERSERKER) {
            if (b && !scratching) {
                scratching = true;
                scratchDamageDealt = false;
            }
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
                    levelState.addDamageNumber(result.damage, e.getx(), e.gety() - e.getHeight() / 2.0, result.isCrit);
                    fireBalls.remove(j);
                    j--;
                }
            }

            for (int j = 0; j < arrows.size(); j++) {
                Arrow a = arrows.get(j);
                if(a.intersects(e)) {
                    DamageResult result = calculateDamage(ARCHER_PROJECTILE_DMG_BASE_BOOST + arrowDMG, strength, CC, critDMG, null);
                    e.hit(result.damage);
                    levelState.addDamageNumber(result.damage, e.getx(), e.gety() - e.getHeight() / 2.0, result.isCrit);
                    arrows.remove(j);
                    j--;
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
        if (flying) {
            dx = 0; dy = 0;
            if (left) dx = -maxSpeed; else if (right) dx = maxSpeed;
            if (up) dy = -maxSpeed; else if (down) dy = maxSpeed;
            return;
        }

        if (left) {
            dx -= moveSpeed;
            if (dx < -maxSpeed) dx = -maxSpeed;
        } else if (right) {
            dx += moveSpeed;
            if (dx > maxSpeed) dx = maxSpeed;
        } else {
            if (dx > 0) { dx -= stopSpeed; if (dx < 0) dx = 0; }
            else if (dx < 0) { dx += stopSpeed; if (dx > 0) dx = 0; }
        }

        if ((currentAction == SCRATCHING || currentAction == FIREBALL) && !falling) {
            dx = 0;
        }

        if (jumping && jumpsAvailable > 0) {
            falling = true;
        }

        if (falling) {
            if (gliding && dy > 0) {
                dy += fallSpeed * 0.1;
            } else {
                dy += fallSpeed;
            }

            if (dy < 0 && !jumping) {
                dy += stopJumpSpeed;
            }

            if (dy > maxFallSpeed) {
                dy = maxFallSpeed;
            }
        }
    }

    public void update() {
        if (outOfMap) respawn(true);
        getNextPosition();
        checkTileMapCollision();
        setPosition(xtemp, ytemp);

        if (currentAction == SCRATCHING && animation.hasPlayedOnce()) {
            scratching = false;
        }
        if (currentAction == FIREBALL && animation.hasPlayedOnce()) {
            firing = false;
            shootingArrow = false;
        }

        double now = System.nanoTime();
        double healthElapsedMillis = (now - lastRegenTime) / 1_000_000;
        double intelElapsedMillis = (now - lastIntelRegenTime) / 1_000_000;

        if (intelElapsedMillis >= 1000) {
            intelRegenAmount = maxIntelligence * (intelRegen / 100.0);
            intelligence += intelRegenAmount;
            if (intelligence > maxIntelligence) intelligence = maxIntelligence;
            lastIntelRegenTime = now;
        }

        if (healthElapsedMillis >= 1000) {
            regenAmount = maxHealth * (regen / 100.0);
            health += regenAmount;
            if (health > maxHealth) health = maxHealth;
            lastRegenTime = now;
        }

        // Fireball attack
        if (firing && currentAction != FIREBALL && intelligence >= fireCost) {
            intelligence -= fireCost;
            FireBall fb = new FireBall(tileMap, facingRight);
            fb.setPosition(x, y);
            fireBalls.add(fb);
        }

        // Arrow attack
        if (shootingArrow && chosenClass == PlayerClass.ARCHER && currentAction != FIREBALL && intelligence >= arrowCost) {
            intelligence -= arrowCost;
            Arrow a = new Arrow(tileMap, facingRight);
            a.setPosition(x, y);
            arrows.add(a);

        }

        // Update fireballs
        for (int i = 0; i < fireBalls.size(); i++) {
            fireBalls.get(i).update();
            if (fireBalls.get(i).shouldRemove()) {
                fireBalls.remove(i);
                i--;
            }
        }

        for(int i = 0; i < arrows.size(); i++) {
            arrows.get(i).update();
            if(arrows.get(i).shouldRemove()) {
                arrows.remove(i);
                i--;
            }
        }

        // Check flinching
        if (flinching) {
            long elapsed = (System.nanoTime() - flinchTimer) / 1_000_000;
            if (elapsed > 1000) {
                flinching = false;
            }
        }

        // Set animation
        if (scratching) {
            if (currentAction != SCRATCHING) {
                currentAction = SCRATCHING;
                animation.setFrames(sprites.get(SCRATCHING));
                animation.setDelay(50);
                width = 30;
            }
        } else if ((firing && chosenClass == PlayerClass.MAGE) || (shootingArrow && chosenClass == PlayerClass.ARCHER)) {
            if (currentAction != FIREBALL) {
                currentAction = FIREBALL;
                animation.setFrames(sprites.get(FIREBALL));
                animation.setDelay(100);
                width = 30;
            }
        } else if (dy < 0) {
            if (currentAction != JUMPING) {
                currentAction = JUMPING;
                animation.setFrames(sprites.get(JUMPING));
                animation.setDelay(100);
                width = 30;
            }
        } else if (dy > 0) {
            if (gliding && currentAction != GLIDING) {
                currentAction = GLIDING;
                animation.setFrames(sprites.get(GLIDING));
                animation.setDelay(100);
                width = 30;
            } else if (!gliding && currentAction != FALLING) {
                currentAction = FALLING;
                animation.setFrames(sprites.get(FALLING));
                animation.setDelay(100);
                width = 30;
            }
        } else if (left || right) {
            if (currentAction != WALKING) {
                currentAction = WALKING;
                animation.setFrames(sprites.get(WALKING));
                animation.setDelay(40);
                width = 30;
            }
        } else {
            if (currentAction != IDLE) {
                currentAction = IDLE;
                animation.setFrames(sprites.get(IDLE));
                animation.setDelay(400);
                width = 30;
            }
        }

        animation.update();

        // Set direction
        if (currentAction != SCRATCHING && currentAction != FIREBALL) {
            if (right) facingRight = true;
            if (left) facingRight = false;
        }
    }

    public void draw(Graphics2D g) {

        setMapPosition();

        // draw fireballs
        for(int i = 0; i < fireBalls.size(); i++) {
            fireBalls.get(i).draw(g);
        }

        // draw arrow
        for (int i = 0; i < arrows.size(); i++) {
            arrows.get(i).draw(g);
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

        setPosition(gsm.getCurrentState().getSpawnX(), gsm.getCurrentState().getSpawnY());
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
            this.maxHealth = TRUE_BASE_MAX_HEALTH;
            this.defence = TRUE_BASE_DEFENSE;
            this.strength = TRUE_BASE_STRENGTH;
            this.maxIntelligence = TRUE_BASE_MAX_INTELLIGENCE;
            this.abilityDMG = TRUE_BASE_ABILITY_DMG;
            this.moveSpeed = TRUE_BASE_MOVESPEED;
            this.maxSpeed = TRUE_BASE_MAXSPEED;
            this.scratchDamage = TRUE_BASE_SCRATCH_DAMAGE_VALUE;
            this.scratchRange = TRUE_BASE_SCRATCH_RANGE;
            this.CC = TRUE_BASE_CC;
            this.critDMG = TRUE_BASE_CRIT_DAMAGE;
            this.arrowDMG = TRUE_BASE_ARROW_DMG;
            applyCurrentClassLevelBonuses();
            this.health = this.maxHealth;
            this.intelligence = this.maxIntelligence;
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

    private void applyCurrentClassLevelBonuses() {
        this.maxHealth = TRUE_BASE_MAX_HEALTH;
        this.defence = TRUE_BASE_DEFENSE;
        this.strength = TRUE_BASE_STRENGTH;
        this.maxIntelligence = TRUE_BASE_MAX_INTELLIGENCE;
        this.abilityDMG = TRUE_BASE_ABILITY_DMG;
        this.moveSpeed = TRUE_BASE_MOVESPEED;
        this.maxSpeed = TRUE_BASE_MAXSPEED;
        this.scratchRange = TRUE_BASE_SCRATCH_RANGE;
        this.maxJumps = 1;

        ClassProgress progress = classProgressData.get(this.chosenClass);
        if (progress == null || this.chosenClass == PlayerClass.NONE) {
            this.intelligence = this.maxIntelligence;
            this.health = this.maxHealth;
            return;
        }

        int levelPoints = progress.level;

        if (this.chosenClass == PlayerClass.MAGE) {
            this.maxIntelligence += levelPoints * MAGE_INTELLIGENCE_GAIN_PER_LEVEL;
            this.abilityDMG += levelPoints * MAGE_ABILITY_DMG_GAIN_PER_LEVEL;
        } else if (this.chosenClass == PlayerClass.BERSERKER) {
            this.strength += levelPoints * BERSERKER_STRENGTH_GAIN_PER_LEVEL;
            this.defence += levelPoints * BERSERKER_DEFENSE_GAIN_PER_LEVEL;
            this.moveSpeed += levelPoints * BERSERKER_MOVESPEED_GAIN_PER_LEVEL;
            this.maxSpeed += levelPoints * BERSERKER_MOVESPEED_GAIN_PER_LEVEL;
            this.scratchRange += levelPoints * BERSERKER_MELEE_RANGE_GAIN_PER_LEVEL;
            this.maxHealth += levelPoints * BERSERKER_HEALTH_GAIN_PER_LEVEL;
            this.scratchDamage += levelPoints * BERSERKER_SCRATCH_DAMAGE_GAIN_PER_LEVEL;
        } else if (this.chosenClass == PlayerClass.ARCHER) {
            this.maxJumps = ARCHER_MAX_JUMPS;
            this.arrowDMG += levelPoints * ARCHER_ARROW_DMG_GAIN_PER_LEVEL;
            this.CC += levelPoints * ARCHER_CC_GAIN_PER_LEVEL;
            this.critDMG += levelPoints * ARCHER_CD_GAIN_PER_LEVEL;
        }

        this.health = this.maxHealth;
        this.intelligence = this.maxIntelligence;
        this.maxSpeed = Math.max(this.maxSpeed, this.moveSpeed);

    }

    public void addXP(int xpGained) {
        if (xpGained <= 0 || this.chosenClass == PlayerClass.NONE) {
            return;
        }
        ClassProgress currentClassProg = classProgressData.get(this.chosenClass);
        if (currentClassProg == null) return;

        currentClassProg.xp += xpGained;
        // System.out.println(chosenClass + " gained " + xpGained + " XP. Current: " + currentClassProg.xp + "/" + currentClassProg.xpToNextLevel);

        boolean hasLeveledUp = false;
        while (currentClassProg.xp >= currentClassProg.xpToNextLevel) {
            currentClassProg.xp -= currentClassProg.xpToNextLevel;
            currentClassProg.level++;
            currentClassProg.xpToNextLevel = (int) (currentClassProg.xpToNextLevel * currentClassProg.xpCurveMultiplier);
            hasLeveledUp = true;
            // System.out.println(chosenClass + " Leveled Up! New Level: " + currentClassProg.level);
        }

        if (hasLeveledUp) {
            applyCurrentClassLevelBonuses();
        }
        saveAllClassData();
    }

    public int getCurrentClassLevel() {
        if (chosenClass == PlayerClass.NONE || !classProgressData.containsKey(chosenClass)) return 0;
        return classProgressData.get(chosenClass).level;
    }
    public int getCurrentClassXP() {
        if (chosenClass == PlayerClass.NONE || !classProgressData.containsKey(chosenClass)) return 0;
        return classProgressData.get(chosenClass).xp;
    }
    public int getCurrentClassXPToNextLevel() {
        if (chosenClass == PlayerClass.NONE || !classProgressData.containsKey(chosenClass)) return 100; // Default
        return classProgressData.get(chosenClass).xpToNextLevel;
    }
    public PlayerClass getChosenClass() { return chosenClass; }

    public void saveAllClassData() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("player_save.txt"))) {
            for (Map.Entry<PlayerClass, ClassProgress> entry : classProgressData.entrySet()) {
                if (entry.getKey() == PlayerClass.NONE) continue;
                PlayerClass pc = entry.getKey();
                ClassProgress prog = entry.getValue();
                writer.write(pc.name() + "_level=" + prog.level); writer.newLine();
                writer.write(pc.name() + "_xp=" + prog.xp); writer.newLine();
                writer.write(pc.name() + "_xpToNext=" + prog.xpToNextLevel); writer.newLine();
            }
            // System.out.println("All class data saved.");
        } catch (IOException e) {
            System.err.println("Error saving class data: " + e.getMessage());
        }
    }

    public void loadAllClassData() {
        File saveFile = new File("player_save.txt");
        if (!saveFile.exists()) {
            // System.out.println("No class save file found. Using default values for all classes.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(saveFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    String key = parts[0];
                    int value = Integer.parseInt(parts[1]);

                    if (key.endsWith("_level")) {
                        PlayerClass pc = PlayerClass.valueOf(key.substring(0, key.indexOf("_level")).toUpperCase());
                        if (classProgressData.containsKey(pc)) classProgressData.get(pc).level = value;
                    } else if (key.endsWith("_xp")) {
                        PlayerClass pc = PlayerClass.valueOf(key.substring(0, key.indexOf("_xp")).toUpperCase());
                        if (classProgressData.containsKey(pc)) classProgressData.get(pc).xp = value;
                    } else if (key.endsWith("_xpToNext")) {
                        PlayerClass pc = PlayerClass.valueOf(key.substring(0, key.indexOf("_xpToNext")).toUpperCase());
                        if (classProgressData.containsKey(pc)) classProgressData.get(pc).xpToNextLevel = value;
                    }
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Error loading class data, resetting to defaults: " + e.getMessage());
            classProgressData.clear();
            classProgressData.put(PlayerClass.MAGE, new ClassProgress(MAGE_STARTING_LEVEL, 0, MAGE_INITIAL_XP_TO_NEXT_LEVEL, MAGE_XP_CURVE_MULTIPLIER));
            classProgressData.put(PlayerClass.BERSERKER, new ClassProgress(BERSERKER_STARTING_LEVEL, 0, BERSERKER_INITIAL_XP_TO_NEXT_LEVEL, BERSERKER_XP_CURVE_MULTIPLIER));
            classProgressData.put(PlayerClass.ARCHER, new ClassProgress(ARCHER_STARTING_LEVEL, 0, ARCHER_INITIAL_XP_TO_NEXT_LEVEL, ARCHER_XP_CURVE_MULTIPLIER));
        }
    }


    @Override
    public void setJumping(boolean b) {
        if (b) {
            if (jumpsAvailable > 0 && !jumping) {
                this.jumping = true;
                this.dy = jumpStart;
                this.falling = true;
                this.jumpsAvailable--;
                // System.out.println("Jumped! Jumps left: " + jumpsAvailable);
            }
        } else {
            this.jumping = false;
        }
    }

    @Override
    public void checkTileMapCollision() {
        double oldDy = dy;
        boolean wasFalling = falling;

        super.checkTileMapCollision();

        if ((wasFalling && !falling) || (wasFalling && dy == 0 && oldDy > 0)) {
            falling = false;
            jumpsAvailable = maxJumps;
            jumping = false;
            // System.out.println("Landed! Jumps  to: " + jumpsAvailable);
        }
    }

    public double getPositionX() {return xtemp;}
    public int getDefence() { return defence; }
    public int getStrength() { return strength; }
    public double getCritChance() { return CC; }
    public double getCritDamage() { return critDMG; }
    public double getRegenRate() { return regen; }
    public int getIntelligence() { return intelligence; }
    public double getAbilityDamageBonus() { return abilityDMG; }

}