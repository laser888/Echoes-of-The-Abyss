package GameState;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.ImageIcon;
import Main.GamePanel;

// Shows cat GIF
public class CatState extends GameState {
    private Image catImage; // Cat GIF
    private Color titleColor; // Title color
    private Font titleFont; // Title font
    private Font font; // Text font

    // Initializes state
    public CatState(GameStateManager gsm) {
        this.gsm = gsm;
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/Backgrounds/cat-cat-vibing.gif"));
            catImage = icon.getImage();
            titleColor = new Color(128, 0, 0);
            titleFont = new Font("Century Gothic", Font.PLAIN, 28);
            font = new Font("Arial", Font.PLAIN, 12);
        } catch (Exception e) {
            System.err.println("CatState: Failed to load GIF");
            e.printStackTrace();
        }
    }

    // Initializes state
    public void init() {}

    // Updates state
    public void update() {}

    // Draws state
    public void draw(Graphics2D g) {
        // Clears background
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT);
        if (catImage != null) {
            int imgWidth = catImage.getWidth(null);
            int imgHeight = catImage.getHeight(null);
            if (imgWidth > 0 && imgHeight > 0) {
                double aspectRatio = (double) imgWidth / imgHeight;
                int scaledWidth = GamePanel.WIDTH;
                int scaledHeight = (int) (GamePanel.WIDTH / aspectRatio);
                if (scaledHeight > GamePanel.HEIGHT) {
                    scaledHeight = GamePanel.HEIGHT;
                    scaledWidth = (int) (GamePanel.HEIGHT * aspectRatio);
                }
                int x = (GamePanel.WIDTH - scaledWidth) / 2;
                int y = (GamePanel.HEIGHT - scaledHeight) / 2;
                g.drawImage(catImage, x, y, scaledWidth, scaledHeight, null);
            } else {
                System.err.println("CatState: Invalid GIF dimensions");
            }
        } else {
            System.err.println("CatState: No GIF to draw");
        }
    }

    // Handles key press
    public void keyPressed(int k) {
        if (k == KeyEvent.VK_ESCAPE) {
            gsm.setState(GameStateManager.MENUSTATE);
        }
    }

    // Handles key release
    public void keyReleased(int k) {}

    // Handles mouse press
    public void mousePressed(MouseEvent e) {}

    // Gets spawn X
    public int getSpawnX() { return 0; }

    // Gets spawn Y
    public int getSpawnY() { return 0; }
}