package GameState;

import Main.GamePanel;
import Score.ScoreData;
import TileMap.Background;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

// Displays level completion screen and score breakdown
public class WinState extends GameState {

    private Background bg; // Background image
    private GamePanel gamePanel; // Game panel reference

    private int currentChoice = 0; // Current selected menu item
    private String[] options = { "Next Level", "Replay", "Main Menu", "Quit" }; // Menu options

    private Color titleColor; // Title text color
    private Font titleFont; // Font for "LEVEL COMPLETED"
    private Font rankFont; // Font for letter grade
    private Font bigScoreFont; // Font for score number
    private Font statsInfoFont; // Font for detailed stats
    private Font xpFont; // Font for XP gained
    private Font font; // Font for menu options

    private ScoreData scoreData; // Score breakdown from level
    private int nextLevelStateId; // ID for next level state

    // Initializes fonts, colors, background
    public WinState(GameStateManager gsm, GamePanel gamePanel) {
        this.gsm = gsm;
        this.gamePanel = gamePanel;

        try {
            bg = new Background("/Backgrounds/menubg.gif", 1);

            titleColor = new Color(0, 100, 0);
            titleFont = new Font("Century Gothic", Font.BOLD, 28);
            rankFont = new Font("Arial", Font.BOLD, 60);
            bigScoreFont = new Font("Arial", Font.BOLD, 42);
            statsInfoFont = new Font("Arial", Font.PLAIN, 14);
            xpFont = new Font("Arial", Font.BOLD, 14);
            font = new Font("Arial", Font.PLAIN, 12);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Sets which state is considered the "next level"
    public void setNextLevelState(int id) {
        this.nextLevelStateId = id;
    }

    // Sets the score data to display
    public void setScoreData(ScoreData data) {
        this.scoreData = data;
    }

    public void init() {}

    // Updates background animation
    public void update() {
        bg.update();
    }

    // Draws win screen title, score info, XP, and menu
    public void draw(Graphics2D g) {
        bg.draw(g);

        // Title
        g.setFont(titleFont);
        g.setColor(titleColor);
        String winText = "LEVEL COMPLETED!";
        g.drawString(winText, (GamePanel.WIDTH - g.getFontMetrics().stringWidth(winText)) / 2, 40);

        // Rank
        g.setFont(rankFont);
        g.setColor(Color.ORANGE);
        int rankWidth = g.getFontMetrics().stringWidth(scoreData.rank);
        g.drawString(scoreData.rank, GamePanel.WIDTH - 50 - rankWidth, 105);

        // Final Score
        g.setFont(bigScoreFont);
        String scoreStr = String.format("%.0f", scoreData.finalDisplayScore);
        g.drawString(scoreStr, GamePanel.WIDTH - 50 - g.getFontMetrics().stringWidth(scoreStr), 175);

        // Stats
        g.setFont(statsInfoFont);
        g.setColor(Color.WHITE);
        int statsY = 70;
        int lineHeight = 16;

        g.drawString(String.format("Enemies Defeated: %d / %d", scoreData.enemiesKilled, scoreData.totalEnemies), 20, statsY);
        statsY += lineHeight;

        g.drawString(String.format("Terminals Completed: %d / %d", scoreData.puzzlesSolved, scoreData.totalPuzzles), 20, statsY);
        statsY += lineHeight;

        g.drawString("Time: " + scoreData.timeTakenFormatted, 20, statsY);
        statsY += lineHeight;

        if (scoreData.playerDidNotDieInLevel && scoreData.finalDisplayScore > 0) {
            g.setColor(Color.BLUE);
            g.drawString("No Death Bonus: +10", 20, statsY);
            statsY += lineHeight;
        }

        g.setColor(Color.WHITE);
        g.drawString("Rank: " + scoreData.rank, 20, statsY);
        statsY += lineHeight;

        // XP Gained
        g.setFont(xpFont);
        g.setColor(new Color(150, 255, 150));
        statsY += lineHeight - 18;
        g.drawString(gsm.getSelectedPlayerClass() + " Gained: +" + scoreData.xpAwarded, 20, statsY);

        // Menu Options
        g.setFont(font);
        int optionsY = statsY + 20;

        for (int i = 0; i < options.length; i++) {
            g.setColor(i == currentChoice ? Color.BLACK : Color.YELLOW);
            g.drawString(options[i], 20, optionsY + i * 12);
        }
    }

    // Executes action for selected option
    private void select() {
        if (currentChoice == 0) {
            gsm.setState(nextLevelStateId); // Next level
        }
        if (currentChoice == 1) {
            gsm.setState(nextLevelStateId - 1); // Replay current level
        }
        if (currentChoice == 2) {
            gsm.setState(GameStateManager.MENUSTATE); // Return to menu
        }
        if (currentChoice == 3) {
            System.exit(0); // Exit game
        }
    }

    // Handles keyboard navigation
    public void keyPressed(int k) {
        if (k == KeyEvent.VK_ENTER) {
            select();
        }
        if (k == KeyEvent.VK_UP) {
            currentChoice--;
            if (currentChoice < 0) currentChoice = options.length - 1;
        }
        if (k == KeyEvent.VK_DOWN) {
            currentChoice++;
            if (currentChoice >= options.length) currentChoice = 0;
        }
    }

    public void keyReleased(int k) {}

    // Handles mouse clicks on options
    public void mousePressed(MouseEvent e) {
        int panelWidth = gamePanel.getCurrentWidth();
        int panelHeight = gamePanel.getCurrentHeight();

        float scaleX = (float) GamePanel.WIDTH / panelWidth;
        float scaleY = (float) GamePanel.HEIGHT / panelHeight;
        int logicalX = (int) (e.getX() * scaleX);
        int logicalY = (int) (e.getY() * scaleY);

        int optionsY = scoreData != null
                ? (70 + (scoreData.playerDidNotDieInLevel && scoreData.finalDisplayScore > 0 ? 5 : 4) * 16 + 10)
                : 100;

        for (int i = 0; i < options.length; i++) {
            int itemY = optionsY + i * 12;
            int textWidth = gamePanel.getFontMetrics(font).stringWidth(options[i]);
            Rectangle itemBounds = new Rectangle(20, itemY - font.getSize(), textWidth, 12);

            if (itemBounds.contains(logicalX, logicalY)) {
                currentChoice = i;
                select();
                System.out.println("Clicked option: " + options[i]);
                break;
            }
        }
    }

    public int getSpawnX() { return 0; }
    public int getSpawnY() { return 0; }
}
