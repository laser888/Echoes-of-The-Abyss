package Entity.Projectiles;

import Entity.Animation;
import Entity.MapObject;
import TileMap.TileMap;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;

// Manages FireBall projectile behavior
public class FireBall extends MapObject {
    private boolean hit; // Hit state
    private boolean remove; // Removal flag
    private BufferedImage[] sprites; // Flight animation frames
    private BufferedImage[] hitSprites; // Hit animation frames
    private static final int SPRITE_COUNT = 4; // Flight sprite count
    private static final int HIT_SPRITE_COUNT = 3; // Hit sprite count

    // Initializes FireBall
    public FireBall(TileMap tm, boolean right) {
        super(tm); // Defaults to empty tile map
        facingRight = right;
        moveSpeed = 3.8;
        dx = right ? moveSpeed : -moveSpeed;
        width = 30;
        height = 30;
        cwidth = 14;
        cheight = 15;
        loadSprites();
    }

    // Loads fireball sprites
    private void loadSprites() {
        try {
            BufferedImage spritesheet = ImageIO.read(getClass().getResourceAsStream("/Sprites/Player/Projectiles/fireball.gif"));
            if (spritesheet == null || spritesheet.getWidth() < width * SPRITE_COUNT || spritesheet.getHeight() < height * 2) {
                throw new IOException("Invalid sprite sheet");
            }
            sprites = new BufferedImage[SPRITE_COUNT];
            hitSprites = new BufferedImage[HIT_SPRITE_COUNT];
            for (int i = 0; i < SPRITE_COUNT; i++) {
                sprites[i] = spritesheet.getSubimage(i * width, 0, width, height);
            }
            for (int i = 0; i < HIT_SPRITE_COUNT; i++) {
                hitSprites[i] = spritesheet.getSubimage(i * width, height, width, height);
            }
        } catch (IOException e) {
            // Creates placeholder sprites
            BufferedImage placeholder = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = placeholder.createGraphics();
            g.setColor(Color.BLUE);
            g.fillRect(0, 0, width, height);
            g.dispose();
            sprites = new BufferedImage[SPRITE_COUNT];
            hitSprites = new BufferedImage[HIT_SPRITE_COUNT];
            Arrays.fill(sprites, placeholder);
            Arrays.fill(hitSprites, placeholder);
        }
        animation = new Animation();
        if (sprites != null) {
            animation.setFrames(sprites);
            animation.setDelay(70);
        }
    }

    // Triggers hit animation
    public void setHit() {
        if (hit) return;
        hit = true;
        if (animation != null && hitSprites != null) {
            animation.setFrames(hitSprites);
            animation.setDelay(70);
        }
        dx = 0;
    }

    // Checks if projectile should be removed
    public boolean shouldRemove() {
        return remove;
    }

    // Updates projectile state
    public void update() {
        if (tileMap == null) return;
        checkTileMapCollision();
        setPosition(xtemp, ytemp);
        // Triggers hit on collision
        if (dx == 0 && !hit) {
            setHit();
        }
        // Removes after hit animation
        if (hit && animation != null && animation.hasPlayedOnce()) {
            remove = true;
        }
        if (animation != null) animation.update();
    }

    // Draws projectile
    public void draw(Graphics2D g) {
        if (g == null || tileMap == null || animation == null) return;
        setMapPosition();
        super.draw(g);
    }
}