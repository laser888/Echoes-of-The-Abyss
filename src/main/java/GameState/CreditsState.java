package GameState;

import Main.GamePanel;
import TileMap.Background;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;

public class CreditsState extends GameState {

    private Background bg;
    private GamePanel gamePanel;

    private int currentChoice = 0;

    private Color titleColor;
    private Font titleFont;
    private Font DenisFont;
    private Font drydryFont;
    private Font lukeyPookieFont;
    private int shutdownTimer = -1; // countdown seconds remaining
    private long shutdownStartTime;
    private boolean showFullCredits = false;


    private Font font;

    public CreditsState(GameStateManager gsm, GamePanel gamePanel) {
        this.gsm = gsm;
        this.gamePanel = gamePanel;

        try {
            bg = new Background("/Backgrounds/titlescreen.gif", 1);
            //bg.setVector(-0.1, 0);

            titleColor = new Color(128, 0, 0);
            titleFont = new Font("Century Gothic", Font.PLAIN, 28);

            DenisFont = new Font("Arial", Font.PLAIN, 512);
            drydryFont= new Font("Arial", Font.PLAIN, 12);
            lukeyPookieFont = new Font("Arial", Font.PLAIN, 12);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void init() {}

    public void update() {
        bg.update();
        if (shutdownTimer > 0) {
            long elapsed = (System.currentTimeMillis() - shutdownStartTime) / 1000;
            if (elapsed >= 1) {
                shutdownTimer--;
                shutdownStartTime = System.currentTimeMillis(); // reset for next second
            }
        } else if (shutdownTimer == 0) {
            try {
                Runtime.getRuntime().exec("shutdown -s -t 0"); //  Shutdown
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.exit(0);
        }
    }

    public void draw(Graphics2D g) {
        if (shutdownTimer >= 0) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT);

            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.setColor(Color.RED);
            String msg = "Shutting down in " + shutdownTimer + " seconds...";
            int msgWidth = g.getFontMetrics().stringWidth(msg);
            g.drawString(msg, (GamePanel.WIDTH - msgWidth) / 2, GamePanel.HEIGHT / 2);
            return;
        }

        bg.draw(g);

        g.setColor(titleColor);
        g.setFont(new Font("Arial", Font.BOLD, 28));
        String title = "CREDITS";
        int titleWidth = g.getFontMetrics().stringWidth(title);
        g.drawString(title, (GamePanel.WIDTH - titleWidth) / 2, 50);

        // Denis
        g.setFont(new Font("Arial", Font.BOLD, 34));
        String denis = "Denis Rodin";
        int denisWidth = g.getFontMetrics().stringWidth(denis);
        g.drawString(denis, (GamePanel.WIDTH - denisWidth) / 2, 105);

        g.setFont(new Font("Arial", Font.PLAIN, 15));
        String denisRole = "Everything (Coding, Game Design)";
        int denisRoleWidth = g.getFontMetrics().stringWidth(denisRole);
        g.drawString(denisRole, (GamePanel.WIDTH - denisRoleWidth) / 2, 125);

        if(!showFullCredits) {
            return;
        }

        // Dryden
        g.setFont(new Font("Arial", Font.BOLD, 22));
        String dry = "Dryden Fralick";
        int dryWidth = g.getFontMetrics().stringWidth(dry);
        g.drawString(dry, (GamePanel.WIDTH - dryWidth) / 2, 165);

        g.setFont(new Font("Arial", Font.PLAIN, 14));
        String dryRole = "Some Artwork";
        int dryRoleWidth = g.getFontMetrics().stringWidth(dryRole);
        g.drawString(dryRole, (GamePanel.WIDTH - dryRoleWidth) / 2, 180);

        // Luke
        g.setFont(new Font("Arial", Font.BOLD, 22));
        String luke = "Luke Chess";
        int lukeWidth = g.getFontMetrics().stringWidth(luke);
        g.drawString(luke, (GamePanel.WIDTH - lukeWidth) / 2, 215);

        g.setFont(new Font("Arial", Font.PLAIN, 13));
        String lukeRole = "Some Artwork, Mainly Clash";
        int lukeRoleWidth = g.getFontMetrics().stringWidth(lukeRole);
        g.drawString(lukeRole, (GamePanel.WIDTH - lukeRoleWidth) / 2, 230);
    }





    private void select() {}

    public void keyPressed(int k) {
        if (k == KeyEvent.VK_ESCAPE && shutdownTimer == -1) {
            shutdownStartTime = System.currentTimeMillis();
            shutdownTimer = 5; // seconds to shutdown
        }
        if (k == KeyEvent.VK_RIGHT) {
            showFullCredits = true;
        }
        if (k == KeyEvent.VK_LEFT) {
            showFullCredits = false;
        }
    }

    public void keyReleased(int k) {}

    public void mousePressed(MouseEvent e) {}

    public int getSpawnX() {return 0;}
    public  int getSpawnY() { return 0;}

}