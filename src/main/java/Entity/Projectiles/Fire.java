package Entity.Projectiles;

import Entity.MapObject;
import Entity.Player;
import TileMap.TileMap;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Fire extends MapObject {
    private boolean hit;
    private boolean remove;
    private BufferedImage sprite;
    private static final double SPEED = 3.0;
    private int damage;
    private long flightTimer;
    private static final long FLIGHT_DURATION_NANO = 2_000_000_000L; // 2 seconds

    public Fire(TileMap tm, boolean right, int damage) {
        super(tm);
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

    private void loadSprite() {
        String[] possiblePaths = {"/Sprites/Enemies/Projectiles/fire.gif"};
        for (String path : possiblePaths) {
            try {
                System.out.println("Attempting to load Fire sprite from: " + path);
                sprite = ImageIO.read(getClass().getResourceAsStream(path));
                if (sprite != null) {
                    width = sprite.getWidth();
                    height = sprite.getHeight();
                    cwidth = width * 3 / 4;
                    cheight = height * 3 / 4;
                    System.out.println("Fire sprite loaded from " + path + ": " + width + "x" + height);
                    return;
                }
            } catch (Exception e) {
                System.out.println("Failed to load Fire sprite from " + path + ": " + e.getMessage());
            }
        }

        System.out.println("All paths failed; using placeholder sprite");
        sprite = new BufferedImage(10, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = sprite.createGraphics();
        g.setColor(Color.RED.brighter());
        g.fillRect(0, 0, 10, 16);
        g.dispose();
    }

    public void setHit() {
        if (hit) return;
        hit = true;
        dx = 0;
        remove = true;
    }

    public boolean shouldRemove() {
        return remove;
    }

    public void update() {
        if (!hit) {
            long elapsedNanos = System.nanoTime() - flightTimer;
            if (elapsedNanos > FLIGHT_DURATION_NANO) {
                remove = true;
                System.out.println("Fire timed out at x=" + x);
            }

            checkTileMapCollision();
            setPosition(xtemp, ytemp);
            if (dx == 0) {
                setHit();
            }
            if (x < -width || x > tileMap.getWidth() + width) {
                remove = true;
                System.out.println("Fire removed at x=" + x);
            }
        }
    }

    public void draw(Graphics2D g) {
        setMapPosition();
        int drawX = (int)(x + xmap - width); // Center sprite
        int drawY = (int)(y + ymap - height);
        int scaledWidth = width * 2; // Scale 2x
        int scaledHeight = height * 2;
        if (facingRight) {
            g.drawImage(sprite, drawX, drawY, scaledWidth, scaledHeight, null);
        } else {
            g.drawImage(sprite, drawX + scaledWidth, drawY, -scaledWidth, scaledHeight, null);
        }
    }

    public boolean intersects(Player player) {
        Rectangle r1 = new Rectangle((int)x - cwidth / 2, (int)y - cheight / 2, cwidth, cheight);
        Rectangle r2 = new Rectangle((int)player.getx() - player.getWidth() / 2,
                (int)player.gety() - player.getHeight() / 2,
                player.getWidth(), player.getHeight());
        boolean intersects = r1.intersects(r2);
        if (intersects) {
            System.out.println("Fire intersects player at (" + x + "," + y + ") with hitbox (" + cwidth + "," + cheight + ")");
        }
        return intersects;
    }

    public int getDamage() {
        return damage;
    }
}