package Entity.Projectiles;

import Entity.Animation;
import Entity.MapObject;
import TileMap.TileMap;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;

// Manages Card projectile behavior
public class Card extends MapObject {
    private boolean hit; // Hit state
    private boolean remove; // Removal flag
    private BufferedImage[] sprites; // Card sprite frames
    private BufferedImage[] cardTypes; // Card type sprites
    private int currentCardType; // Current card type index
    private static final int CARD_SIZE = 30; // Card dimensions (pixels)
    private int damage; // Damage value

    // Initializes Card
    public Card(TileMap tm, boolean right, int damage) {
        super(tm); // Defaults to empty tile map
        this.damage = damage;
        this.facingRight = right;
        width = CARD_SIZE;
        height = CARD_SIZE;
        cwidth = 25;
        cheight = 25;
        moveSpeed = 3.5;
        dx = right ? moveSpeed : -moveSpeed;
        loadSprites();
    }

    // Loads card sprites
    private void loadSprites() {
        try {
            BufferedImage spritesheet = ImageIO.read(getClass().getResourceAsStream("/Sprites/Enemies/Projectiles/CardProjectiles.gif"));
            if (spritesheet == null || spritesheet.getWidth() != 150 || spritesheet.getHeight() != 90) {
                throw new IOException("Invalid sprite sheet dimensions");
            }
            cardTypes = new BufferedImage[3];
            sprites = new BufferedImage[5];
            // Loads random card type
            for (int i = 0; i < 3; i++) {
                cardTypes[i] = spritesheet.getSubimage(0, i * CARD_SIZE, 150, CARD_SIZE);
            }
            currentCardType = (int)(Math.random() * 3);
            for (int i = 0; i < 5; i++) {
                sprites[i] = cardTypes[currentCardType].getSubimage(i * CARD_SIZE, 0, CARD_SIZE, CARD_SIZE);
            }
        } catch (IOException e) {
            // Creates placeholder sprites
            sprites = new BufferedImage[5];
            BufferedImage placeholder = new BufferedImage(CARD_SIZE, CARD_SIZE, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = placeholder.createGraphics();
            g.setColor(Color.BLUE);
            g.fillRect(0, 0, CARD_SIZE, CARD_SIZE);
            g.dispose();
            Arrays.fill(sprites, placeholder);
        }
        animation = new Animation();
        if (sprites != null) {
            animation.setFrames(sprites);
            animation.setDelay(70);
        }
    }

    // Marks card as hit
    public void setHit() {
        if (hit) return;
        hit = true;
        dx = 0;
        remove = true;
    }

    // Checks if card should be removed
    public boolean shouldRemove() {
        return remove;
    }

    // Updates card state
    public void update() {
        if (tileMap == null) return;
        // Checks off screen
        if (!hit && (x + xmap < 0 || x + xmap > tileMap.getWidth())) {
            setHit();
        }
        x += dx;
        y += dy;
        if (animation != null) animation.update();
    }

    // Draws card
    @Override
    public void draw(Graphics2D g) {
        if (g == null || tileMap == null || animation == null) return;
        setMapPosition();
        int drawX = (int)(x + xmap - width / 2);
        int drawY = (int)(y + ymap - height / 2);
        if (facingRight) {
            g.drawImage(animation.getImage(), drawX, drawY, null);
        } else {
            g.drawImage(animation.getImage(), drawX + width, drawY, -width, height, null);
        }
    }
}