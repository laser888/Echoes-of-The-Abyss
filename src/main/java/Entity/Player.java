package Entity;

import Blessing.Blessing;
import Effects.DamageResult;
import Entity.Enemies.Bosses.CloneBoss;
import Entity.Enemies.Bosses.FinalBoss;
import Entity.Enemies.Skeleton;
import Entity.Enemies.Bosses.WizardBoss;
import Entity.Projectiles.Arrow;
import Entity.Projectiles.FireBall;
import GameState.GameStateManager;
import GameState.BaseLevelState;
import TileMap.*;
import Data.GameData;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

// Manages player behavior
public class Player extends MapObject {
    private BaseLevelState currentLevelState; // Current level state
    private GameStateManager gsm; // Game state manager
    private GameData gameData; // Game data reference
    public enum PlayerClass {
        NONE, MAGE, BERSERKER, ARCHER
    }
    private PlayerClass chosenClass; // Selected class
    private ArrayList<Blessing> blessings = new ArrayList<>(); // Active blessings

    private static class ClassProgress {
        int level; // Class level
        int xp; // Current XP
        int xpToNextLevel; // XP needed for next level
        double xpCurveMultiplier; // XP growth multiplier
        ClassProgress(int initialLevel, int initialXp, int initialXpToNextLevel, double curveMultiplier) {
            this.level = initialLevel;
            this.xp = initialXp;
            this.xpToNextLevel = initialXpToNextLevel;
            this.xpCurveMultiplier = curveMultiplier;
        }
    }
    private Map<PlayerClass, ClassProgress> classProgressData; // Class progress data

    // Base Stats
    private static final int TRUE_BASE_MAX_HEALTH = 600;
    private static final int TRUE_BASE_DEFENSE = 25;
    private static final int TRUE_BASE_STRENGTH = 40;
    private static final int TRUE_BASE_MAX_INTELLIGENCE = 100;
    private static final double TRUE_BASE_ABILITY_DMG = 0.0;
    private static final double TRUE_BASE_MOVESPEED = 0.3;
    private static final double TRUE_BASE_MAXSPEED = 1.6;
    private static final int TRUE_BASE_SCRATCH_DAMAGE = 120;
    private static final int TRUE_BASE_SCRATCH_RANGE = 35;
    private static final double TRUE_BASE_CC = 15.0;
    private static final double TRUE_BASE_CRIT_DAMAGE = 50.0;
    private static final int TRUE_BASE_ARROW_DMG = 120;
    private static final double TRUE_BASE_INTEL_REGEN = 1.0;
    private static final int TRUE_BASE_FIREBALL_DAMAGE = 100;

    // Mage Stats
    public static final int MAGE_STARTING_LEVEL = 1;
    public static final int MAGE_INTELLIGENCE_GAIN_PER_LEVEL = 15;
    public static final double MAGE_ABILITY_DMG_GAIN_PER_LEVEL = 2.5;
    public static final double MAGE_XP_CURVE_MULTIPLIER = 1.05;
    public static final int MAGE_INITIAL_XP_TO_NEXT_LEVEL = 300;
    public static final double MAGE_INTEL_REGEN = 3.0;

    // Berserker Stats
    public static final int BERSERKER_STARTING_LEVEL = 1;
    public static final int BERSERKER_INITIAL_XP_TO_NEXT_LEVEL = 300;
    public static final double BERSERKER_XP_CURVE_MULTIPLIER = 1.05;
    public static final int BERSERKER_HEALTH_GAIN_PER_LEVEL = 25;
    public static final int BERSERKER_STRENGTH_GAIN_PER_LEVEL = 10;
    public static final int BERSERKER_DEFENSE_GAIN_PER_LEVEL = 5;
    public static final int BERSERKER_SCRATCH_DAMAGE_GAIN_PER_LEVEL = 5;
    public static final double BERSERKER_MOVESPEED_GAIN_PER_LEVEL = 0.01;
    public static final double BERSERKER_MELEE_RANGE_GAIN_PER_LEVEL = 1.0;

    // Archer Stats
    public static final int ARCHER_STARTING_LEVEL = 1;
    public static final int ARCHER_INITIAL_XP_TO_NEXT_LEVEL = 300;
    public static final double ARCHER_XP_CURVE_MULTIPLIER = 1.05;
    public static final int ARCHER_PROJECTILE_DMG_BASE_BOOST = 10;
    public static final int ARCHER_MAX_JUMPS = 2;
    public static final int ARCHER_ARROW_DMG_GAIN_PER_LEVEL = 6;
    public static final double ARCHER_CC_GAIN_PER_LEVEL = 1.2;
    public static final double ARCHER_CD_GAIN_PER_LEVEL = 5.0;
    public static final double ARCHER_INTEL_REGEN = 4.0;

    // Player Stats
    private int health; // Current health
    private int maxHealth; // Maximum health
    private int defence; // Defense value
    private int strength; // Strength value
    private double CC; // Critical chance
    private double critDMG; // Critical damage multiplier
    private double regen; // Health regen rate
    private double finalDMG; // Final damage taken
    private double regenAmount; // Health regen amount
    private double lastRegenTime; // Last health regen time (ns)
    private double lastIntelRegenTime; // Last intelligence regen time (ns)
    private double intelRegenAmount; // Intelligence regen amount
    private double intelRegen; // Intelligence regen rate
    private double abilityDMG; // Ability damage multiplier
    private int intelligence; // Current intelligence
    private int maxIntelligence; // Maximum intelligence
    private boolean dead; // Death state
    private boolean flinching; // Flinch state
    private long flinchTimer; // Flinch timer (ns)
    private boolean flying; // Flying state
    private boolean immune; // Immunity state
    private long immuneTimer; // Immunity timer (ns)
    private static final long IMMUNITY_DURATION_NANO = 2_000_000_000L; // Immunity duration (ns)

    // Abilities
    private boolean firing; // Fireball state
    private int fireCost; // Fireball intelligence cost
    private int fireBallDamage; // Fireball damage
    private ArrayList<FireBall> fireBalls; // Fireball projectiles

    private boolean scratching; // Scratch state
    private int scratchDamage; // Scratch damage
    private double scratchRange; // Scratch range (pixels)
    private boolean scratchDamageDealt; // Scratch damage flag

    private boolean shootingArrow; // Arrow state
    private int arrowCost; // Arrow intelligence cost
    private ArrayList<Arrow> arrows; // Arrow projectiles
    private int arrowDMG; // Arrow damage

    // Movement
    private boolean gliding; // Gliding state
    private int jumpsAvailable; // Available jumps
    private int maxJumps; // Maximum jumps

    // Animations
    private String spriteFilePath; // Sprite sheet path
    private ArrayList<BufferedImage[]> sprites; // Animation sprites
    private int[] numFrames = {1, 2, 3, 4, 5}; // Frames per animation
    private static final int IDLE = 0; // Idle animation
    private static final int WALKING = 3; // Walking animation
    private static final int JUMPING = 1; // Jumping animation
    private static final int FALLING = 2; // Falling animation
    private static final int GLIDING = 2; // Gliding animation
    private static final int FIREBALL = 4; // Fireball animation
    private static final int SCRATCHING = 4; // Scratching animation

    // Initializes Player
    public Player(TileMap tm, BaseLevelState levelState, PlayerClass selectedClass, GameStateManager gsm, GameData gameData) {
        super(tm);
        this.currentLevelState = levelState;
        this.gsm = gsm;
        this.gameData = gameData;
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
        this.scratchDamage = TRUE_BASE_SCRATCH_DAMAGE;
        this.scratchRange = TRUE_BASE_SCRATCH_RANGE;

        lastRegenTime = System.nanoTime();
        lastIntelRegenTime = System.nanoTime();
        this.CC = TRUE_BASE_CC;
        this.critDMG = TRUE_BASE_CRIT_DAMAGE;
        regen = 2.0;
        this.intelRegen = chosenClass == PlayerClass.MAGE ? MAGE_INTEL_REGEN : chosenClass == PlayerClass.ARCHER ? ARCHER_INTEL_REGEN : TRUE_BASE_INTEL_REGEN;
        fireCost = 10;
        this.fireBallDamage = TRUE_BASE_FIREBALL_DAMAGE;
        arrowCost = 6;
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

        if (gsm != null && gsm.getSelectedPlayerClass() == PlayerClass.MAGE) {
            spriteFilePath = "/Sprites/Player/playersprites_mage.gif";
        } else if (gsm != null && gsm.getSelectedPlayerClass() == PlayerClass.BERSERKER) {
            spriteFilePath = "/Sprites/Player/playersprites_bers.gif";
            numFrames[4] = 6;
        } else if (gsm != null && gsm.getSelectedPlayerClass() == PlayerClass.ARCHER) {
            spriteFilePath = "/Sprites/Player/playersprites_archer.gif";
            numFrames[4] = 3;
        } else {
            spriteFilePath = "/Sprites/Player/playersprites.gif";
        }

        sprites = new ArrayList<>();
        loadSprites();
    }

    // Loads player sprites
    private void loadSprites() {
        try {
            BufferedImage spritesheet = ImageIO.read(getClass().getResourceAsStream(spriteFilePath));
            if (spritesheet == null || spritesheet.getWidth() < width * numFrames[4] || spritesheet.getHeight() < height * numFrames.length) {
                throw new IOException("Invalid sprite sheet");
            }
            for (int i = 0; i < numFrames.length; i++) {
                BufferedImage[] bi = new BufferedImage[numFrames[i]];
                for (int j = 0; j < numFrames[i]; j++) {
                    bi[j] = spritesheet.getSubimage(j * width, i * height, width, height);
                }
                sprites.add(bi);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Creates placeholder sprites
            BufferedImage placeholder = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = placeholder.createGraphics();
            g.setColor(Color.MAGENTA);
            g.fillRect(0, 0, width, height);
            g.dispose();
            for (int i = 0; i < numFrames.length; i++) {
                BufferedImage[] bi = new BufferedImage[numFrames[i]];
                Arrays.fill(bi, placeholder);
                sprites.add(bi);
            }
        }
        animation = new Animation();
        currentAction = IDLE;
        animation.setFrames(sprites.isEmpty() ? null : sprites.get(IDLE));
        animation.setDelay(400);
    }

    // Gets current health
    public int getHealth() { return health; }

    // Gets maximum health
    public int getMaxHealth() { return maxHealth; }

    // Gets maximum intelligence
    public int getMaxIntelligence() { return maxIntelligence; }

    // Checks if player is dead
    public boolean isDead() { return dead; }

    // Sets firing state
    public void setFiring(boolean shooting) {
        if (shooting) {
            if (chosenClass == PlayerClass.MAGE) {
                firing = true;
            } else if (chosenClass == PlayerClass.ARCHER) {
                shootingArrow = true;
            }
        }
    }

    // Sets scratching state
    public void setScratching(boolean b) {
        if (gsm != null && gsm.getSelectedPlayerClass() == PlayerClass.BERSERKER) {
            if (b && !scratching) {
                scratching = true;
                scratchDamageDealt = false;
            }
        }
    }

    // Sets gliding state
    public void setGliding(boolean b) {
        gliding = b;
    }

    // Checks attacks against enemies
    public void checkAttack(ArrayList<Enemy> enemies) {
        if (enemies == null || currentLevelState == null) {
            System.err.println("Player checkAttack skipped: enemies or currentLevelState is null");
            return;
        }
        for (int i = 0; i < enemies.size(); i++) {
            Enemy e = enemies.get(i);
            if (e == null) continue;
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
                    DamageResult result = calculateDamage(scratchDamage, strength, CC, 0);
                    e.hit(result.damage);
                    currentLevelState.addDamageNumber(result.damage, e.getx(), e.gety() - e.getHeight() / 2.0, result.isCrit);
                    scratchDamageDealt = true;
                }
            }
            for (int j = 0; j < fireBalls.size(); j++) {
                FireBall fb = fireBalls.get(j);
                if (fb == null) continue;
                if (fb.intersects(e)) {
                    DamageResult result = calculateMagicDamage(fireBallDamage, intelligence, abilityDMG, 0);
                    e.hit(result.damage);
                    currentLevelState.addDamageNumber(result.damage, e.getx(), e.gety() - e.getHeight() / 2.0, result.isCrit);
                    fireBalls.remove(j);
                    j--;
                }
            }
            for (int j = 0; j < arrows.size(); j++) {
                Arrow a = arrows.get(j);
                if (a == null) continue;
                if (a.intersects(e)) {
                    DamageResult result = calculateDamage(ARCHER_PROJECTILE_DMG_BASE_BOOST + arrowDMG, strength, CC, 0);
                    e.hit(result.damage);
                    currentLevelState.addDamageNumber(result.damage, e.getx(), e.gety() - e.getHeight() / 2.0, result.isCrit);
                    arrows.remove(j);
                    j--;
                }
            }
            if (intersects(e) && !(e instanceof Skeleton) && !(e instanceof FinalBoss) && !(e instanceof WizardBoss) && !(e instanceof CloneBoss)) {
                hit(e.getDamage());
            }
        }
    }

    // Applies damage to player
    public void hit(int damage) {
        if (flinching || immune) return;
        dead = false;
        finalDMG = damage * 100 / (100 + defence);
        health -= finalDMG;
        if (health < 0) health = 0;
        if (health == 0) {
            dead = true;
            respawn();
            if (currentLevelState != null) currentLevelState.recordPlayerDeath();
        }
        flinching = true;
        flinchTimer = System.nanoTime();
    }

    // Calculates next position
    private void getNextPosition() {
        if (tileMap == null) {
            System.err.println("Player getNextPosition skipped: tileMap is null");
            return;
        }
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

    // Updates player state
    public void update() {
        if (tileMap == null) {
            System.err.println("Player update skipped: tileMap is null");
            return;
        }
        if (outOfMap) respawn();
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

        if (immune) {
            long elapsed = (System.nanoTime() - immuneTimer) / 1_000_000;
            if (elapsed > IMMUNITY_DURATION_NANO / 1_000_000) {
                immune = false;
                System.out.println("Player immunity ended");
            }
        }

        if (firing && currentAction != FIREBALL && intelligence >= fireCost) {
            intelligence -= fireCost;
            FireBall fb = new FireBall(tileMap, facingRight);
            fb.setPosition(x, y);
            fireBalls.add(fb);
        }

        if (shootingArrow && chosenClass == PlayerClass.ARCHER && currentAction != FIREBALL && intelligence >= arrowCost) {
            intelligence -= arrowCost;
            Arrow a = new Arrow(tileMap, facingRight, false);
            a.setPosition(x, y);
            arrows.add(a);
        }

        for (int i = 0; i < fireBalls.size(); i++) {
            FireBall fb = fireBalls.get(i);
            if (fb != null) {
                fb.update();
                if (fb.shouldRemove()) {
                    fireBalls.remove(i);
                    i--;
                }
            }
        }

        for (int i = 0; i < arrows.size(); i++) {
            Arrow a = arrows.get(i);
            if (a != null) {
                a.update();
                if (a.shouldRemove()) {
                    arrows.remove(i);
                    i--;
                }
            }
        }

        if (flinching) {
            long elapsed = (System.nanoTime() - flinchTimer) / 1_000_000;
            if (elapsed > 1000) {
                flinching = false;
            }
        }

        if (scratching) {
            if (currentAction != SCRATCHING) {
                currentAction = SCRATCHING;
                animation.setFrames(sprites != null && sprites.size() > SCRATCHING ? sprites.get(SCRATCHING) : null);
                animation.setDelay(50);
                width = 30;
            }
        } else if ((firing && chosenClass == PlayerClass.MAGE) || (shootingArrow && chosenClass == PlayerClass.ARCHER)) {
            if (currentAction != FIREBALL) {
                currentAction = FIREBALL;
                animation.setFrames(sprites != null && sprites.size() > FIREBALL ? sprites.get(FIREBALL) : null);
                animation.setDelay(40);
                width = 30;
            }
        } else if (dy < 0) {
            if (currentAction != JUMPING) {
                currentAction = JUMPING;
                animation.setFrames(sprites != null && sprites.size() > JUMPING ? sprites.get(JUMPING) : null);
                animation.setDelay(100);
                width = 30;
            }
        } else if (dy > 0) {
            if (gliding && currentAction != GLIDING) {
                currentAction = GLIDING;
                animation.setFrames(sprites != null && sprites.size() > GLIDING ? sprites.get(GLIDING) : null);
                animation.setDelay(100);
                width = 30;
            } else if (!gliding && currentAction != FALLING) {
                currentAction = FALLING;
                animation.setFrames(sprites != null && sprites.size() > FALLING ? sprites.get(FALLING) : null);
                animation.setDelay(100);
                width = 30;
            }
        } else if (left || right) {
            if (currentAction != WALKING) {
                currentAction = WALKING;
                animation.setFrames(sprites != null && sprites.size() > WALKING ? sprites.get(WALKING) : null);
                animation.setDelay(40);
                width = 30;
            }
        } else {
            if (currentAction != IDLE) {
                currentAction = IDLE;
                animation.setFrames(sprites != null && sprites.size() > IDLE ? sprites.get(IDLE) : null);
                animation.setDelay(400);
                width = 30;
            }
        }

        if (animation != null) animation.update();

        if (currentAction != SCRATCHING && currentAction != FIREBALL) {
            if (right) facingRight = true;
            if (left) facingRight = false;
        }
    }

    // Draws player and projectiles
    public void draw(Graphics2D g) {
        if (g == null || tileMap == null) {
            System.err.println("Player draw skipped: g or tileMap is null");
            return;
        }
        setMapPosition();
        for (FireBall fb : fireBalls) {
            if (fb != null) fb.draw(g);
        }
        for (Arrow a : arrows) {
            if (a != null) a.draw(g);
        }
        if (flinching || immune) {
            long elapsed = (System.nanoTime() - flinchTimer) / 1000000;
            if (elapsed / 100 % 2 == 0) {
                return;
            }
        }
        super.draw(g);
    }

    // Respawns player
    public void respawn() {
        if (dead || outOfMap) {
            if (currentLevelState != null && BaseLevelState.inTerminal) currentLevelState.closeActiveTerminal();
            setPosition(gsm != null && gsm.getCurrentState() != null ? gsm.getCurrentState().getSpawnX() : 0, gsm != null && gsm.getCurrentState() != null ? gsm.getCurrentState().getSpawnY() : 0);
            health = maxHealth;
            intelligence = maxIntelligence;
            dead = false;
            immune = true;
            immuneTimer = System.nanoTime();
        }
    }

    // Sets movement speed
    public void setSpeed(double speed) {
        maxSpeed = speed;
        moveSpeed = speed;
    }

    // Toggles god mode
    public void godMode(boolean god) {
        if (god) {
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
            maxHealth = TRUE_BASE_MAX_HEALTH;
            defence = TRUE_BASE_DEFENSE;
            strength = TRUE_BASE_STRENGTH;
            maxIntelligence = TRUE_BASE_MAX_INTELLIGENCE;
            abilityDMG = TRUE_BASE_ABILITY_DMG;
            moveSpeed = TRUE_BASE_MOVESPEED;
            maxSpeed = TRUE_BASE_MAXSPEED;
            scratchDamage = TRUE_BASE_SCRATCH_DAMAGE;
            scratchRange = TRUE_BASE_SCRATCH_RANGE;
            CC = TRUE_BASE_CC;
            critDMG = TRUE_BASE_CRIT_DAMAGE;
            arrowDMG = TRUE_BASE_ARROW_DMG;
            applyCurrentClassLevelBonuses();
            health = maxHealth;
            intelligence = maxIntelligence;
            flying = false;
            falling = true;
            jumping = true;
        }
    }

    // Toggles flying mode
    public void fly(boolean fly) {
        flying = fly;
        if (flying) {
            falling = false;
            jumping = false;
            dy = 0;
        } else {
            falling = true;
            jumping = true;
        }
    }

    // Calculates physical damage
    public DamageResult calculateDamage(int baseSkillDamage, int strength, double critChance, int targetDefence) {
        double rawDamage = baseSkillDamage + strength;
        boolean crit = Math.random() * 100 < critChance;
        if (crit) {
            rawDamage *= (1.0 + (critDMG / 100.0));
        }
        double finalDamageDouble = rawDamage * 100.0 / (100.0 + targetDefence);
        return new DamageResult((int) finalDamageDouble, crit, rawDamage);
    }

    // Calculates magic damage
    public DamageResult calculateMagicDamage(int baseSkillDamage, int intelligence, double abilityDamagePercent, int targetDefence) {
        double rawDamage = baseSkillDamage + (intelligence * 1.2);
        rawDamage *= (1.0 + (abilityDamagePercent / 100.0));
        boolean crit = false;
        double finalDamageDouble = rawDamage * 100.0 / (100.0 + targetDefence);
        return new DamageResult((int) finalDamageDouble, crit, rawDamage);
    }

    // Applies class level bonuses
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
            this.intelRegen = MAGE_INTEL_REGEN;
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
            this.intelRegen = ARCHER_INTEL_REGEN;
        }

        this.health = this.maxHealth;
        this.intelligence = this.maxIntelligence;
        this.maxSpeed = Math.max(this.maxSpeed, this.moveSpeed);
    }

    // Adds experience points
    public void addXP(int xpGained) {
        if (xpGained <= 0 || this.chosenClass == PlayerClass.NONE) {
            return;
        }
        ClassProgress currentClassProg = classProgressData.get(this.chosenClass);
        if (currentClassProg == null) return;

        currentClassProg.xp += xpGained;
        boolean hasLeveledUp = false;
        while (currentClassProg.xp >= currentClassProg.xpToNextLevel) {
            currentClassProg.xp -= currentClassProg.xpToNextLevel;
            currentClassProg.level++;
            currentClassProg.xpToNextLevel = (int) (currentClassProg.xpToNextLevel * currentClassProg.xpCurveMultiplier);
            hasLeveledUp = true;
        }

        if (hasLeveledUp) {
            applyCurrentClassLevelBonuses();
        }
        saveAllClassData();
    }

    // Gets current class level
    public int getCurrentClassLevel() {
        if (chosenClass == PlayerClass.NONE || !classProgressData.containsKey(chosenClass)) return 0;
        return classProgressData.get(chosenClass).level;
    }

    // Gets current class XP
    public int getCurrentClassXP() {
        if (chosenClass == PlayerClass.NONE || !classProgressData.containsKey(chosenClass)) return 0;
        return classProgressData.get(chosenClass).xp;
    }

    // Gets XP needed for next level
    public int getCurrentClassXPToNextLevel() {
        if (chosenClass == PlayerClass.NONE || !classProgressData.containsKey(chosenClass)) return 100;
        return classProgressData.get(chosenClass).xpToNextLevel;
    }

    // Gets chosen class
    public PlayerClass getChosenClass() { return chosenClass; }

    // Saves class data
    public void saveAllClassData() {
        if (gameData == null) {
            System.err.println("Cannot save player data, GameData object is null.");
            return;
        }

        gameData.playerClassProgress.clear();

        for (Map.Entry<PlayerClass, ClassProgress> entry : classProgressData.entrySet()) {
            if (entry.getKey() == PlayerClass.NONE) continue;

            String className = entry.getKey().name();
            ClassProgress prog = entry.getValue();

            Map<String, Integer> singleClassData = new HashMap<>();
            singleClassData.put("level", prog.level);
            singleClassData.put("xp", prog.xp);
            singleClassData.put("xpToNextLevel", prog.xpToNextLevel);

            gameData.playerClassProgress.put(className, singleClassData);
        }
    }

    // Loads class data
    public void loadAllClassData() {
        if (gameData == null || gameData.playerClassProgress == null || gameData.playerClassProgress.isEmpty()) {
            System.out.println("No player class data found in GameData object. Using default values.");
            return;
        }

        for (Map.Entry<String, Map<String, Integer>> entry : gameData.playerClassProgress.entrySet()) {
            try {
                PlayerClass pc = PlayerClass.valueOf(entry.getKey());
                Map<String, Integer> savedProgress = entry.getValue();

                if (classProgressData.containsKey(pc)) {
                    ClassProgress targetProgress = classProgressData.get(pc);
                    targetProgress.level = savedProgress.getOrDefault("level", 1);
                    targetProgress.xp = savedProgress.getOrDefault("xp", 0);
                    targetProgress.xpToNextLevel = savedProgress.getOrDefault("xpToNextLevel", 100);
                }
            } catch (IllegalArgumentException e) {
                System.err.println("Warning: Found unknown class '" + entry.getKey() + "' in save data. Ignoring.");
            }
        }
    }

    // Sets jumping state
    @Override
    public void setJumping(boolean b) {
        if (b) {
            if (jumpsAvailable <= 0) return;
            if ((chosenClass != PlayerClass.ARCHER && falling) || (chosenClass != PlayerClass.ARCHER && jumping))
                return;

            this.jumping = true;
            this.dy = jumpStart;
            this.falling = true;
            this.jumpsAvailable--;
        } else {
            this.jumping = false;
        }
    }

    // Checks tile map collisions
    @Override
    public void checkTileMapCollision() {
        double oldDy = dy;
        boolean wasFalling = falling;

        super.checkTileMapCollision();

        if ((wasFalling && !falling) || (wasFalling && dy == 0 && oldDy > 0)) {
            falling = false;
            jumpsAvailable = maxJumps;
            jumping = false;
        }
    }

    // Applies blessing effects
    public void applyBlessings(Blessing blessing) {
        if (blessing == null) return;
        blessings.add(blessing);
        double multiplier = blessing.getValue();
        System.out.println("Applying " + blessing.getType() + " blessing. Multiplier: " + multiplier);

        switch (blessing.getType()) {
            case STRENGTH:
                strength = (int)(strength * multiplier);
                break;
            case CRITDAMAGE:
                critDMG *= multiplier;
                CC += (multiplier - 1.0) * 10;
                break;
            case DAMAGE:
                scratchDamage = (int)(scratchDamage * multiplier);
                arrowDMG = (int)(arrowDMG * multiplier);
                fireBallDamage = (int)(fireBallDamage * multiplier);
                break;
            case SPEED:
                maxSpeed *= multiplier;
                moveSpeed *= multiplier;
                break;
            case HEALTH:
                maxHealth = (int)(maxHealth * multiplier);
                health = maxHealth;
                break;
            case DEFENCE:
                defence = (int)(defence * multiplier);
                break;
            case INTELLIGENCE:
                maxIntelligence *= multiplier;
                break;
        }
    }

    // Clears all blessings
    public void clearBlessings() {
        for (int i = blessings.size() - 1; i >= 0; i--) {
            Blessing b = blessings.get(i);
            double inverseMultiplier = 1.0 / b.getValue();
            switch (b.getType()) {
                case STRENGTH: strength = (int)(strength * inverseMultiplier); break;
                case CRITDAMAGE:
                    critDMG *= inverseMultiplier;
                    CC -= (b.getValue() - 1.0) * 10;
                    break;
                case DAMAGE:
                    scratchDamage = (int)(scratchDamage * inverseMultiplier);
                    arrowDMG = (int)(arrowDMG * inverseMultiplier);
                    fireBallDamage = (int)(fireBallDamage * inverseMultiplier);
                    break;
                case SPEED:
                    maxSpeed *= inverseMultiplier;
                    moveSpeed *= inverseMultiplier;
                    break;
                case HEALTH:
                    maxHealth = (int)(maxHealth * inverseMultiplier);
                    health = Math.min(health, maxHealth);
                    break;
                case DEFENCE:
                    defence = (int)(defence * inverseMultiplier);
                    break;
                case INTELLIGENCE:
                    maxIntelligence = (int)(maxIntelligence * inverseMultiplier);
                    break;
            }
        }
        blessings.clear();
        applyCurrentClassLevelBonuses();
    }

    // Gets X position
    public double getPositionX() { return xtemp; }

    // Gets defense value
    public int getDefence() { return defence; }

    // Gets strength value
    public int getStrength() { return strength; }

    // Gets critical chance
    public double getCritChance() { return CC; }

    // Gets critical damage
    public double getCritDamage() { return critDMG; }

    // Gets regen rate
    public double getRegenRate() { return regen; }

    // Gets intelligence value
    public int getIntelligence() { return intelligence; }

    // Gets ability damage bonus
    public double getAbilityDamageBonus() { return abilityDMG; }
}