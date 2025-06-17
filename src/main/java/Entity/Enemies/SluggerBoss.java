package Entity.Enemies;

import Entity.Animation;
import Entity.Enemy;
import TileMap.TileMap;
import Entity.Player;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;

public class SluggerBoss extends Enemy {

    private BufferedImage[] sprites;
    private Player player;

    public SluggerBoss(TileMap tm, Player player) {
        super(tm);
        this.player = player;
        moveSpeed = 0.6;
        maxSpeed = 0.6;
        fallSpeed = 0.2;
        maxFallSpeed = 10.0;

        this.name = "Slugger Boss";

        width = 30;
        height = 30;
        cwidth = 20;
        cheight = 20;

        health = maxHealth = 1500;
        damage = 45;

        // load sprites
        try {
            BufferedImage spritesheet = ImageIO.read(getClass().getResourceAsStream("/Sprites/Enemies/sluggerBoss.gif"));
            sprites = new BufferedImage[3];
            for (int i = 0; i < sprites.length; i++) {
                sprites[i] = spritesheet.getSubimage(i * width, 0, width, height);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        animation = new Animation();
        animation.setFrames(sprites);
        animation.setDelay(300);

        right = true;
        facingRight = true;
    }

    @Override
    public boolean isBoss() {
        return true; // Mark SluggerBoss as a boss
    }

    private void getNextPosition() {
        if (left) {
            dx -= moveSpeed;
            if (dx < -maxSpeed) {
                dx = -maxSpeed;
            }
        } else if (right) {
            dx += moveSpeed;
            if (dx > maxSpeed) {
                dx = maxSpeed;
            }
        }

        if (falling) {
            dy += fallSpeed;
            if (dy > 0) jumping = false;
        }
    }

    public void update() {
        getNextPosition();
        checkTileMapCollision();
        setPosition(xtemp, ytemp);

        if (flinching) {
            long elapsed = (System.nanoTime() - flinchTimer) / 1000000;
            if (elapsed > 400) {
                flinching = false;
            }
        }

        if (player != null) {
            double playerX = player.getPositionX();
            double bossX = getx();
            double tolerance = 1.5;

            if (Math.abs(playerX - bossX) <= tolerance) {
                right = false;
                left = false;
                dx = 0;
            } else if (playerX > bossX) {
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
        } else {
            if (this.right && this.dx == 0) {
                this.right = false;
                this.left = true;
                this.facingRight = false;
                this.dx = -this.maxSpeed;
            } else if (this.left && this.dx == 0) {
                this.left = false;
                this.right = true;
                this.facingRight = true;
                this.dx = this.maxSpeed;
            }
        }

        animation.update();
    }

    public void draw(Graphics2D g) {
        setMapPosition();
        super.draw(g);
    }
}