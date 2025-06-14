package Terminals;

import java.awt.*;

public abstract class Terminal {

    protected boolean completed;
    protected Rectangle triggerZone;

    public abstract void start();
    public abstract void update();
    public abstract void render(Graphics2D g);
    public abstract void close();
    public abstract void mousePressed(int x, int y);
    public Rectangle getTriggerZone() {return triggerZone;}
    public boolean isCompleted() {return completed;}


}
