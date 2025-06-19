package Effects;

import Main.GamePanel;
import TileMap.TileMap;

import java.awt.*;

// Shows damage numbers above enemies
public class DamageNumber {
    private double x, y; // Screen position
    private String damageText; // Damage value to display
    private Color colour; // Text colour
    private long startTime; // Start time for fading
    private long duration; // Display duration (ms)
    private double dy; // Vertical movement speed
    private TileMap tileMap; // Tile map for positioning
    private boolean remove; // Flags for removal

    // Creates a new damage number
    public DamageNumber(String damageText, double x, double y, Color color, TileMap tm) {
        this.damageText = (damageText != null) ? damageText : "0"; // Defaults to "0" if null
        this.x = x;
        this.y = y;
        this.colour = (color != null) ? color : Color.WHITE; // Defaults to white if null
        this.tileMap = tm;
        this.startTime = System.nanoTime();
        this.duration = 1000; // Sets 1 second duration
        this.dy = -0.5; // Moves upward
        this.remove = false;
    }

    // Updates damage number position and fade
    public void update() {
        y += dy; // Moves number upward

        // Marks for removal after duration
        long elapsed = (System.nanoTime() - startTime) / 1_000_000;
        if (elapsed > duration) {
            remove = true;
        }
    }

    // Draws damage number on screen
    public void draw(Graphics2D g) {
        if (tileMap == null || g == null) {
            return; // Skips rendering if invalid
        }

        // Calculates screen position
        double screenX = x + tileMap.getx();
        double screenY = y + tileMap.gety();

        // Skips if off-screen
        if (screenX < -20 || screenX > GamePanel.WIDTH + 20 ||
                screenY < -20 || screenY > GamePanel.HEIGHT + 20) {
            return;
        }

        // Sets font and color
        g.setFont(new Font("Arial", Font.BOLD, 10));
        g.setColor(colour);

        // Fades out over time
        long elapsed = (System.nanoTime() - startTime) / 1_000_000;
        float alpha = 1.0f - (float) elapsed / Math.max(duration, 1); // Prevents division by zero
        alpha = Math.max(0, Math.min(1, alpha)); // Clamps alpha

        AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
        g.setComposite(ac);

        // Renders text
        g.drawString(damageText, (int) screenX, (int) screenY);

        // Resets composite
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }

    // Checks if damage number should be removed
    public boolean shouldRemove() {
        return remove;
    }
}