package Entity;

import TileMap.TileMap;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;

// Manages explosion effect
public class Explosion {
    private int x; // World X position
    private int y; // World Y position
    private int xmap; // Map offset X
    private int ymap; // Map offset Y
    private int width; // Sprite width
    private int height; // Sprite height
    private Animation animation; // Explosion animation
    private BufferedImage[] sprites; // Sprite frames
    private boolean remove; // Removal flag
    private static final int SPRITE_COUNT = 6; // Sprite frame count
    private static final int SPRITE_SIZE = 30; // Sprite dimensions (pixels)

    // Initializes Explosion
    public Explosion(TileMap tm, int x, int y) {
        this.x = x;
        this.y = y;
        this.width = SPRITE_SIZE;
        this.height = SPRITE_SIZE;
        loadSprites();
    }

    // Loads explosion sprites
    private void loadSprites() {
        try {
            BufferedImage spritesheet = ImageIO.read(getClass().getResourceAsStream("/Sprites/Enemies/explosion.gif"));
            if (spritesheet == null || spritesheet.getWidth() < SPRITE_SIZE * SPRITE_COUNT || spritesheet.getHeight() < SPRITE_SIZE) {
                throw new IOException("Invalid sprite sheet");
            }
            sprites = new BufferedImage[SPRITE_COUNT];
            for (int i = 0; i < SPRITE_COUNT; i++) {
                sprites[i] = spritesheet.getSubimage(i * SPRITE_SIZE, 0, SPRITE_SIZE, SPRITE_SIZE);
            }
            animation = new Animation();
            animation.setFrames(sprites);
            animation.setDelay(70);
        } catch (IOException e) {
            // Creates placeholder sprites
            BufferedImage placeholder = new BufferedImage(SPRITE_SIZE, SPRITE_SIZE, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = placeholder.createGraphics();
            g.setColor(Color.BLUE);
            g.fillRect(0, 0, SPRITE_SIZE, SPRITE_SIZE);
            g.dispose();
            sprites = new BufferedImage[SPRITE_COUNT];
            Arrays.fill(sprites, placeholder);
            animation = new Animation();
            animation.setFrames(sprites);
            animation.setDelay(70);
        }
    }

    // Updates explosion animation
    public void update() {
        if (animation != null) {
            animation.update();
            if (animation.hasPlayedOnce()) remove = true;
        }
    }

    // Checks if explosion should be removed
    public boolean shouldRemove() {
        return remove;
    }

    // Sets map offset
    public void setMapPosition(int x, int y) {
        this.xmap = x;
        this.ymap = y;
    }

    // Draws explosion
    public void draw(Graphics2D g) {
        if (g == null || animation == null) return;
        BufferedImage image = animation.getImage();
        if (image != null) {
            g.drawImage(image, x + xmap - width / 2, y + ymap - height / 2, width, height, null);
        }
    }
}