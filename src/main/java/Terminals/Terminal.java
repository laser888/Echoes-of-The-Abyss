package Terminals;

import java.awt.*;

// Base class for all terminal puzzles
public abstract class Terminal {

    protected boolean completed; // Puzzle completion state
    protected Rectangle triggerZone; // Area where player can interact

    // Starts the puzzle
    public abstract void start();

    // Updates puzzle state
    public abstract void update();

    // Renders puzzle visuals
    public abstract void render(Graphics2D g);

    // Closes the puzzle
    public abstract void close();

    // Handles mouse input
    public abstract void mousePressed(int x, int y);

    // Returns trigger zone
    public Rectangle getTriggerZone() { return triggerZone; }

    // Returns completion state
    public boolean isCompleted() { return completed; }
}
