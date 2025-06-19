package Entity.Enemies.Bosses;

import Entity.Animation;
import Entity.Enemy;
import Entity.Player;
import Entity.Projectiles.Lightning;
import Entity.Projectiles.Fire;
import Main.GamePanel;
import TileMap.TileMap;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

// Manages WizardBoss behavior
public class WizardBoss extends Enemy {
    private Player player; // Player reference
    private ArrayList<Lightning> lightningStrikes; // Lightning attack list
    private ArrayList<Fire> fireWaves; // Fire attack list
    // Animation states
    private static final int IDLE = 0;
    private static final int WALKING = 1;
    private static final int LIGHTNING = 2;
    private static final int FIRE = 3;
    private long lastLightningTime; // Last lightning time
    private long lastFireTime; // Last fire time
    private long lastMoveTime; // Last movement time
    private static final long LIGHTNING_COOLDOWN = 5000; // Lightning cooldown (ms)
    private static final long FIRE_COOLDOWN = 3000; // Fire cooldown (ms)
    private static final int ATTACK_RANGE = 300; // Attack range (pixels)
    private static final int CHASE_RANGE = 500; // Chase range (pixels)
    private static final int TOLERANCE = 80; // Movement tolerance (pixels)

    // Initializes WizardBoss
    public WizardBoss(TileMap tm, Player player) {
        super(tm);
        this.player = player;
        this.lightningStrikes = new ArrayList<>();
        this.fireWaves = new ArrayList<>();
        this.name = "Wizard Boss";
        moveSpeed = 1.0;
        maxSpeed = 1.5;
        fallSpeed = 0.2;
        maxFallSpeed = 10.0;
        falling = true;
        width = 31;
        height = 30;
        cwidth = 20;
        cheight = 20;
        health = maxHealth = 4500;
        damage = 30;
        loadSprites();
        facingRight = true;
    }

    // Loads boss sprites
    private void loadSprites() {
        BufferedImage spritesheet;
        try {
            spritesheet = ImageIO.read(getClass().getResourceAsStream("/Sprites/Enemies/Bosses/WizardBoss.gif"));
            if (spritesheet == null || spritesheet.getWidth() < 93 || spritesheet.getHeight() < 120) {
                throw new IOException("Invalid sprite sheet");
            }
        } catch (IOException e) {
            // Creates placeholder sprite
            spritesheet = new BufferedImage(31, 120, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = spritesheet.createGraphics();
            g.setColor(Color.MAGENTA);
            g.fillRect(0, 0, 31, 120);
            g.dispose();
        }

        sprites = new ArrayList<>();
        BufferedImage[] idleFrames = new BufferedImage[1];
        BufferedImage[] walkingFrames = new BufferedImage[3];
        BufferedImage[] lightningFrames = new BufferedImage[3];
        BufferedImage[] fireFrames = new BufferedImage[3];

        try {
            idleFrames[0] = spritesheet.getSubimage(0, 0, 31, 30);
            for (int i = 0; i < 3; i++) {
                walkingFrames[i] = spritesheet.getSubimage(i * 31, 30, 31, 30);
                lightningFrames[i] = spritesheet.getSubimage(i * 31, 60, 31, 30);
                fireFrames[i] = spritesheet.getSubimage(i * 31, 90, 31, 30);
            }
        } catch (Exception e) {
            // Creates placeholder frames
            BufferedImage placeholder = new BufferedImage(31, 30, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = placeholder.createGraphics();
            g.setColor(Color.MAGENTA);
            g.fillRect(0, 0, 31, 30);
            g.dispose();
            idleFrames[0] = placeholder;
            for (int i = 0; i < 3; i++) {
                walkingFrames[i] = lightningFrames[i] = fireFrames[i] = placeholder;
            }
        }

        sprites.add(idleFrames);
        sprites.add(walkingFrames);
        sprites.add(lightningFrames);
        sprites.add(fireFrames);

        animation = new Animation();
        setAnimation(IDLE);
    }

    // Sets animation state
    private void setAnimation(int anim) {
        if (currentAction != anim && animation != null) {
            currentAction = anim;
            animation.setFrames(sprites.get(Math.min(anim, sprites.size() - 1)));
            animation.setDelay(anim == WALKING ? 100 : (anim == LIGHTNING || anim == FIRE ? 150 : 400));
        }
    }

    // Fires lightning at player
    private void fireLightning() {
        if (player == null) return;
        boolean right = player.getx() > x;
        Lightning lightning = new Lightning(tileMap, right, 80);
        double spawnY = player.gety() - 100;
        double warnY = player.gety() - 50;
        lightning.setWarning(player.getx(), warnY);
        lightning.setPosition(player.getx(), spawnY);
        lightningStrikes.add(lightning);
        lastLightningTime = System.currentTimeMillis();
        setAnimation(LIGHTNING);
    }

    // Fires fire wave
    private void fireWave() {
        if (player == null) return;
        boolean fireRight = player.getx() > x;
        Fire wave = new Fire(tileMap, fireRight, damage);
        wave.setPosition(x, y);
        fireWaves.add(wave);
        lastFireTime = System.currentTimeMillis();
        setAnimation(FIRE);
    }

    // Sets position
    @Override
    public void setPosition(double x, double y) {
        super.setPosition(x, y);
    }

    // Handles damage taken
    @Override
    public void hit(int damage) {
        if (dead || flinching) return;
        health = Math.max(0, health - Math.max(0, damage));
        if (health <= 0) dead = true;
        flinching = true;
        flinchTimer = System.nanoTime();
    }

    // Calculates next position
    private void getNextPosition() {
        if (player == null) return;
        facingRight = player.getx() > x;
        double distToPlayer = Math.abs(player.getx() - x);
        if (distToPlayer < CHASE_RANGE && distToPlayer > TOLERANCE) {
            // Chases player
            dx = (player.getx() > x) ? moveSpeed : -moveSpeed;
            setAnimation(WALKING);
        } else {
            dx = 0;
            setAnimation(IDLE);
        }
        dx = Math.max(-maxSpeed, Math.min(dx, maxSpeed));
        if (falling) {
            dy += fallSpeed;
            dy = Math.min(dy, maxFallSpeed);
        } else {
            dy = 0;
        }
    }

    // Updates boss state
    @Override
    public void update() {
        if (player == null || tileMap == null) return;
        getNextPosition();
        checkTileMapCollision();
        setPosition(xtemp, ytemp);

        if (flinching) {
            long elapsed = (System.nanoTime() - flinchTimer) / 1_000_000;
            if (elapsed > 1000) flinching = false;
        }

        // Triggers attacks
        double distToPlayer = Math.abs(player.getx() - x);
        if (distToPlayer < ATTACK_RANGE) {
            if (System.currentTimeMillis() - lastLightningTime > LIGHTNING_COOLDOWN) fireLightning();
            if (System.currentTimeMillis() - lastFireTime > FIRE_COOLDOWN) fireWave();
        }

        // Updates projectiles
        for (int i = 0; i < lightningStrikes.size(); i++) {
            Lightning lightning = lightningStrikes.get(i);
            lightning.update();
            if (lightning.shouldRemove()) lightningStrikes.remove(i--);
        }
        for (int i = 0; i < fireWaves.size(); i++) {
            Fire fire = fireWaves.get(i);
            fire.update();
            if (fire.shouldRemove()) fireWaves.remove(i--);
        }

        // Updates animation
        if ((currentAction == LIGHTNING || currentAction == FIRE) && animation != null && animation.hasPlayedOnce()) {
            setAnimation(IDLE);
        } else if (dx != 0 && currentAction != LIGHTNING && currentAction != FIRE) {
            setAnimation(WALKING);
        } else if (currentAction != LIGHTNING && currentAction != FIRE) {
            setAnimation(IDLE);
        }

        if (animation != null) animation.update();
    }

    // Draws boss
    @Override
    public void draw(Graphics2D g) {
        if (g == null || tileMap == null) return;
        setMapPosition();
        super.draw(g);
    }

    // Identifies as boss
    @Override
    public boolean isBoss() {
        return true;
    }

    // Returns lightning strikes
    public ArrayList<Lightning> getLightningStrikes() {
        return lightningStrikes;
    }

    // Returns fire waves
    public ArrayList<Fire> getFireWaves() {
        return fireWaves;
    }
}