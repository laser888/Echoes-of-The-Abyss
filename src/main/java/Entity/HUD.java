package Entity;

import Main.GamePanel;
import GameState.Level1State;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;

import static GameState.Level1State.typedText;

public class HUD {

    private Player player;

    private BufferedImage image;
    private Font font;
    private static boolean debug = false;

    public static void toggleDebug() { debug = !debug; }

    public HUD(Player p) {
        player = p;
        try {

            image = ImageIO.read(getClass().getResourceAsStream("/HUD/hud.gif"));
            font = new Font("Arial", Font.PLAIN, 14);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void draw(Graphics2D g) {

        g.drawImage(image, 0, 10, null);
        g.setFont(font);
        g.setColor(Color.WHITE);
        g.drawString(player.getHealth() + "/" + player.getMaxHealth(), 35, 25);
        g.drawString(player.getFire() / 100 + "/" + player.getMaxFire() / 100, 30,45);

        if(debug) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 16));
            g.drawString("X: " + player.getx(), 80, 20);
            g.drawString("Y: " + player.gety(), 80, 50);
        }

        g.setColor(Color.BLUE);
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.drawString("FPS: " + GamePanel.getFPS(), 269, 20);

        if(Level1State.isTyping) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 215, 999, 25);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 14));
            g.drawString("<Player>: " + typedText.toString(), 5, 230);
        }

    }

}
