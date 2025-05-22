package Effects;

import Main.GamePanel;
import TileMap.TileMap;

import java.awt.*;

public class DamageNumber {
    private double x, y;
    private String damageText;
    private Color color;
    private long startTime;
    private long duration;
    private double dy;
    private TileMap tileMap;

    private boolean remove = false;

    public DamageNumber(String damageText, double x, double y, Color color, TileMap tm) {

        this.damageText = damageText;
        this.x = x;
        this.y = y;
        this.color = color;
        this.tileMap = tm;

        this.startTime = System.nanoTime();
        this.duration = 1000;
        this.dy = -0.5;
    }

    public void update() {
        y += dy;

        long elapsed = (System.nanoTime() - startTime) / 1000000;

        if (elapsed > duration) {
            remove = true;
        }
    }

    public void draw(Graphics2D g) {

        double screenX = x + tileMap.getx();
        double screenY = y + tileMap.gety();

        if (screenX < -20 || screenX > GamePanel.WIDTH + 20 ||
                screenY < -20 || screenY > GamePanel.HEIGHT + 20) {
        }


        g.setFont(new Font("Arial", Font.BOLD, 10));
        g.setColor(this.color);

        long elapsed = (System.nanoTime() - startTime) / 1000000;
        float alpha = 1.0f - (float) elapsed / duration;

        if (alpha < 0) alpha = 0;
        if (alpha > 1) alpha = 1;

        AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
        g.setComposite(ac);

        g.drawString(damageText, (int) screenX, (int) screenY);

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }

    public boolean shouldRemove() {
        return remove;
    }
}