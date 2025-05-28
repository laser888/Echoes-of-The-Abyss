package GameState;

import Main.GamePanel;
import Score.ScoreData;
import TileMap.Background;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class WinState extends GameState {

    private Background bg;
    private GamePanel gamePanel;

    private int currentChoice = 0;
    private String[] options = { "Main Menu", "Quit"};

    private Color titleColor;
    private Font titleFont;
    private Font rankFont;
    private Font bigScoreFont;
    private Font statsInfoFont;
    private Font xpFont;
    private Font font;

    private ScoreData scoreData;

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

    public void setScoreData(ScoreData data) {
        this.scoreData = data;
    }

    public void init() {}

    public void update() {
        bg.update();
    }

    public void draw(java.awt.Graphics2D g) {
        bg.draw(g);

        g.setFont(titleFont);
        String winText = "LEVEL COMPLETED!";
        int winTextWidth = g.getFontMetrics().stringWidth(winText);
        g.setColor(titleColor);
        g.drawString(winText, (GamePanel.WIDTH - winTextWidth) / 2, 40);

        g.setFont(rankFont);
        g.setColor(Color.ORANGE);
        int rankWidth = g.getFontMetrics().stringWidth(scoreData.rank);
        g.drawString(scoreData.rank, GamePanel.WIDTH - 80 - rankWidth, 105);

        g.setFont(bigScoreFont);
        String scoreStr = String.format("%.0f", scoreData.finalDisplayScore);
        int scoreStrWidth = g.getFontMetrics().stringWidth(scoreStr);
        g.drawString(scoreStr, GamePanel.WIDTH - 80 - scoreStrWidth, 175);

        g.setFont(statsInfoFont);
        g.setColor(Color.DARK_GRAY);
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

        g.setColor(Color.DARK_GRAY);
        g.drawString("Rank: " + scoreData.rank, 20, statsY);
        statsY += lineHeight;

        g.drawString(String.format("Trinket Multiplier: %.2f", scoreData.trinketMultiplier), 20, statsY);
        statsY += lineHeight;

        g.setFont(xpFont);
        g.setColor(new Color(150, 255, 150));
        String xpText = gsm.getSelectedPlayerClass() + " Gained: +" + scoreData.xpAwarded;

        statsY += lineHeight - 18;
        g.drawString(xpText, 20, statsY);

        g.setFont(font);
        int optionsY = statsY + 20;

        for (int i = 0; i < options.length; i++) {
            if (i == currentChoice) {
                g.setColor(Color.YELLOW);
            } else {
                g.setColor(Color.BLACK);
            }
            g.drawString(options[i], 20, optionsY + i * 12);
        }
    }

    // TODO: FIX THIS
    private void select() {
        if(currentChoice == 1) {
            gsm.setState(GameStateManager.MENUSTATE);
        }
        if(currentChoice == 0) {
            System.exit(0);
        }
    }

    public void keyPressed(int k) {
        if(k == KeyEvent.VK_ENTER) {
            select();
        }
        if(k == KeyEvent.VK_UP) {
            currentChoice--;
            if(currentChoice == -1) {
                currentChoice = options.length - 1;
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

        int optionsY = scoreData != null ? (70 + (scoreData.playerDidNotDieInLevel && scoreData.finalDisplayScore > 0 ? 5 : 4) * 16 + 10) : 100;

        for (int i = 0; i < options.length; i++) {
            int itemY = optionsY + i * 12;
            int itemHeight = 12;
            int textWidth = gamePanel.getFontMetrics(font).stringWidth(options[i]);
            Rectangle itemBounds = new Rectangle(20, itemY - font.getSize(), textWidth, itemHeight);

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