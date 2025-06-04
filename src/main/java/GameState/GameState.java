package GameState;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

public abstract class GameState {
    protected GameStateManager gsm;
    protected boolean isInitialized = false;

    public abstract void init();
    public abstract void update();
    public abstract void draw(Graphics2D g);
    public abstract void keyPressed(int k);
    public abstract void keyReleased(int k);
    public abstract void mousePressed(MouseEvent e);

    protected void setInitialized(boolean initialized) {
        this.isInitialized = initialized;
    }

    public boolean isReady() {
        return isInitialized;
    }

    public int getSpawnX() { return 100; }
    public int getSpawnY() { return 100; }
}