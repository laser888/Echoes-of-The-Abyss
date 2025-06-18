package Entity.Enemies;

import Entity.Animation;
import Entity.Enemy;
import TileMap.TileMap;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Zombie extends Enemy {

    private BufferedImage[] sprites;

    public Zombie (TileMap tm) {

        super(tm);

        moveSpeed = 0.2;
        maxSpeed = 0.2;
        fallSpeed = 0.2;
        maxFallSpeed = 10.0;

        width = 30;
        height = 30;
        cwidth = 20;
        cheight = 20;

        health = maxHealth = 100;
        damage = 20;

        // load sprites
        try {

            BufferedImage spritesheet = ImageIO.read(getClass().getResourceAsStream("/Sprites/Enemies/zombie.gif"));

            sprites = new BufferedImage[4];
            for(int i = 0; i < sprites.length; i++) {
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

    private boolean isAtEdge() {
        double nextX = x + (right ? cwidth / 2 + 1 : -cwidth / 2 - 1);
        double nextY = y + cheight / 2 + 1;

        int tileX = (int) (nextX / tileMap.getTileSize());
        int tileY = (int) (nextY / tileMap.getTileSize());

        return tileMap.getType(tileY,tileX) == 0;
    }

    public void update() {

        // update position
        getNextPosition();
        checkTileMapCollision();
        setPosition(xtemp, ytemp);

        // check flinching
        if(flinching) {
            long elapsed = (System.nanoTime() - flinchTimer) / 1000000;
            if(elapsed > 400) {
                flinching = false;
            }
        }

        // if it hits a wall, go other direction
        if (isAtEdge() || (right && dx == 0) || (left && dx == 0)) {
            right = !right;
            left = !left;
            facingRight = right;
            dx = right ? maxSpeed : -maxSpeed;

        }

        // update animation
        animation.update();

    }

    public void draw(Graphics2D g) {

        //if(notOnScreen()) return;

        setMapPosition();

        super.draw(g);


    }



}
