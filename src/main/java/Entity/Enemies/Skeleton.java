package Entity.Enemies;

import Entity.Animation;
import Entity.Projectiles.Arrow;
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

    private ArrayList<BufferedImage[]> sprites;
    private final int[] numFrames = {1, 9};
    private static final int IDLE = 0;
    private static final int WALKING = 1;

    private long lastFireTimeNano;
    private static final long FIRE_COOLDOWN_NANO = 3 * 1000 * 1000000L; // 3 seconds
    private static final double ATTACK_RANGE_PIXELS = 125.0;
    private static final double DETECTION_RANGE_PIXELS = 300.0;
    private int damage;

    private boolean patrolling;

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

        try {
            BufferedImage spritesheet = ImageIO.read(getClass().getResourceAsStream("/Sprites/Enemies/skeleton.gif"));

            sprites = new ArrayList<>();

            for (int i = 0; i < numFrames.length; i++) {
                BufferedImage[] bi = new BufferedImage[numFrames[i]];
                for (int j = 0; j < numFrames[i]; j++) {
                    bi[j] = spritesheet.getSubimage(j * width, i * height, width, height);
                }
                sprites.add(bi);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        animation = new Animation();
        currentAction = IDLE;
        animation.setFrames(sprites.get(IDLE));
        animation.setDelay(400);
    }

    private void getNextPosition() {

        // Movement
        if (left && !isAtEdge()) {
            dx -= moveSpeed;
            if (dx < -maxSpeed) {
                dx = -maxSpeed;
            }
        } else if (right && !isAtEdge()) {
            dx += moveSpeed;
            if (dx > maxSpeed) {
                dx = maxSpeed;
            }
        } else {
            dx = 0;
        }

        if (falling) {
            dy += fallSpeed;
            if (dy > 0) jumping = false;
        }
    }

    private boolean isAtEdge() {
        double nextX = x + (right ? cwidth / 2 + 1 : -cwidth / 2 - 1);
        double nextY = y + cheight / 2 + 1;

        int tileX = (int) (nextX / tileMap.getTileSize());
        int tileY = (int) (nextY / tileMap.getTileSize());

        return tileMap.getType(tileY,tileX) == 0;
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
        double distanceToPlayer = Math.abs(playerX - skeletonX);

        if (distanceToPlayer > DETECTION_RANGE_PIXELS) {

            patrolling = true;
            if (isAtEdge() || (right && dx == 0) || (left && dx == 0)) {
                right = !right;
                left = !left;
                facingRight = right;
                dx = right ? maxSpeed : -maxSpeed;

            }

            if (currentAction != WALKING) {
                currentAction = WALKING;
                animation.setFrames(sprites.get(WALKING));
                animation.setDelay(50);
            }
        } else if (distanceToPlayer <= ATTACK_RANGE_PIXELS) {

            patrolling = false;
            right = false;
            left = false;
            dx = 0;

            if (currentAction != IDLE) {
                currentAction = IDLE;
                animation.setFrames(sprites.get(IDLE));
                animation.setDelay(400);
            }
        } else {
            patrolling = false;

            if (playerX < skeletonX) {
                right = false;
                left = true;
                facingRight = false;
                dx = -maxSpeed;

            } else {
                right = true;
                left = false;
                facingRight = true;
                dx = maxSpeed;
            }

            if (currentAction != WALKING) {
                currentAction = WALKING;
                animation.setFrames(sprites.get(WALKING));
                animation.setDelay(50);
            }
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

    public int getDamage() {
        return damage;
    }
}