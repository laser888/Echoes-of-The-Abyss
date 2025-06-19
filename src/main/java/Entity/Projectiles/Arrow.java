package Entity.Projectiles;

import Entity.Animation;
import Entity.MapObject;
import Main.GamePanel;
import TileMap.TileMap;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

// Manages Arrow projectile behavior
public class Arrow extends MapObject {
    private boolean hit; // Hit state
    private boolean remove; // Removal flag
    private BufferedImage sprite; // Arrow sprite
    private long flightTimer; // Flight duration timer
    private boolean falling; // Falling state
    private static final long FLIGHT_DURATION_NANO = 1_000_000_000L; // Flight duration (ns)
    private static final double FALL_SPEED = 0.15; // Fall speed (pixels/tick)
    private static final double MAX_FALL_SPEED = 4.0; // Max fall speed (pixels/tick)
    private boolean isEnemyArrow; // Enemy arrow flag

    // Initializes Arrow
    public Arrow(TileMap tm, boolean right, boolean isEnemyArrow) {
        super(tm);
        this.facingRight = right;
        this.isEnemyArrow = isEnemyArrow;
        moveSpeed = 3.8;
        dx = right ? moveSpeed : -moveSpeed;
        width = 30;
        height = 15;
        cwidth = 10;
        cheight = 8;
        falling = false;
        flightTimer = System.nanoTime();
        loadSprite();
    }

    // Loads arrow sprite
    private void loadSprite() {
        try {
            sprite = ImageIO.read(getClass().getResourceAsStream("/Sprites/Player/Projectiles/arrow.gif"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Marks arrow as hit
    public void setHit() {
        if (hit) return;
        hit = true;
        dx = 0;
        remove = true;
    }

    // Checks if arrow should be removed
    public boolean shouldRemove() {
        return remove;
    }

    // Checks if arrow is from enemy
    public boolean isEnemyArrow() {
        return isEnemyArrow;
    }

    // Updates arrow state
    public void update() {
        if (tileMap == null) return;
        // Starts falling after duration
        if (!hit && (System.nanoTime() - flightTimer) > FLIGHT_DURATION_NANO) {
            falling = true;
        }
        if (falling && !hit) {
            dy = Math.min(MAX_FALL_SPEED, dy + FALL_SPEED);
        }
        checkTileMapCollision();
        setPosition(xtemp, ytemp);
        // Marks hit on collision
        if (dx == 0 && !hit) {
            setHit();
        }
    }

    // Draws arrow
    public void draw(Graphics2D g) {
        if (g == null || tileMap == null || sprite == null) return;
        setMapPosition();
        if (facingRight) {
            g.drawImage(sprite, (int) (x + xmap - sprite.getWidth() / 2), (int) (y + ymap - sprite.getHeight() / 2), null);
        } else {
            g.drawImage(sprite, (int) (x + xmap - sprite.getWidth() / 2) + sprite.getWidth(), (int) (y + ymap - sprite.getHeight() / 2), -sprite.getWidth(), sprite.getHeight(), null);
        }
    }
}