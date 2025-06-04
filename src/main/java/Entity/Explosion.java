package Entity;

import TileMap.TileMap;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Explosion {
    private int x;
    private int y;
    private int xmap;
    private int ymap;
    private int width;
    private int height;
    private Animation animation;
    private BufferedImage[] sprites;
    private boolean remove;

    public Explosion(TileMap tm, int x, int y) {
        this.x = x;
        this.y = y;
        this.width = 30;
        this.height = 30;

        try {
            BufferedImage spritesheet = ImageIO.read(getClass().getResourceAsStream("/Sprites/Enemies/explosion.gif"));
            if (spritesheet == null) {
                throw new IOException("Explosion spritesheet not found: /Sprites/Enemies/explosion.gif");
            }
            sprites = new BufferedImage[6];
            for (int i = 0; i < sprites.length; i++) {
                sprites[i] = spritesheet.getSubimage(i * width, 0, width, height);
            }
            animation = new Animation();
            animation.setFrames(sprites);
            animation.setDelay(70);
        } catch (Exception e) {
            System.err.println("Error loading explosion sprites: " + e.getMessage());
            e.printStackTrace();
            remove = true;
        }
    }

    public void update() {
        if (animation != null) {
            animation.update();
            if (animation.hasPlayedOnce()) {
                remove = true;
            }
        }
    }

    public boolean shouldRemove() {
        return remove;
    }

    public void setMapPosition(int x, int y) {
        this.xmap = x;
        this.ymap = y;
    }

    public void draw(Graphics2D g) {
        if (animation == null || animation.getImage() == null) {
            return;
        }
        g.drawImage(
                animation.getImage(),
                x + xmap - width / 2,
                y + ymap - height / 2,
                width,
                height,
                null
        );
    }
}