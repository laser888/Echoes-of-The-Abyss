package Entity;

import java.awt.image.BufferedImage;

public class Animation {
    private BufferedImage[] frames;
    private int currentFrame;
    private long startTime;
    private long delay;

    public Animation() {
        frames = null;
        currentFrame = 0;
        delay = -1;
    }

    public void setFrames(BufferedImage[] frames) {
        this.frames = frames;
        if (frames != null && frames.length > 0) {
            currentFrame = 0;
            startTime = System.nanoTime();
        }
    }

    public void setDelay(long d) { delay = d; }

    public void update() {
        if (frames == null || delay == -1 || frames.length == 0) return;
        long elapsed = (System.nanoTime() - startTime) / 1_000_000;
        if (elapsed > delay) {
            currentFrame++;
            startTime = System.nanoTime();
        }
        if (currentFrame == frames.length) {
            currentFrame = 0;
        }
    }

    public BufferedImage getImage() {
        if (frames == null || frames.length == 0) return null;
        return frames[currentFrame];
    }

    public boolean hasPlayedOnce() {
        if (frames == null || frames.length == 0) return false;
        return currentFrame == frames.length - 1;
    }
}