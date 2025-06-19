package GameState;

import Data.GameData;
import Main.GamePanel;
import TileMap.Background;
import Entity.Player;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

// Handles class selection
public class ClassSelectionState extends GameState {
    private Background bg; // Background
    private GamePanel gamePanel; // Game panel
    private GameData gameData; // Game data
    private int currentChoice = 0; // Menu choice
    private String[] options = { "Mage", "Berserker", "Archer", "Back to Menu" }; // Menu options
    private Color titleColor; // Title color
    private Font titleFont; // Title font
    private Font optionFont; // Option font

    // Initializes state
    public ClassSelectionState(GameStateManager gsm, GamePanel gamePanel) {
        this.gsm = gsm;
        this.gamePanel = gamePanel;
        this.gameData = gsm.getGameData();
        try {
            bg = new Background("/Backgrounds/menubg.gif", 0.8);
            titleColor = new Color(220, 180, 50);
            titleFont = new Font("Century Gothic", Font.BOLD, 28);
            optionFont = new Font("Arial", Font.BOLD, 20);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Initializes state
    public void init() {
        currentChoice = 0;
    }

    // Updates state
    public void update() {
        bg.update();
    }

    // Draws state
    public void draw(Graphics2D g) {
        bg.draw(g);
        // Draws title
        g.setColor(titleColor);
        g.setFont(titleFont);
        String titleText = "Choose Your Class";
        FontMetrics fmTitle = g.getFontMetrics(titleFont);
        int titleWidth = fmTitle.stringWidth(titleText);
        g.drawString(titleText, (GamePanel.WIDTH - titleWidth) / 2, 60);
        // Draws options
        g.setFont(optionFont);
        FontMetrics fmOption = g.getFontMetrics(optionFont);
        int optionsStartY = 120;
        int optionSpacing = 35;
        for (int i = 0; i < options.length; i++) {
            String text = options[i];
            if (i < 3) {
                String classKey = options[i].toUpperCase();
                int level = 0;
                if (gameData.playerClassProgress.containsKey(classKey)) {
                    level = gameData.playerClassProgress.get(classKey).getOrDefault("level", 0);
                }
                text += " (Lv " + level + ")";
            }
            g.setFont(optionFont);
            g.setColor(i == currentChoice ? Color.YELLOW : Color.BLACK);
            int optionWidth = fmOption.stringWidth(text);
            g.drawString(text, (GamePanel.WIDTH - optionWidth) / 2, optionsStartY + i * optionSpacing);
        }
    }

    // Handles menu selection
    private void select() {
        Player.PlayerClass selectedClass = Player.PlayerClass.NONE;
        boolean proceedToGame = false;
        switch (currentChoice) {
            case 0:
                selectedClass = Player.PlayerClass.MAGE;
                proceedToGame = true;
                break;
            case 1:
                selectedClass = Player.PlayerClass.BERSERKER;
                proceedToGame = true;
                break;
            case 2:
                selectedClass = Player.PlayerClass.ARCHER;
                proceedToGame = true;
                break;
            case 3:
                gsm.setState(GameStateManager.MENUSTATE);
                return;
        }
        if (proceedToGame) {
            gsm.setSelectedPlayerClass(selectedClass);
            GameData data = gsm.getGameData();
            boolean hasCompletedLevel = data != null && data.completedLevels != null && !data.completedLevels.isEmpty();
            if (!hasCompletedLevel) {
                gsm.setState(GameStateManager.INTROSTATE);
            } else {
                gsm.setState(GameStateManager.LevelSelectionState);
            }
        }
    }

    // Handles key press
    public void keyPressed(int k) {
        if (k == KeyEvent.VK_ENTER) {
            select();
        }
        if (k == KeyEvent.VK_UP) {
            currentChoice--;
            if (currentChoice < 0) {
                currentChoice = options.length - 1;
            }
        }
        if (k == KeyEvent.VK_DOWN) {
            currentChoice++;
            if (currentChoice >= options.length) {
                currentChoice = 0;
            }
        }
        if (k == KeyEvent.VK_ESCAPE) {
            gsm.setState(GameStateManager.MENUSTATE);
        }
    }

    // Handles key release
    public void keyReleased(int k) {}

    // Handles mouse press
    public void mousePressed(MouseEvent e) {
        int panelWidth = gamePanel.getCurrentWidth();
        int panelHeight = gamePanel.getCurrentHeight();
        float scaleX = (float) GamePanel.WIDTH / panelWidth;
        float scaleY = (float) GamePanel.HEIGHT / panelHeight;
        int logicalX = (int) (e.getX() * scaleX);
        int logicalY = (int) (e.getY() * scaleY);
        FontMetrics fmOption = gamePanel.getFontMetrics(optionFont);
        int optionsStartY = 120;
        int optionSpacing = 35;
        for (int i = 0; i < options.length; i++) {
            int optionWidth = fmOption.stringWidth(options[i]);
            int textDrawX = (GamePanel.WIDTH - optionWidth) / 2;
            int textDrawY = optionsStartY + i * optionSpacing;
            Rectangle itemBounds = new Rectangle(
                    textDrawX,
                    textDrawY - fmOption.getAscent(),
                    optionWidth,
                    fmOption.getHeight()
            );
            if (itemBounds.contains(logicalX, logicalY)) {
                currentChoice = i;
                select();
                break;
            }
        }
    }

    // Gets spawn X
    public int getSpawnX() { return 0; }

    // Gets spawn Y
    public int getSpawnY() { return 0; }
}