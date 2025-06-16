package Entity.Enemies;

import Entity.Animation;
import Entity.Enemy;
import Entity.Player;
import Entity.Lightning;
import Entity.Fire;
import TileMap.TileMap;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

public class WizardBoss extends Enemy {
    private Player player;
    private ArrayList<Lightning> lightningStrikes;
    private ArrayList<Fire> fireWaves;
    private static final int IDLE = 0;
    private static final int WALKING = 1;
    private static final int LIGHTNING = 2;
    private static final int FIRE = 3;
    private long lastLightningTime;
    private long lastFireTime;
    private long lastMoveTime;
    private static final long LIGHTNING_COOLDOWN = 5000; // 5 seconds
    private static final long FIRE_COOLDOWN = 3000; // 3 seconds
    private static final long MOVE_COOLDOWN = 4000; // 4 seconds
    private static final int ATTACK_RANGE = 300; // Range for attacks
    private static final int CHASE_RANGE = 200;
    private static final int PATROL_RANGE = 100;
    private double startX;
    private boolean movingRight;

    public WizardBoss(TileMap tm, Player player) {
        super(tm);
        this.player = player;
        this.lightningStrikes = new ArrayList<>();
        this.fireWaves = new ArrayList<>();
        moveSpeed = 1.0;
        maxSpeed = 1.5;
        fallSpeed = 0.2;
        maxFallSpeed = 10.0;
        falling = true;
        width = 31;
        height = 30;
        cwidth = 20;
        cheight = 20;
        health = maxHealth = 600;
        damage = 15;
        startX = x;
        movingRight = true;
        loadSprites();
        facingRight = true;
    }

    private void loadSprites() {
        BufferedImage spritesheet = null;
        try {
            spritesheet = ImageIO.read(getClass().getResourceAsStream("/Sprites/Enemies/WizardBoss.gif"));
            if (spritesheet == null) {
                throw new IOException("WizardBoss sprite sheet is null");
            }
            System.out.println("WizardBoss sprite loaded successfully");
        } catch (Exception e) {
            System.out.println("Failed to load WizardBoss sprite: " + e.getMessage());
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
            System.out.println("Failed to extract WizardBoss frames: " + e.getMessage());
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

    private void setAnimation(int anim) {
        if (currentAction != anim) {
            currentAction = anim;
            animation.setFrames(sprites.get(anim));
            animation.setDelay(anim == WALKING ? 100 : (anim == LIGHTNING || anim == FIRE ? 150 : 400));
        }
    }

    private void fireLightning() {
        boolean right = player.getx() > x;
        Lightning lightning = new Lightning(tileMap, right, 150); // 150 damage
        double spawnY = player.gety() - 100; // Above player
        double warnY = player.gety() - 50; // Warning slightly below
        lightning.setWarning(player.getx(), warnY); // Set warning position
        lightning.setPosition(player.getx(), spawnY);
        lightningStrikes.add(lightning);
        lastLightningTime = System.currentTimeMillis();
        setAnimation(LIGHTNING);
        System.out.println("WizardBoss fired lightning at (" + player.getx() + "," + spawnY + "), warning at (" + player.getx() + "," + warnY + "), tileMap.gety=" + tileMap.gety());
    }

    private void fireWave() {
        boolean fireRight = (player.getx() > x);
        Fire wave = new Fire(tileMap, fireRight, damage);
        wave.setPosition(x, y); // Start at boss’s position
        fireWaves.add(wave);
        lastFireTime = System.currentTimeMillis();
        setAnimation(FIRE);
        System.out.println("WizardBoss fired fire wave at (" + x + "," + y + ")");
    }

    @Override
    public void setPosition(double x, double y) {
        super.setPosition(x, y);
        if (startX == 0) startX = x;
    }

    @Override
    public void hit(int damage) {
        if (dead || flinching) return;
        health -= damage;
        if (health <= 0) {
            dead = true;
        }
        flinching = true;
        flinchTimer = System.nanoTime();
    }

    private void getNextPosition() {

        facingRight = player.getx() > x;

        if (System.currentTimeMillis() - lastMoveTime > MOVE_COOLDOWN) {
            double distToPlayer = Math.abs(player.getx() - x);
            if (distToPlayer < CHASE_RANGE) {

                dx = (player.getx() > x) ? moveSpeed : -moveSpeed;
                setAnimation(WALKING);
            } else {

                if (Math.abs(x - startX) > PATROL_RANGE) {
                    movingRight = !movingRight;
                }
                dx = movingRight ? moveSpeed : -moveSpeed;
                setAnimation(WALKING);
            }
            lastMoveTime = System.currentTimeMillis();
        } else {
            dx = 0;
        }

        if (dx != 0) {
            if (dx > maxSpeed) dx = maxSpeed;
            if (dx < -maxSpeed) dx = -maxSpeed;
        }

        if (falling) {
            dy += fallSpeed;
            if (dy > maxFallSpeed) dy = maxFallSpeed;
        } else {
            dy = 0;
        }
    }

    @Override
    public void update() {
        getNextPosition();
        checkTileMapCollision();
        setPosition(xtemp, ytemp);

        if (flinching) {
            long elapsed = (System.nanoTime() - flinchTimer) / 1_000_000;
            if (elapsed > 1000) {
                flinching = false;
            }
        }

        double distToPlayer = Math.abs(player.getx() - x);
        if (distToPlayer < ATTACK_RANGE) {
            if (System.currentTimeMillis() - lastLightningTime > LIGHTNING_COOLDOWN) {
                fireLightning();
            }
            if (System.currentTimeMillis() - lastFireTime > FIRE_COOLDOWN) {
                fireWave();
            }
        }

        for (int i = 0; i < lightningStrikes.size(); i++) {
            lightningStrikes.get(i).update();
            if (lightningStrikes.get(i).shouldRemove()) {
                lightningStrikes.remove(i);
                i--;
            }
        }

        for (int i = 0; i < fireWaves.size(); i++) {
            fireWaves.get(i).update();
            if (fireWaves.get(i).shouldRemove()) {
                fireWaves.remove(i);
                i--;
            }
        }

        if (currentAction == LIGHTNING || currentAction == FIRE) {
            if (animation.hasPlayedOnce()) {
                setAnimation(IDLE);
            }
        } else if (dx != 0) {
            setAnimation(WALKING);
        } else {
            setAnimation(IDLE);
        }

        animation.update();
    }

    @Override
    public void draw(Graphics2D g) {
        setMapPosition();
        super.draw(g);
    }

    public ArrayList<Lightning> getLightningStrikes() { return lightningStrikes; }
    public ArrayList<Fire> getFireWaves() { return fireWaves; }
}