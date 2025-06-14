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

public class LevelSelectionState extends GameState {

    private Background bg;
    private GamePanel gamePanel;

    private int currentChoice = 0;
    private String[] options = { "Tutorial", "Level 1", "Level 2", "Level 3", "~=[,,_,,]:3"};

    private Color titleColor;
    private Font titleFont;
    private Font optionFont;
    private Font lockFont;

    private static class LevelEntry {
        String displayName;
        int stateId;
        String requiredLevelToUnlock;

        LevelEntry(String displayName, int stateId, String requiredLevelToUnlock) {
            this.displayName = displayName;
            this.stateId = stateId;
            this.requiredLevelToUnlock = requiredLevelToUnlock;
        }
    }

    private ArrayList<LevelEntry> allLevels;
    private Set<String> completedLevelsFromSave;

    private Font font;

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
        allLevels.add(new LevelEntry("Tutorial", GameStateManager.LEVEL1STATE, null)); // Requires no previous level
        allLevels.add(new LevelEntry("Level 2", GameStateManager.LEVEL2STATE, "Level1State")); // Requires Level 1
        allLevels.add(new LevelEntry("Level 3", GameStateManager.LEVEL3STATE, "Level2State")); // Requires Level 2
        allLevels.add(new LevelEntry("Level 4", GameStateManager.LEVEL4STATE, "Level3State")); // Requires Level 3
        allLevels.add(new LevelEntry("~=[,,_,,]:3", GameStateManager.CATSTATE, null));
    }

    public void init() {

        GameData gameData = gsm.getGameData();

        if (gameData != null && gameData.completedLevels != null) {
            this.completedLevelsFromSave = gameData.completedLevels;
            //System.out.println("Level Selection: Loaded completed levels: " + completedLevelsFromSave);
        } else {
            // Fallback
            this.completedLevelsFromSave = new HashSet<>();
            System.out.println("Level Selection: No save data found, starting with no completed levels.");
        }
        currentChoice = 0;
    }

    public void update() {
        bg.update();
    }

    public void draw(java.awt.Graphics2D g) {
        // draw bg
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

            // Draw Locked
            if (!isUnlocked) {
                g.setFont(lockFont);
                g.setColor(new Color(150, 0, 0));
                g.drawString("(Locked)", 120 + g.getFontMetrics(optionFont).stringWidth(level.displayName) + 10, startY + i * lineHeight);
                g.setFont(optionFont);
            }
        }
    }

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

    public void keyPressed(int k) {
        if(k == KeyEvent.VK_ENTER) {
            select();
        }
        if(k == KeyEvent.VK_UP) {
            currentChoice--;
            if(currentChoice == -1) {
                currentChoice = options.length - 1 ;
            }
        }
        if(k == KeyEvent.VK_DOWN) {
            currentChoice++;
            if(currentChoice == options.length) {
                currentChoice = 0;
            }
        }
    }

    public void keyReleased(int k) {}

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

    public int getSpawnX() {return 0;}
    public  int getSpawnY() { return 0;}

}