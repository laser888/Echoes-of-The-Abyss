package Entity.Enemies;

import Entity.Animation;
import Entity.Enemy;
import Main.GamePanel;
import TileMap.TileMap;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

// Manages Slugger enemy behavior
public class Slugger extends Enemy {
    private static final int SPRITE_COUNT = 3; // Number of sprite frames
    BufferedImage[] frames; // Animation frames

    // Initializes Slugger
    public Slugger(TileMap tm) {
        super(tm);
        moveSpeed = 0.3;
        maxSpeed = 0.3;
        fallSpeed = 0.2;
        maxFallSpeed = 10.0;
        width = 30;
        height = 30;
        cwidth = 20;
        cheight = 20;
        health = maxHealth = 40;
        damage = 5;
        loadSprites();
        right = true;
        facingRight = true;
    }

    // Loads slugger sprites
    private void loadSprites() {
        try {
            BufferedImage spritesheet = ImageIO.read(getClass().getResourceAsStream("/Sprites/Enemies/slugger.gif"));
            if (spritesheet == null || spritesheet.getWidth() < width * SPRITE_COUNT || spritesheet.getHeight() < height) {
                throw new IOException("Invalid sprite sheet");
            }
            sprites = new ArrayList<>();
            frames = new BufferedImage[SPRITE_COUNT];
            for (int i = 0; i < SPRITE_COUNT; i++) {
                frames[i] = spritesheet.getSubimage(i * width, 0, width, height);
            }
            sprites.add(frames);
        } catch (IOException e) {
            // Creates placeholder sprites
            sprites = new ArrayList<>();
            BufferedImage placeholder = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = placeholder.createGraphics();
            g.setColor(Color.MAGENTA);
            g.fillRect(0, 0, width, height);
            g.dispose();
            BufferedImage[] frames = new BufferedImage[SPRITE_COUNT];
            for (int i = 0; i < SPRITE_COUNT; i++) frames[i] = placeholder;
            sprites.add(frames);
        }

        animation = new Animation();
        animation.setFrames(frames);
        animation.setDelay(300);

        right = true;
        facingRight = true;
    }

    // Sets animation
    private void setAnimation() {
        if (animation != null && sprites != null && !sprites.isEmpty()) {
            animation.setFrames(sprites.get(0));
            animation.setDelay(300);
        }
    }

    // Checks platform edge
    private boolean isAtEdge() {
        if (tileMap == null) return true;
        double nextX = x + (right ? cwidth / 2 + 1 : -cwidth / 2 - 1);
        double nextY = y + cheight / 2 + 1;
        int tileX = (int)(nextX / tileMap.getTileSize());
        int tileY = (int)(nextY / tileMap.getTileSize());
        return tileMap.getType(tileY, tileX) == 0;
    }

    // Calculates next position
    private void getNextPosition() {
        if (left) {
            dx = Math.max(-maxSpeed, dx - moveSpeed);
        } else if (right) {
            dx = Math.min(maxSpeed, dx + moveSpeed);
        }
        if (falling) {
            dy = Math.min(maxFallSpeed, dy + fallSpeed);
            if (dy > 0) jumping = false;
        }
    }

    // Updates slugger state
    public void update() {
        getNextPosition();
        checkTileMapCollision();
        setPosition(xtemp, ytemp);

        if (flinching) {
            long elapsed = (System.nanoTime() - flinchTimer) / 1_000_000;
            if (elapsed > 400) flinching = false;
        }

        // Reverses direction at edge
        if (isAtEdge() || (right && dx == 0) || (left && dx == 0)) {
            right = !right;
            left = !left;
            facingRight = right;
            dx = right ? maxSpeed : -maxSpeed;
        }

        if (animation != null) animation.update();
    }

    // Draws slugger
    public void draw(Graphics2D g) {
        if (g == null || tileMap == null) return;
        setMapPosition();
        super.draw(g);
    }
}