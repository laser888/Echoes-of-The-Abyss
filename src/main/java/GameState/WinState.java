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

    private Font font;
    private Font rankFont;

    private ScoreData scoreData;

    private Font bigScoreFont;
    private Font statsInfoFont;

    public WinState(GameStateManager gsm, GamePanel gamePanel) {
        this.gsm = gsm;
        this.gamePanel = gamePanel;
        try {
            bg = new Background("/Backgrounds/menubg.gif", 1);

            titleColor = new Color(0, 100, 0);
            titleFont = new Font("Century Gothic", Font.BOLD, 24);
            rankFont = new Font("Arial", Font.BOLD, 60);

            bigScoreFont = new Font("Arial", Font.BOLD, 36);
            statsInfoFont = new Font("Arial", Font.PLAIN, 12);

            font = new Font("Arial", Font.PLAIN, 10);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setScoreData(ScoreData data) {
        this.scoreData = data;
    }

    private String getRank(double score) {
        if (score >= 90) return "S+";
        else if (score >= 80) return "S";
        else if (score >= 70) return "A";
        else if (score >= 60) return "B";
        else if (score >= 50) return "C";
        else return "D";
    }

    private double getMultiplier(String rank) {
        switch (rank) {
            case "S+": return 1.2;
            case "S": return 1.0;
            case "A": return 0.8;
            case "B": return 0.64;
            case "C": return 0.512;
            case "D": return 0.41;
            default: return 1.0;
        }
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

        String rank = getRank(scoreData.finalScore);
        g.setFont(rankFont);
        g.setColor(Color.ORANGE);
        int rankWidth = g.getFontMetrics().stringWidth(rank);
        g.drawString(rank, GamePanel.WIDTH - 80 - rankWidth, 105);

        g.setFont(bigScoreFont);
        String scoreStr = String.format("%.0f", scoreData.finalScore);
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

        if (scoreData.playerDidNotDieInLevel && scoreData.finalScore > 0) {
            g.setColor(Color.BLUE);
            g.drawString("No Death Bonus: +10", 20, statsY);
            statsY += lineHeight;
        }

        g.setColor(Color.DARK_GRAY);
        g.drawString("Rank: " + rank, 20, statsY);
        statsY += lineHeight;

        g.drawString(String.format("Trinket Multiplier: %.2f", getMultiplier(rank)), 20, statsY);
        statsY += lineHeight;

        g.setFont(font);
        int optionsY = statsY + 10;

        for (int i = 0; i < options.length; i++) {
            if (i == currentChoice) {
                g.setColor(Color.YELLOW);
            } else {
                g.setColor(Color.BLACK);
            }
            g.drawString(options[i], 20, optionsY + i * 12);
        }
    }

    private void select() {
        if(currentChoice == 0) {
            gsm.setState(GameStateManager.MENUSTATE);
        }
        if(currentChoice == 1) {
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

        int optionsY = scoreData != null ? (70 + (scoreData.playerDidNotDieInLevel && scoreData.finalScore > 0 ? 5 : 4) * 16 + 10) : 100;

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
}