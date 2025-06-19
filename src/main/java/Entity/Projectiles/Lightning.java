package Entity.Projectiles;

import Entity.MapObject;
import Entity.Player;
import TileMap.TileMap;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

// Manages Lightning projectile behavior
public class Lightning extends MapObject {
    private boolean hit; // Hit state
    private boolean remove; // Removal flag
    private BufferedImage sprite; // Projectile sprite
    private static final double SPEED = 1.5; // Movement speed (pixels/tick)
    private int damage; // Damage value
    private long flightTimer; // Flight duration timer
    private static final long FLIGHT_DURATION_NANO = 3_000_000_000L; // Flight duration (ns)
    private boolean warningActive; // Warning state
    private long warningTimer; // Warning timer
    private static final long WARNING_DURATION_NANO = 1_000_000_000L; // Warning duration (ns)
    private double warningX; // Warning position X
    private double warningY; // Warning position Y

    // Initializes Lightning
    public Lightning(TileMap tm, boolean right, int damage) {
        super(tm); // Defaults to empty tile map
        this.facingRight = right;
        this.damage = damage;
        moveSpeed = SPEED;
        dy = SPEED;
        width = 9;
        height = 21;
        cwidth = 7;
        cheight = 16;
        flightTimer = System.nanoTime();
        warningActive = true;
        warningTimer = System.nanoTime();
        loadSprite();
    }

    // Sets warning position
    public void setWarning(double x, double y) {
        this.warningX = x;
        this.warningY = y;
    }

    // Checks if warning is active
    public boolean isWarningActive() {
        return warningActive;
    }

    // Returns warning timer
    public long getWarningTimer() {
        return warningTimer;
    }

    // Returns warning X position
    public double getWarningX() {
        return warningX;
    }

    // Returns warning Y position
    public double getWarningY() {
        return warningY;
    }

    // Loads lightning sprite
    private void loadSprite() {
        try {
            sprite = ImageIO.read(getClass().getResourceAsStream("/Sprites/Enemies/Projectiles/lightning.gif"));
            if (sprite == null) {
                throw new IOException("Invalid sprite");
            }
        } catch (IOException e) {
            // Creates placeholder sprite
            sprite = new BufferedImage(9, 21, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = sprite.createGraphics();
            g.setColor(Color.CYAN);
            g.fillRect(0, 0, 9, 21);
            g.dispose();
        }
    }

    // Marks projectile as hit
    public void setHit() {
        if (hit) return;
        hit = true;
        dy = 0;
        remove = true;
    }

    // Checks if projectile should be removed
    public boolean shouldRemove() {
        return remove;
    }

    // Updates projectile state
    public void update() {
        if (tileMap == null) return;
        if (warningActive) {
            // Ends warning after duration
            if (System.nanoTime() - warningTimer > WARNING_DURATION_NANO) {
                warningActive = false;
                flightTimer = System.nanoTime();
            }
        } else if (!hit) {
            // Removes after duration
            if (System.nanoTime() - flightTimer > FLIGHT_DURATION_NANO) {
                remove = true;
            }
            // Removes off screen
            if (y > tileMap.getHeight() + height * 2) {
                remove = true;
            }
            checkTileMapCollision();
            setPosition(xtemp, ytemp);
            // Marks hit on collision
            if (dy == 0) {
                setHit();
            }
        }
    }

    // Draws projectile
    public void draw(Graphics2D g) {
        if (g == null || tileMap == null) return;
        setMapPosition();
        if (warningActive) {
            // Draws warning indicator
            g.setColor(new Color(255, 0, 0, 150));
            long elapsed = (System.nanoTime() - warningTimer) / 1_000_000;
            if (elapsed % 500 < 250) {
                int warnX = (int)(warningX + xmap - 10);
                int warnY = (int)(warningY + ymap - 10);
                g.fillRect(warnX, warnY, 20, 20);
            }
        } else if (sprite != null) {
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

    // Returns X map offset
    public double getXmap() {
        return xmap;
    }

    // Returns Y map offset
    public double getYmap() {
        return ymap;
    }
}