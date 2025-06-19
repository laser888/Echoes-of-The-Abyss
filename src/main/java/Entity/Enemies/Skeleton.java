package Entity.Enemies;

import Data.GameData;
import Entity.Animation;
import Entity.Enemy;
import Entity.Player;
import Entity.Projectiles.Arrow;
import Main.GamePanel;
import TileMap.TileMap;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

// Manages Skeleton enemy behavior
public class Skeleton extends Enemy {
    private Player player; // Player reference
    private ArrayList<Arrow> arrows; // Arrow projectile list
    private final int[] numFrames = {1, 9}; // Frames per animation
    private static final int IDLE = 0; // Idle animation
    private static final int WALKING = 1; // Walking animation
    private long lastFireTimeNano; // Last arrow fire time
    private static final long FIRE_COOLDOWN_NANO = 3_000_000_000L; // Arrow cooldown (ns)
    private static final double ATTACK_RANGE_PIXELS = 125.0; // Attack range (pixels)
    private static final double DETECTION_RANGE_PIXELS = 300.0; // Detection range (pixels)
    private int damage; // Damage per arrow
    private boolean patrolling; // Patrolling state

    // Initializes Skeleton
    public Skeleton(TileMap tm, Player player) {
        super(tm);
        this.player = player;
        this.arrows = new ArrayList<>();
        moveSpeed = 0.3;
        maxSpeed = 0.3;
        fallSpeed = 0.2;
        maxFallSpeed = 10.0;
        width = 30;
        height = 30;
        cwidth = 20;
        cheight = 20;
        health = maxHealth = 200;
        damage = 30;
        lastFireTimeNano = System.nanoTime() - FIRE_COOLDOWN_NANO;
        patrolling = true;
        right = true;
        loadSprites();
    }

    // Loads skeleton sprites
    private void loadSprites() {
        try {
            BufferedImage spritesheet = ImageIO.read(getClass().getResourceAsStream("/Sprites/Enemies/skeleton.gif"));
            if (spritesheet == null || spritesheet.getWidth() < width * 9 || spritesheet.getHeight() < height * 2) {
                throw new IOException("Invalid sprite sheet");
            }
            sprites = new ArrayList<>();
            for (int i = 0; i < numFrames.length; i++) {
                BufferedImage[] bi = new BufferedImage[numFrames[i]];
                for (int j = 0; j < numFrames[i]; j++) {
                    bi[j] = spritesheet.getSubimage(j * width, i * height, width, height);
                }
                sprites.add(bi);
            }
        } catch (IOException e) {
            e.printStackTrace(); // Restored debug output
            // Creates placeholder sprites
            sprites = new ArrayList<>();
            BufferedImage placeholder = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = placeholder.createGraphics();
            g.setColor(Color.MAGENTA);
            g.fillRect(0, 0, width, height);
            g.dispose();
            BufferedImage[] idle = {placeholder};
            BufferedImage[] walking = new BufferedImage[9];
            for (int i = 0; i < 9; i++) walking[i] = placeholder;
            sprites.add(idle);
            sprites.add(walking);
        }
        animation = new Animation();
        currentAction = IDLE;
        animation.setFrames(sprites.get(IDLE));
        animation.setDelay(400);
    }

    // Sets animation state
    private void setAnimation(int anim) {
        if (currentAction != anim && animation != null) {
            currentAction = anim;
            animation.setFrames(sprites.get(Math.min(anim, sprites.size() - 1)));
            animation.setDelay(anim == WALKING ? 50 : 400);
        }
    }

    // Checks edge of platform
    private boolean isAtEdge() {
        if (tileMap == null) return true;
        double nextX = x + (right ? cwidth / 2 + 1 : -cwidth / 2 - 1);
        double nextY = y + cheight / 2 + 1;
        int tileX = (int)(nextX / tileMap.getTileSize());
        int tileY = (int)(nextY / tileMap.getTileSize());
        return tileMap.getType(tileY, tileX) == 0;
    }

    // Fires arrow at player
    public void fireArrow() {
        if (player == null) return;
        facingRight = player.getx() < x ? false : true;
        Arrow arrow = new Arrow(tileMap, facingRight, true);
        arrow.setPosition(x, y);
        arrows.add(arrow);
        lastFireTimeNano = System.nanoTime();
    }

    // Calculates next position
    private void getNextPosition() {
        if (left && !isAtEdge()) {
            dx = Math.max(-maxSpeed, dx - moveSpeed);
        } else if (right && !isAtEdge()) {
            dx = Math.min(maxSpeed, dx + moveSpeed);
        } else {
            dx = 0;
        }
        if (falling) {
            dy = Math.min(maxFallSpeed, dy + fallSpeed);
            if (dy > 0) jumping = false;
        }
    }

    // Updates skeleton state
    public void update() {
        if (tileMap == null) return; // Soften check to allow player-null updates
        double playerX = player != null ? player.getPositionX() : x;
        double skeletonX = getx();
        double distanceToPlayer = Math.abs(playerX - skeletonX);

        if (distanceToPlayer > DETECTION_RANGE_PIXELS || player == null) {
            // Patrols platform
            patrolling = true;
            if (isAtEdge() || (right && dx == 0) || (left && dx == 0)) {
                right = !right;
                left = !left;
                facingRight = right;
                dx = right ? maxSpeed : -maxSpeed;
            }
            setAnimation(WALKING);
        } else if (distanceToPlayer <= ATTACK_RANGE_PIXELS) {
            // Stops to attack
            patrolling = false;
            right = false;
            left = false;
            dx = 0;
            setAnimation(IDLE);
        } else {
            // Chases player
            patrolling = false;
            right = playerX > skeletonX;
            left = !right;
            facingRight = right;
            dx = right ? maxSpeed : -maxSpeed;
            setAnimation(WALKING);
        }

        getNextPosition();
        checkTileMapCollision();
        setPosition(xtemp, ytemp);

        if (flinching) {
            long elapsed = (System.nanoTime() - flinchTimer) / 1_000_000;
            if (elapsed > 400) flinching = false;
            if (animation != null) animation.update();
            return;
        }

        // Triggers arrow attack
        if (player != null && System.nanoTime() - lastFireTimeNano >= FIRE_COOLDOWN_NANO && distanceToPlayer <= ATTACK_RANGE_PIXELS) {
            fireArrow();
        }

        // Updates arrows
        for (int i = 0; i < arrows.size(); i++) {
            Arrow arrow = arrows.get(i);
            arrow.update();
            if (arrow.shouldRemove()) arrows.remove(i--);
        }

        if (animation != null) animation.update();
    }

    // Draws skeleton and arrows
    public void draw(Graphics2D g) {
        if (g == null || tileMap == null) return;
        setMapPosition();
        for (Arrow arrow : arrows) {
            if (arrow != null) arrow.draw(g);
        }
        super.draw(g);
    }

    // Returns arrow projectiles
    public ArrayList<Arrow> getArrows() {
        return arrows;
    }

    // Returns damage value
    public int getDamage() {
        return damage;
    }
}