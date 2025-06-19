package Entity.Enemies.Bosses;

import Entity.Animation;
import Entity.Enemy;
import Entity.Player;
import Main.GamePanel;
import TileMap.TileMap;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

// Manages SluggerBoss behavior
public class SluggerBoss extends Enemy {
    private Player player; // Player reference
    private static final int SPRITE_COUNT = 3; // Number of sprite frames
    private static final double TOLERANCE = 1.5; // Movement tolerance (pixels)

    // Initializes SluggerBoss
    public SluggerBoss(TileMap tm, Player player) {
        super(tm);
        this.player = player;
        moveSpeed = 0.6;
        maxSpeed = 0.6;
        fallSpeed = 0.2;
        maxFallSpeed = 10.0;
        name = "Slugger Boss";
        width = 30;
        height = 30;
        cwidth = 20;
        cheight = 20;
        health = maxHealth = 600;
        damage = 45;
        loadSprites();
        right = true;
        facingRight = true;
    }

    // Loads boss sprites
    private void loadSprites() {
        try {
            BufferedImage spritesheet = ImageIO.read(getClass().getResourceAsStream("/Sprites/Enemies/Bosses/sluggerBoss.gif"));
            if (spritesheet == null || spritesheet.getWidth() < width * SPRITE_COUNT || spritesheet.getHeight() < height) {
                throw new IOException("Invalid sprite sheet");
            }
            sprites = new ArrayList<>();
            BufferedImage[] frames = new BufferedImage[SPRITE_COUNT];
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
        setAnimation();
    }

    // Sets animation
    private void setAnimation() {
        if (animation != null && sprites != null && !sprites.isEmpty()) {
            animation.setFrames(sprites.get(0));
            animation.setDelay(300);
        }
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

    // Updates boss state
    public void update() {
        getNextPosition();
        checkTileMapCollision();
        setPosition(xtemp, ytemp);

        if (flinching) {
            long elapsed = (System.nanoTime() - flinchTimer) / 1_000_000;
            if (elapsed > 400) flinching = false;
        }

        // Chases player or patrols
        if (player != null) {
            double playerX = player.getPositionX();
            double bossX = getx();
            if (Math.abs(playerX - bossX) <= TOLERANCE) {
                right = false;
                left = false;
                dx = 0;
            } else {
                right = playerX > bossX;
                left = !right;
                facingRight = right;
                dx = right ? maxSpeed : -maxSpeed;
            }
        } else if ((right && dx == 0) || (left && dx == 0)) {
            right = !right;
            left = !left;
            facingRight = right;
            dx = right ? maxSpeed : -maxSpeed;
        }

        if (animation != null) animation.update();
    }

    // Draws boss
    public void draw(Graphics2D g) {
        if (g == null || tileMap == null) return;
        setMapPosition();
        super.draw(g);
    }

    // Identifies as boss
    @Override
    public boolean isBoss() {
        return true;
    }
}