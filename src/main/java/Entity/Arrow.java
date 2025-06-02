package Entity;

import TileMap.TileMap;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Arrow extends MapObject {

    private boolean hit;
    private boolean remove;
    private BufferedImage sprite;
    private long flightTimer;
    private boolean falling;
    private static final long FLIGHT_DURATION_NANO = 1_000_000_000L; // 1 second
    private static final double FALL_SPEED = 0.15;
    private static final double MAX_FALL_SPEED = 4.0;

    private boolean isEnemyArrow;

    public Arrow(TileMap tm, boolean right, boolean isEnemyArrow) {
        super(tm);

        this.facingRight = right;
        this.isEnemyArrow = isEnemyArrow;

        moveSpeed = 3.8;
        if (right) dx = moveSpeed;
        else dx = -moveSpeed;

        width = 30;
        height = 15;
        cwidth = 10;
        cheight = 8;

        falling = false;
        flightTimer = System.nanoTime();

        try {
            sprite = ImageIO.read(getClass().getResourceAsStream("/Sprites/Player/arrow.gif"));
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public boolean isEnemyArrow() {
        return isEnemyArrow;
    }

    public void update() {
        long elapsedNanos = System.nanoTime() - flightTimer;
        if (!hit && elapsedNanos > FLIGHT_DURATION_NANO) {
            falling = true;
        }

        if (falling && !hit) {
            dy += FALL_SPEED;
            if (dy > MAX_FALL_SPEED) {
                dy = MAX_FALL_SPEED;
            }
        }

        checkTileMapCollision();
        setPosition(xtemp, ytemp);

        if (dx == 0 && !hit) {
            setHit();
        }

//        if (!hit && (x < -width || x > tileMap.getWidth() + width || y < -height || y > tileMap.getHeight() + height) ) {
//            remove = true;
//        }
    }

    public void draw(Graphics2D g) {
        setMapPosition();

        if (facingRight) {
            g.drawImage(sprite, (int) (x + xmap - sprite.getWidth() / 2), (int) (y + ymap - sprite.getHeight() / 2), null);
        } else {
            g.drawImage(sprite, (int) (x + xmap - sprite.getWidth() / 2) + sprite.getWidth(), (int) (y + ymap - sprite.getHeight() / 2), -sprite.getWidth(), sprite.getHeight(), null);
        }
    }
}