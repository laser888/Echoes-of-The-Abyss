package GameState;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

// Base class for all game states (e.g. levels, menus)
public abstract class GameState {
    protected GameStateManager gsm; // Reference to manager
    protected boolean isInitialized = false; // Tracks if state has been initialized

    // Initializes game state
    public abstract void init();

    // Updates game state logic
    public abstract void update();

    // Renders game state
    public abstract void draw(Graphics2D g);

    // Handles key press
    public abstract void keyPressed(int k);

    // Handles key release
    public abstract void keyReleased(int k);

    // Handles mouse press
    public abstract void mousePressed(MouseEvent e);

    // Sets initialization state
    protected void setInitialized(boolean initialized) {
        this.isInitialized = initialized;
    }

    // Returns player spawn X coordinate
    public int getSpawnX() { return 100; }

    // Returns player spawn Y coordinate
    public int getSpawnY() { return 100; }
}
