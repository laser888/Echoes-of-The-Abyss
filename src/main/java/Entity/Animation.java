package Entity;

import java.awt.image.BufferedImage;

// Manages sprite animations
public class Animation {
    private BufferedImage[] frames; // Animation frames
    private int currentFrame; // Current frame index
    private long startTime; // Frame start time (ns)
    private long delay; // Frame delay (ms)

    // Initializes Animation
    public Animation() {
        delay = -1;
    }

    // Sets animation frames
    public void setFrames(BufferedImage[] frames) {
        this.frames = frames;
        if (frames != null && frames.length > 0) {
            currentFrame = 0;
            startTime = System.nanoTime();
        } else {
            currentFrame = 0;
        }
    }

    // Sets frame delay
    public void setDelay(long d) {
        delay = d;
    }

    // Updates animation frame
    public void update() {
        if (frames == null || frames.length == 0 || delay == -1) return;
        long elapsed = (System.nanoTime() - startTime) / 1_000_000;
        if (elapsed > delay) {
            currentFrame++;
            startTime = System.nanoTime();
        }
        if (currentFrame >= frames.length) {
            currentFrame = 0;
        }
    }

    // Returns current frame
    public BufferedImage getImage() {
        if (frames == null || frames.length == 0) return null;
        return frames[currentFrame];
    }

    // Checks if animation played once
    public boolean hasPlayedOnce() {
        if (frames == null || frames.length == 0) return false;
        return currentFrame == frames.length - 1;
    }
}