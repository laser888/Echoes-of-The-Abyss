package GameState;

import Main.GamePanel;
import TileMap.Background;
import Entity.Player;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class ClassSelectionState extends GameState {

    private Background bg;
    private GamePanel gamePanel;

    private int currentChoice = 0;
    private String[] options = {
            "Mage",
            "Berserker",
            "Archer",
            "Back to Menu"
    };

    private Color titleColor;
    private Font titleFont;
    private Font optionFont;

    public ClassSelectionState(GameStateManager gsm, GamePanel gamePanel) {
        this.gsm = gsm;
        this.gamePanel = gamePanel;

        try {
            bg = new Background("/Backgrounds/menubg.gif", 0.8);
            bg.setVector(-0.05, 0);

            titleColor = new Color(220, 180, 50);
            titleFont = new Font("Century Gothic", Font.BOLD, 28);
            optionFont = new Font("Arial", Font.BOLD, 20);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void init() {
        currentChoice = 0;
    }

    public void update() {
        bg.update();
    }

    public void draw(Graphics2D g) {
        bg.draw(g);

        // Title
        g.setColor(titleColor);
        g.setFont(titleFont);
        String titleText = "Choose Your Class";
        FontMetrics fmTitle = g.getFontMetrics(titleFont);
        int titleWidth = fmTitle.stringWidth(titleText);
        g.drawString(titleText, (GamePanel.WIDTH - titleWidth) / 2, 60);

        // Options
        g.setFont(optionFont);
        FontMetrics fmOption = g.getFontMetrics(optionFont);
        int optionsStartY = 120;
        int optionSpacing = 35;

        for (int i = 0; i < options.length; i++) {
            if (i == currentChoice) {
                g.setColor(Color.YELLOW);

            } else {
                g.setColor(Color.BLACK);
            }

            int optionWidth = fmOption.stringWidth(options[i]);
            g.drawString(options[i], (GamePanel.WIDTH - optionWidth) / 2, optionsStartY + i * optionSpacing);
        }
    }

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
            gsm.setState(GameStateManager.LEVEL1STATE);
        }
    }

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

    public void keyReleased(int k) {}

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
    public int getSpawnX() {return 0;}
    public  int getSpawnY() { return 0;}
}