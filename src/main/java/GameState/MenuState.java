package GameState;

import Main.GamePanel;
import TileMap.Background;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class MenuState extends GameState {

    private Background bg;
    private GamePanel gamePanel;

    private int currentChoice = 0;
    private String[] options = { "Start", "Settings", "Quit"};

    private Color titleColor;
    private Font titleFont;

    private Font font;

    public MenuState(GameStateManager gsm, GamePanel gamePanel) {
        this.gsm = gsm;
        this.gamePanel = gamePanel;

        try {
            bg = new Background("/Backgrounds/titlescreen.gif", 1);
            //bg.setVector(-0.1, 0);

            titleColor = new Color(128, 0, 0);
            titleFont = new Font("Century Gothic", Font.PLAIN, 28);

            font = new Font("Arial", Font.PLAIN, 12);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void init() {}

    public void update() {
        bg.update();
    }

    public void draw(java.awt.Graphics2D g) {
        // draw bg
        bg.draw(g);

        // draw title
        g.setColor(titleColor);
        g.setFont(titleFont);
        g.drawString("Echoes of the Abyss", 40, 70);

        // draw menu options
        g.setFont(font);
        for(int i = 0; i < options.length; i++) {
            if(i == currentChoice) {
                g.setColor(Color.WHITE);
            } else {
                g.setColor(Color.RED);
            }
            g.drawString(options[i], 145, 140 + i * 15);
        }
    }

    private void select() {
        if(currentChoice == 0) {
            gsm.setState(GameStateManager.CLASSSELECTIONSTATE);
        }
        if(currentChoice == 1) {
            gsm.setState(GameStateManager.SETTINGSSTATE);
        }
        if(currentChoice == 2) {
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