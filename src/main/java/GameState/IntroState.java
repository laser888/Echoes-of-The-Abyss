package GameState;

import TileMap.Background;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class IntroState extends GameState {

    private Background bg;
    private Font mainFont;
    private Font promptFont;

    private String[] lines = {
            "Welcome to the dungeon. Fight enemies as you move through each room.",
            "Solve terminals to earn blessings and boost your stats.",
            "Your score determines your final rank — higher rank, better rewards.",
            "Defeat the boss at the end of each floor to progress."
    };

    private long startTime;
    private boolean allowSkip = false;

    public IntroState(GameStateManager gsm) {
        this.gsm = gsm;
        try {
            bg = new Background("/Backgrounds/titlescreen.gif", 1);
            mainFont = new Font("Arial", Font.PLAIN, 14);
            promptFont = new Font("Arial", Font.BOLD, 12);
            startTime = System.currentTimeMillis();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void init() {
        startTime = System.currentTimeMillis();
    }

    public void update() {
        bg.update();
        if (!allowSkip && System.currentTimeMillis() - startTime >= 5000) {
            allowSkip = true;
        }
    }

    public void draw(Graphics2D g) {
        bg.draw(g);
        g.setFont(mainFont);
        g.setColor(Color.WHITE);

        int panelWidth = 320;
        int x = 20;
        int y = 50;
        int lineHeight = 18;

        // Draw wrapped lines
        for (String paragraph : lines) {
            for (String line : wrapText(paragraph, g, panelWidth - 40)) {
                g.drawString(line, x, y);
                y += lineHeight;
            }
            y += 10;
        }

        if (allowSkip) {
            g.setFont(promptFont);
            g.setColor(Color.LIGHT_GRAY);
            g.drawString("Press ENTER to continue...", 100, 205);
        }
    }

    public void keyPressed(int k) {
        if (allowSkip && k == KeyEvent.VK_ENTER) {
            gsm.setState(GameStateManager.LEVEL1STATE);
        }
    }

    public void keyReleased(int k) {}

    @Override
    public void mousePressed(MouseEvent e) {}

    // Text wrapping helper
    private java.util.List<String> wrapText(String text, Graphics2D g, int maxWidth) {
        java.util.List<String> lines = new java.util.ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();

        for (String word : words) {
            String testLine = line.length() == 0 ? word : line + " " + word;
            int width = g.getFontMetrics().stringWidth(testLine);
            if (width > maxWidth) {
                lines.add(line.toString());
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(testLine);
            }
        }
        if (line.length() > 0) {
            lines.add(line.toString());
        }
        return lines;
    }
}