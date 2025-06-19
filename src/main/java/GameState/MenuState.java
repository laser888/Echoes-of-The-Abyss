package GameState;

import Main.GamePanel;
import TileMap.Background;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

// Main menu screen for the game
public class MenuState extends GameState {

    private Background bg; // Background image
    private GamePanel gamePanel; // Reference to game panel

    private int currentChoice = 0; // Index of current selection
    private String[] options = { "Start", "Settings", "Credits", "Quit" }; // Menu options

    private Color titleColor; // Title text color
    private Font titleFont; // Title font
    private Font font; // Menu font

    // Initializes menu and assets
    public MenuState(GameStateManager gsm, GamePanel gamePanel) {
        this.gsm = gsm;
        this.gamePanel = gamePanel;

        try {
            bg = new Background("/Backgrounds/titlescreen.gif", 1);

            titleColor = new Color(128, 0, 0);
            titleFont = new Font("Century Gothic", Font.PLAIN, 28);
            font = new Font("Arial", Font.PLAIN, 12);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void init() {}

    // Updates background animation
    public void update() {
        bg.update();
    }

    // Draws background, title, and menu options
    public void draw(Graphics2D g) {
        bg.draw(g);

        g.setColor(titleColor);
        g.setFont(titleFont);
        g.drawString("Echoes Of The Abyss", 40, 70);

        g.setFont(font);
        for (int i = 0; i < options.length; i++) {
            g.setColor(i == currentChoice ? Color.WHITE : Color.RED);
            g.drawString(options[i], 145, 140 + i * 15);
        }
    }

    // Executes current menu selection
    private void select() {
        if (currentChoice == 0) {
            gsm.setState(GameStateManager.CLASSSELECTIONSTATE);
        }
        if (currentChoice == 1) {
            gsm.setState(GameStateManager.SETTINGSSTATE);
        }
        if (currentChoice == 2) {
            gsm.setState(GameStateManager.CREDITSSTATE);
        }
        if (currentChoice == 3) {
            System.exit(0);
        }
    }

    // Handles up/down arrow key and enter key input
    public void keyPressed(int k) {
        if (k == KeyEvent.VK_ENTER) {
            select();
        }
        if (k == KeyEvent.VK_UP) {
            currentChoice--;
            if (currentChoice == -1) {
                currentChoice = options.length - 1;
            }
        }
        if (k == KeyEvent.VK_DOWN) {
            currentChoice++;
            if (currentChoice == options.length) {
                currentChoice = 0;
            }
        }
    }

    public void keyReleased(int k) {}

    // Handles mouse click on menu items
    public void mousePressed(MouseEvent e) {
        int panelWidth = gamePanel.getCurrentWidth();
        int panelHeight = gamePanel.getCurrentHeight();

        float scaleX = (float) GamePanel.WIDTH / panelWidth;
        float scaleY = (float) GamePanel.HEIGHT / panelHeight;
        int logicalX = (int) (e.getX() * scaleX);
        int logicalY = (int) (e.getY() * scaleY);

        for (int i = 0; i < options.length; i++) {
            int itemY = 140 + i * 15;
            int itemHeight = 15;
            int textWidth = gamePanel.getFontMetrics(font).stringWidth(options[i]);
            Rectangle itemBounds = new Rectangle(145, itemY - font.getSize(), textWidth, itemHeight);

            if (itemBounds.contains(logicalX, logicalY)) {
                currentChoice = i;
                select();
                System.out.println("Clicked option: " + options[i]);
                break;
            }
        }
    }

    // Overrides spawn coordinates (unused)
    public int getSpawnX() { return 0; }
    public int getSpawnY() { return 0; }

}
