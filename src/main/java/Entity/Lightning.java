package Entity;

import TileMap.TileMap;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Lightning extends MapObject {
    private boolean hit;
    private boolean remove;
    private BufferedImage sprite;
    private static final double SPEED = 1.5; // Slower for visibility
    private int damage;
    private long flightTimer;
    private static final long FLIGHT_DURATION_NANO = 3_000_000_000L; // 3 seconds
    private boolean warningActive;
    private long warningTimer;
    private static final long WARNING_DURATION_NANO = 1_000_000_000L; // 1 second
    private double warningX;
    private double warningY;

    public Lightning(TileMap tm, boolean right, int damage) {
        super(tm);
        this.facingRight = right;
        this.damage = damage;
        moveSpeed = SPEED;
        dy = SPEED; // Move downward
        width = 9; // Actual sprite size
        height = 21;
        cwidth = 7;
        cheight = 16;
        flightTimer = System.nanoTime();
        warningActive = true;
        warningTimer = System.nanoTime();
        loadSprite();
    }

    public void setWarning(double x, double y) {
        this.warningX = x;
        this.warningY = y;
    }

    public boolean isWarningActive() {
        return warningActive;
    }

    public long getWarningTimer() {
        return warningTimer;
    }

    public double getWarningX() {
        return warningX;
    }

    public double getWarningY() {
        return warningY;
    }

    private void loadSprite() {
        String[] possiblePaths = {"/Sprites/Enemies/lightning.gif",};
        for (String path : possiblePaths) {
            try {
                System.out.println("Attempting to load Lightning sprite from: " + path);
                sprite = ImageIO.read(getClass().getResourceAsStream(path));
                if (sprite != null) {
                    width = sprite.getWidth();
                    height = sprite.getHeight();
                    cwidth = width * 3 / 4;
                    cheight = height * 3 / 4;
                    System.out.println("Lightning sprite loaded from " + path + ": " + width + "x" + height);
                    return;
                }
            } catch (Exception e) {
                System.out.println("Failed to load Lightning sprite from " + path + ": " + e.getMessage());
            }
        }

        System.out.println("All paths failed; using placeholder sprite");
        sprite = new BufferedImage(9, 21, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = sprite.createGraphics();
        g.setColor(Color.CYAN);
        g.fillRect(0, 0, 9, 21);
        g.dispose();
    }

    public void setHit() {
        if (hit) return;
        hit = true;
        dy = 0;
        remove = true;
    }

    public boolean shouldRemove() {
        return remove;
    }

    public void update() {
        if (warningActive) {
            long elapsedNanos = System.nanoTime() - warningTimer;
            if (elapsedNanos > WARNING_DURATION_NANO) {
                warningActive = false;
                flightTimer = System.nanoTime(); // Reset flight timer after warning
            }
        } else if (!hit) {
            long elapsedNanos = System.nanoTime() - flightTimer;
            if (elapsedNanos > FLIGHT_DURATION_NANO) {
                remove = true;
                System.out.println("Lightning timed out at y=" + y);
            }

            checkTileMapCollision();
            setPosition(xtemp, ytemp);
            if (dy == 0) {
                setHit();
            }
            if (y > tileMap.getHeight() + height * 2) {
                remove = true;
                System.out.println("Lightning removed at y=" + y);
            }
        }
    }

    public void draw(Graphics2D g) {
        setMapPosition();
        if (warningActive) {
            g.setColor(new Color(255, 0, 0, 150)); // Semi transparent red
            long elapsed = (System.nanoTime() - warningTimer) / 1_000_000;
            if (elapsed % 500 < 250) { // Flash every 250ms
                int warnX = (int)(warningX + xmap - 10);
                int warnY = (int)(warningY + ymap - 10);
                g.fillRect(warnX, warnY, 20, 20);
                System.out.println("Drawing lightning warning at screen (" + warnX + "," + warnY + "), xmap=" + xmap + ", ymap=" + ymap);
            }
        } else {
            int drawX = (int)(x + xmap - width); // Center sprite
            int drawY = (int)(y + ymap - height);
            int scaledWidth = width * 2; // Scale 2x
            int scaledHeight = height * 2;
            if (facingRight) {
                g.drawImage(sprite, drawX, drawY, scaledWidth, scaledHeight, null);
            } else {
                g.drawImage(sprite, drawX + scaledWidth, drawY, -scaledWidth, scaledHeight, null);
            }
            System.out.println("Lightning draw: world (" + x + "," + y + "), screen (" + (drawX + scaledWidth/2) + "," + (drawY + scaledHeight/2) + "), xmap=" + xmap + ", ymap=" + ymap);
        }
    }

    public boolean intersects(Player player) {
        Rectangle r1 = new Rectangle((int)x - cwidth / 2, (int)y - cheight / 2, cwidth, cheight);
        Rectangle r2 = new Rectangle((int)player.getx() - player.getWidth() / 2,
                (int)player.gety() - player.getHeight() / 2,
                player.getWidth(), player.getHeight());
        boolean intersects = r1.intersects(r2);
        if (intersects) {
            System.out.println("Lightning intersects player at (" + x + "," + y + ") with hitbox (" + cwidth + "," + cheight + ")");
        }
        return intersects;
    }

    public int getDamage() {
        return damage;
    }

    public double getXmap() {
        return xmap;
    }

    public double getYmap() {
        return ymap;
    }
}