package Entity.Projectiles;

import Entity.Animation;
import Entity.MapObject;
import TileMap.TileMap;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class CardProjectile extends MapObject {
    private boolean hit;
    private boolean remove;
    private BufferedImage[] sprites;
    private BufferedImage[] cardTypes;
    private int currentCardType;
    private static final int CARD_SIZE = 30;
    private int damage;

    public CardProjectile(TileMap tm, boolean right, int damage) {
        super(tm);
        this.damage = damage;
        facingRight = right;

        width = CARD_SIZE;
        height = CARD_SIZE;
        cwidth = 25;
        cheight = 25;
        moveSpeed = 3.5;
        dx = right ? moveSpeed : -moveSpeed;

        String[] possiblePaths = {"/Sprites/Enemies/Projectiles/CardProjectiles.gif"};
        BufferedImage spritesheet = null;

        try {
            for (String path : possiblePaths) {
                spritesheet = ImageIO.read(getClass().getResourceAsStream(path));
                if (spritesheet != null) break;
            }
            if (spritesheet == null)
                throw new IOException("Card projectile sprite sheet not found");

            if (spritesheet.getWidth() != 150 || spritesheet.getHeight() != 90)
                throw new Exception("Invalid sprite sheet dimensions: " + spritesheet.getWidth() + "x" + spritesheet.getHeight());

            cardTypes = new BufferedImage[3];
            sprites = new BufferedImage[5];
            for (int i = 0; i < 3; i++) {
                cardTypes[i] = spritesheet.getSubimage(0, i * CARD_SIZE, 150, CARD_SIZE);
            }
            currentCardType = (int)(Math.random() * 3);
            for (int i = 0; i < 5; i++) {
                sprites[i] = cardTypes[currentCardType].getSubimage(i * CARD_SIZE, 0, CARD_SIZE, CARD_SIZE);
            }
        } catch (Exception e) {
            System.err.println("Error loading CardProjectile sprites: " + e.getMessage());
            e.printStackTrace();
            sprites = new BufferedImage[5];
            BufferedImage placeholder = new BufferedImage(CARD_SIZE, CARD_SIZE, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = placeholder.createGraphics();
            g.setColor(Color.BLUE);
            g.fillRect(0, 0, CARD_SIZE, CARD_SIZE);
            g.dispose();
            for (int i = 0; i < 5; i++) sprites[i] = placeholder;
        }

        animation = new Animation();
        animation.setFrames(sprites);
        animation.setDelay(70);
    }

    public void setHit() {
        if (hit) return;
        hit = true;
        dx = 0;
        remove = true;
    }

    public boolean shouldRemove() { return remove; }

    public void update() {
        x += dx;
        y += dy;

        // Disappears when it goes off screen
        if (!hit && (x + xmap < 0 || x + xmap > tileMap.getWidth())) {
            setHit();
        }

        animation.update();
    }

    @Override
    public void draw(Graphics2D g) {
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
