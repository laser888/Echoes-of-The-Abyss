package Entity.Projectiles;

import Entity.MapObject;
import Entity.Player;
import TileMap.TileMap;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

// Manages Fire projectile behavior
public class Fire extends MapObject {
    private boolean hit; // Hit state
    private boolean remove; // Removal flag
    private BufferedImage sprite; // Projectile sprite
    private static final double SPEED = 3.0; // Movement speed (pixels/tick)
    private int damage; // Damage value
    private long flightTimer; // Flight duration timer
    private static final long FLIGHT_DURATION_NANO = 2_000_000_000L; // Flight duration (ns)

    // Initializes Fire
    public Fire(TileMap tm, boolean right, int damage) {
        super(tm); // Defaults to empty tile map
        this.facingRight = right;
        this.damage = damage;
        moveSpeed = SPEED;
        dx = right ? SPEED : -SPEED;
        width = 10;
        height = 16;
        cwidth = 8;
        cheight = 12;
        flightTimer = System.nanoTime();
        loadSprite();
    }

    // Loads fire sprite
    private void loadSprite() {
        try {
            sprite = ImageIO.read(getClass().getResourceAsStream("/Sprites/Enemies/Projectiles/fire.gif"));
            if (sprite == null) {
                throw new IOException("Invalid sprite");
            }
        } catch (IOException e) {
            // Creates placeholder sprite
            sprite = new BufferedImage(10, 16, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = sprite.createGraphics();
            g.setColor(Color.RED.brighter());
            g.fillRect(0, 0, 10, 16);
            g.dispose();
        }
    }

    // Marks projectile as hit
    public void setHit() {
        if (hit) return;
        hit = true;
        dx = 0;
        remove = true;
    }

    // Checks if projectile should be removed
    public boolean shouldRemove() {
        return remove;
    }

    // Updates projectile state
    public void update() {
        if (tileMap == null || hit) return;
        // Removes after duration
        if (System.nanoTime() - flightTimer > FLIGHT_DURATION_NANO) {
            remove = true;
        }
        // Removes off screen
        if (x < -width || x > tileMap.getWidth() + width) {
            remove = true;
        }
        checkTileMapCollision();
        setPosition(xtemp, ytemp);
        // Marks hit on collision
        if (dx == 0) {
            setHit();
        }
    }

    // Draws projectile
    public void draw(Graphics2D g) {
        if (g == null || tileMap == null || sprite == null) return;
        setMapPosition();
        int drawX = (int)(x + xmap - width);
        int drawY = (int)(y + ymap - height);
        int scaledWidth = width * 2;
        int scaledHeight = height * 2;
        if (facingRight) {
            g.drawImage(sprite, drawX, drawY, scaledWidth, scaledHeight, null);
        } else {
            g.drawImage(sprite, drawX + scaledWidth, drawY, -scaledWidth, scaledHeight, null);
        }
    }

    // Checks collision with player
    public boolean intersects(Player player) {
        if (player == null) return false;
        Rectangle r1 = new Rectangle((int)x - cwidth / 2, (int)y - cheight / 2, cwidth, cheight);
        Rectangle r2 = new Rectangle((int)player.getx() - player.getWidth() / 2,
                (int)player.gety() - player.getHeight() / 2,
                player.getWidth(), player.getHeight());
        return r1.intersects(r2);
    }

    // Returns damage value
    public int getDamage() {
        return damage;
    }
}