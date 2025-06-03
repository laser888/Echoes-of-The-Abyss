package Entity.Enemies;

import Entity.Animation;
import Entity.Arrow;
import Entity.Enemy;
import Entity.Player;
import TileMap.TileMap;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Skeleton extends Enemy {

    private Player player;
    private ArrayList<Arrow> arrows;

    private BufferedImage[] sprites;

    private long lastFireTimeNano;
    private static final long FIRE_COOLDOWN_NANO = 3 * 1000 * 1000000L; // 3 seconds
    private static final double ATTACK_RANGE_PIXELS = 150.0;

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
        damage = 20;

        lastFireTimeNano = System.nanoTime() - FIRE_COOLDOWN_NANO;

        try {
            BufferedImage spritesheet = ImageIO.read(getClass().getResourceAsStream("/Sprites/Enemies/skeleton.gif"));
            sprites = new BufferedImage[9];

            for (int i = 0; i < sprites.length; i++) {
                sprites[i] = spritesheet.getSubimage(i * width, 0, width, height);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        animation = new Animation();
        animation.setFrames(sprites);
        animation.setDelay(150);
    }

    private void getNextPosition() {

        // movement
        if(left) {
            dx -= moveSpeed;
            if(dx < -maxSpeed) {
                dx = -maxSpeed;
            }
        } else if(right) {
            dx += moveSpeed;
            if(dx > maxSpeed) {
                dx = maxSpeed;
            }
        }

        if(falling) {
            dy += fallSpeed;
            if(dy > 0) jumping = false;
        }
    }

    public void fireArrow() {

        if (player.getx() < x) {
            facingRight = false;
        } else {
            facingRight = true;
        }

        Arrow arrow = new Arrow(tileMap, facingRight, true);
        arrow.setPosition(x, y);
        arrows.add(arrow);
        lastFireTimeNano = System.nanoTime();
    }

    public void update() {

            double playerX = player.getPositionX();
            double skeletonX = getx();

            double tolerance = 1.5;

            if (Math.abs(playerX - skeletonX) <= tolerance) {
                right = false;
                left = false;
                dx = 0;

            } else if (playerX > skeletonX) {
                right = true;
                left = false;
                facingRight = true;
                dx = maxSpeed;

            } else {
                right = false;
                left = true;
                facingRight = false;
                dx = -maxSpeed;
            }

        getNextPosition();
        checkTileMapCollision();
        setPosition(xtemp, ytemp);

        // Check flinching
        if (flinching) {
            long elapsed = (System.nanoTime() - flinchTimer) / 1000000;
            if (elapsed > 400) {
                flinching = false;
            }
            animation.update();
            return;
        }

        long currentTimeNano = System.nanoTime();
        if (currentTimeNano - lastFireTimeNano >= FIRE_COOLDOWN_NANO) {

            if (Math.abs(playerX - skeletonX) <= ATTACK_RANGE_PIXELS) {
                fireArrow();
            }
        }

        // Update arrows
        for (int i = 0; i < arrows.size(); i++) {
            Arrow arrow = arrows.get(i);
            arrow.update();
            if (arrow.shouldRemove()) {
                arrows.remove(i);
                i--;
            }
        }

        animation.update();
    }

    public ArrayList<Arrow> getArrows() {
        return arrows;
    }

    @Override
    public void draw(Graphics2D g) {
        setMapPosition();

        for (Arrow arrow : arrows) {
            arrow.draw(g);
        }

        super.draw(g);
    }
}