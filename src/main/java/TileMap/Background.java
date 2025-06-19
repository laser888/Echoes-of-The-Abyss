package TileMap;

import Main.GamePanel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;

// Handles parallax scrolling background
public class Background {

    private BufferedImage image; // Background image

    private double x; // X position
    private double y; // Y position
    private double dx; // X velocity
    private double dy; // Y velocity

    private double moveScale; // Parallax movement factor

    // Loads image and sets move scale
    public Background(String s, double ms) {
        try {
            image = ImageIO.read(getClass().getResourceAsStream(s));
            moveScale = ms;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Sets background position with parallax effect
    public void setPosition(double x, double y) {
        this.x = (x * moveScale) % GamePanel.WIDTH;
        this.y = (y * moveScale) % GamePanel.HEIGHT;
    }

    // Sets background scrolling velocity
    public void setVector(double dx, double dy) {
        this.dx = dx;
        this.dy = dy;
    }

    // Updates background position
    public void update() {
        x += dx;
        y += dy;
    }

    // Draws background (loops horizontally if needed)
    public void draw(Graphics2D g) {
        g.drawImage(image, (int) x, (int) y, null);

        if (x < 0) {
            g.drawImage(image, (int) x + GamePanel.WIDTH, (int) y, null);
        }

        if (x > 0) {
            g.drawImage(image, (int) x - GamePanel.WIDTH, (int) y, null);
        }
    }
}
