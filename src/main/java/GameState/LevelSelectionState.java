package GameState;

import Main.GamePanel;
import TileMap.Background;
import Data.GameData;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

// Displays level selection screen and handles input
public class LevelSelectionState extends GameState {

    private Background bg; // Background image
    private GamePanel gamePanel; // Reference to panel for dimensions

    private int currentChoice = 0; // Currently highlighted option
    private String[] options = { "Tutorial", "Level 1", "Level 2", "Level 3", "~=[,,_,,]:3" }; // Mouse menu labels (legacy)

    private Color titleColor;
    private Font titleFont;
    private Font optionFont;
    private Font lockFont;

    // Represents one level entry in the menu
    private static class LevelEntry {
        String displayName; // Displayed name
        int stateId; // GameState constant
        String requiredLevelToUnlock; // Key from save data

        LevelEntry(String displayName, int stateId, String requiredLevelToUnlock) {
            this.displayName = displayName;
            this.stateId = stateId;
            this.requiredLevelToUnlock = requiredLevelToUnlock;
        }
    }

    private ArrayList<LevelEntry> allLevels; // All levels in selection
    private Set<String> completedLevelsFromSave; // Completed level keys from save

    private Font font; // For measuring mouse bounds

    // Initializes fonts, background, and level list
    public LevelSelectionState(GameStateManager gsm, GamePanel gamePanel) {
        this.gsm = gsm;
        this.gamePanel = gamePanel;

        try {
            bg = new Background("/Backgrounds/levelscreen.gif", 1);
            titleFont = new Font("Century Gothic", Font.BOLD, 28);
            optionFont = new Font("Arial", Font.BOLD, 22);
            lockFont = new Font("Arial", Font.ITALIC, 14);
            font = new Font("Arial", Font.PLAIN, 28);
        } catch (Exception e) {
            e.printStackTrace();
        }

        allLevels = new ArrayList<>();
        allLevels.add(new LevelEntry("Tutorial", GameStateManager.LEVEL1STATE, null));
        allLevels.add(new LevelEntry("Level 1", GameStateManager.LEVEL2STATE, "Level1State"));
        allLevels.add(new LevelEntry("Level 2", GameStateManager.LEVEL3STATE, "Level2State"));
        allLevels.add(new LevelEntry("Level 3", GameStateManager.LEVEL4STATE, "Level3State"));
    }

    // Loads completed levels from save
    public void init() {
        GameData gameData = gsm.getGameData();

        if (gameData != null && gameData.completedLevels != null) {
            this.completedLevelsFromSave = gameData.completedLevels;
        } else {
            this.completedLevelsFromSave = new HashSet<>();
            System.out.println("Level Selection: No save data found, starting with no completed levels.");
        }

        currentChoice = 0;
    }

    // Updates background animation
    public void update() {
        bg.update();
    }

    // Draws level list and lock status
    public void draw(Graphics2D g) {
        bg.draw(g);
        g.setFont(optionFont);

        int startY = 50;
        int lineHeight = 46;

        for (int i = 0; i < allLevels.size(); i++) {
            LevelEntry level = allLevels.get(i);
            boolean isUnlocked = (level.requiredLevelToUnlock == null || completedLevelsFromSave.contains(level.requiredLevelToUnlock));

            if (i == currentChoice) {
                g.setColor(Color.RED);
            } else {
                g.setColor(isUnlocked ? Color.BLACK : Color.DARK_GRAY);
            }

            g.drawString(level.displayName, 120, startY + i * lineHeight);

            if (!isUnlocked) {
                g.setFont(lockFont);
                g.setColor(new Color(150, 0, 0));
                g.drawString("(Locked)", 120 + g.getFontMetrics(optionFont).stringWidth(level.displayName) + 10, startY + i * lineHeight);
                g.setFont(optionFont);
            }
        }
    }

    // Handles selection confirmation
    private void select() {
        LevelEntry selectedLevel = allLevels.get(currentChoice);
        boolean isUnlocked = (selectedLevel.requiredLevelToUnlock == null || completedLevelsFromSave.contains(selectedLevel.requiredLevelToUnlock));

        if (isUnlocked) {
            System.out.println("Starting level: " + selectedLevel.displayName);
            gsm.setState(selectedLevel.stateId);
        } else {
            System.out.println("Cannot start locked level: " + selectedLevel.displayName);
        }
    }

    // Handles key input
    public void keyPressed(int k) {
        if (k == KeyEvent.VK_ENTER) {
            select();
        }
        if (k == KeyEvent.VK_UP) {
            currentChoice = (currentChoice - 1 + options.length) % options.length;
        }
        if (k == KeyEvent.VK_DOWN) {
            currentChoice = (currentChoice + 1) % options.length;
        }
        if (k == KeyEvent.VK_ESCAPE) {
            gsm.setState(GameStateManager.CLASSSELECTIONSTATE);
        }
    }

    public void keyReleased(int k) {}

    // Handles mouse click to select level
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

    // Overrides player spawn (not used)
    public int getSpawnX() { return 0; }
    public int getSpawnY() { return 0; }
}
