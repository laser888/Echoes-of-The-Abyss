package GameState;

import java.awt.*;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import Main.GamePanel;

public class CatState extends GameState {
    private Image catImage;
    private Color titleColor;
    private Font titleFont;
    private Font font;

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

    public void init() {}

    public void update() {
    }

    public void draw(Graphics2D g) {
        // Clear background
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

    public void keyPressed(int k) {
        if (k == KeyEvent.VK_ESCAPE) {
            gsm.setState(GameStateManager.MENUSTATE);
        }
    }

    public void keyReleased(int k) {}
}