package GameState;

import Main.GamePanel;
import TileMap.Background;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;

// Shows game credits
public class CreditsState extends GameState {
    private Background bg; // Background
    private GamePanel gamePanel; // Game panel
    private int currentChoice = 0; // Menu choice
    private Color titleColor; // Title color
    private Font titleFont; // Title font
    private Font creditFont; // Credits font
    private long shutdownStartTime; // Shutdown timer
    private boolean showFullCredits = false; // Full credits toggle
    private Font font; // Text font

    // Initializes state
    public CreditsState(GameStateManager gsm, GamePanel gamePanel) {
        this.gsm = gsm;
        this.gamePanel = gamePanel;
        try {
            bg = new Background("/Backgrounds/titlescreen.gif", 1);
            titleColor = new Color(128, 0, 0);
            titleFont = new Font("Century Gothic", Font.PLAIN, 28);
            creditFont = new Font("Arial", Font.PLAIN, 12);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Initializes state
    public void init() {}

    // Updates state
    public void update() {
        bg.update();
    }

    // Draws state
    public void draw(Graphics2D g) {
        bg.draw(g);
        // Draws title
        g.setColor(titleColor);
        g.setFont(new Font("Arial", Font.BOLD, 28));
        String title = "CREDITS";
        int titleWidth = g.getFontMetrics().stringWidth(title);
        g.drawString(title, (GamePanel.WIDTH - titleWidth) / 2, 50);
        // Draws Denis's credits
        g.setFont(new Font("Arial", Font.BOLD, 24));
        String denis = "Denis Rodin";
        int denisWidth = g.getFontMetrics().stringWidth(denis);
        g.drawString(denis, (GamePanel.WIDTH - denisWidth) / 2, 105);
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        String denisRole = "Coding, Game Design";
        int denisRoleWidth = g.getFontMetrics().stringWidth(denisRole);
        g.drawString(denisRole, (GamePanel.WIDTH - denisRoleWidth) / 2, 125);
        // Draws Dryden's credits
        g.setFont(new Font("Arial", Font.BOLD, 24));
        String dryden = "Dryden Fralick";
        int drydenWidth = g.getFontMetrics().stringWidth(dryden);
        g.drawString(dryden, (GamePanel.WIDTH - drydenWidth) / 2, 165);
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        String drydenRole = "Artwork";
        int drydenRoleWidth = g.getFontMetrics().stringWidth(drydenRole);
        g.drawString(drydenRole, (GamePanel.WIDTH - drydenRoleWidth) / 2, 180);
        // Draws Luke's credits
        g.setFont(new Font("Arial", Font.BOLD, 24));
        String luke = "Luke Chess";
        int lukeWidth = g.getFontMetrics().stringWidth(luke);
        g.drawString(luke, (GamePanel.WIDTH - lukeWidth) / 2, 215);
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        String lukeRole = "Artwork";
        int lukeRoleWidth = g.getFontMetrics().stringWidth(lukeRole);
        g.drawString(lukeRole, (GamePanel.WIDTH - lukeRoleWidth) / 2, 230);
    }

    // Handles menu selection
    private void select() {}

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