package Entity;

import Entity.Enemies.Bosses.CloneBoss;
import Main.GamePanel;
import GameState.BaseLevelState;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;

import static GameState.Level1State.typedText;

public class HUD {

    private Player player;
    private BaseLevelState levelState;
    private BufferedImage image;
    private Font font;

    // Boss health bar
    private Enemy activeBoss;
    private double displayedBossHealth;
    private long lastHealthUpdateTime;
    private static final double HEALTH_ANIMATION_SPEED = 0.15;
    private static final int BAR_WIDTH = 170;
    private static final int BAR_HEIGHT = 14;
    private static final int BAR_X = (GamePanel.WIDTH - BAR_WIDTH) / 2;
    private static final int BAR_Y = 26;

    private static boolean debug = false;

    public static void toggleDebug() { debug = !debug; }

    public HUD(Player p, BaseLevelState levelState) {
        this.player = p;
        this.levelState = levelState;
        try {
            image = ImageIO.read(getClass().getResourceAsStream("/HUD/hud.gif"));
            font = new Font("Arial", Font.PLAIN, 14);
        } catch (Exception e) {
            e.printStackTrace();
        }
        displayedBossHealth = 0;
        lastHealthUpdateTime = System.nanoTime();
    }

    public void draw(Graphics2D g) {
        // Draw player HUD
        g.drawImage(image, 0, 10, null);
        g.setFont(font);
        g.setColor(Color.BLACK);
        g.drawString(player.getHealth() + "/" + player.getMaxHealth(), 17, 25);
        g.drawString(Integer.toString(player.getDefence()), 20, 45);
        g.drawString(player.getIntelligence() + "/" + player.getMaxIntelligence(), 17, 67);

        if (debug) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 16));
            g.drawString("X: " + player.getx(), 80, 20);
            g.drawString("Y: " + player.gety(), 80, 50);
        }

        g.setColor(Color.PINK);
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.drawString("FPS: " + GamePanel.getFPS(), 269, 20);

        if (GameState.Level1State.isTyping) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 215, 999, 25);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 14));
            g.drawString("<Player>: " + typedText.toString(), 5, 230);
        }

        drawBossHealthBar(g);
    }

    private void drawBossHealthBar(Graphics2D g) {

        activeBoss = null;
        if (levelState.getEntityManager() != null) {
            for (Enemy enemy : levelState.getEntityManager().getEnemies()) {
                if (enemy.isBoss() && !enemy.isDead()) {
                    if(enemy instanceof CloneBoss && enemy.isDead()) {
                        activeBoss = enemy;
                        break;
                    }
                    activeBoss = enemy;
                    break;
                }
            }
        }

        if (activeBoss == null) {
            displayedBossHealth = 0;
            return;
        }

        // Animate health
        long currentTime = System.nanoTime();
        double elapsedSeconds = (currentTime - lastHealthUpdateTime) / 1_000_000_000.0;
        lastHealthUpdateTime = currentTime;

        double targetHealth = activeBoss.getHealth();
        if (displayedBossHealth > targetHealth) {
            displayedBossHealth -= (displayedBossHealth - targetHealth) * HEALTH_ANIMATION_SPEED * elapsedSeconds * 60;
            if (displayedBossHealth < targetHealth) {
                displayedBossHealth = targetHealth;
            }
        } else {
            displayedBossHealth = targetHealth;
        }

        // Background
        g.setColor(new Color(30, 30, 30));
        g.fillRoundRect(BAR_X, BAR_Y, BAR_WIDTH, BAR_HEIGHT, 8, 8);

        int healthWidth = (int) ((displayedBossHealth / activeBoss.getMaxHealth()) * BAR_WIDTH);
        GradientPaint gp = new GradientPaint(
                BAR_X, BAR_Y, new Color(220, 50, 50),
                BAR_X + healthWidth, BAR_Y + BAR_HEIGHT, new Color(150, 0, 0)
        );
        g.setPaint(gp);
        g.fillRoundRect(BAR_X, BAR_Y, healthWidth, BAR_HEIGHT, 8, 8);

        // Border
        g.setColor(new Color(255, 70, 70));
        g.drawRoundRect(BAR_X, BAR_Y, BAR_WIDTH, BAR_HEIGHT, 8, 8);

        // Boss name
        String bossName = activeBoss.getName();
        g.setFont(new Font("Arial", Font.BOLD, 15));
        g.setColor(Color.WHITE);
        FontMetrics fm = g.getFontMetrics();
        int nameWidth = fm.stringWidth(bossName);
        g.drawString(bossName, BAR_X + (BAR_WIDTH - nameWidth) / 2, BAR_Y - 6);

        // Health text
        String healthText = (int) displayedBossHealth + " / " + activeBoss.getMaxHealth();
        g.setFont(new Font("Arial", Font.PLAIN, 11));
        fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(healthText);
        g.drawString(healthText, BAR_X + (BAR_WIDTH - textWidth) / 2, BAR_Y + BAR_HEIGHT / 2 + fm.getAscent() / 2 - 1);
    }

}