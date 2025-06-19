package TileMap;

import GameState.BaseLevelState;
import Terminals.SimonSays;
import Main.GamePanel;

import java.awt.*;
import java.awt.image.BufferedImage;

public class TerminalTile {
    private SimonSays simon;
    private int px, py;
    private int tileId;
    private TileMap tileMap;
    private Font font;
    private int tileX;
    private int tileY;
    private static final int INTERACTION_RADIUS_IN_TILES = 2;
    private boolean blessingGiven = false;
    private boolean isGhost;

    public TerminalTile(int px, int py, int tileId, TileMap tileMap, GamePanel gp, int tileX, int tileY, boolean isGhost) {
        this.px = px;
        this.py = py;
        this.tileX = tileX;
        this.tileY = tileY;
        this.tileId = tileId;
        this.tileMap = tileMap;
        this.isGhost = isGhost;
        int radius = tileMap.getTileSize() * INTERACTION_RADIUS_IN_TILES;
        simon = new SimonSays(px, py, gp, tileMap.getTileSize());
        font = new Font("Arial", Font.BOLD, 12);
        if (isGhost) {
            this.blessingGiven = true;
        }
    }

    public TerminalTile(int px, int py, int tileId, TileMap tileMap, GamePanel gp, int tileX, int tileY) {
        this(px, py, tileId, tileMap, gp, tileX, tileY, false);
    }

    public void update() {
        simon.update();
    }

    public void render(Graphics2D g, int camX, int camY) {
        if (isGhost && !simon.isActive()) {
            return;
        }
        int drawX = px + camX;
        int drawY = py + camY;
        int size = tileMap.getTileSize();

        if (!simon.isActive()) {
            BufferedImage terminalImage = tileMap.getTileImage(tileId);
            if (terminalImage != null) {
                g.drawImage(terminalImage, drawX, drawY, null);
            }
        } else {
            simon.render(g);
        }
    }

    public void drawPressEPrompt(Graphics2D g, int camX, int camY) {
        if (simon.isActive() || simon.isCompleted()) {
            return;
        }
        int drawX = px + camX;
        int drawY = py + camY - 20;
        int size = tileMap.getTileSize();

        g.setFont(font);
        g.setColor(isGhost ? new Color(255, 255, 255, 128) : Color.WHITE);

        String text = "Press E";
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();

        g.drawString(text, drawX + (size - textWidth)/2, drawY);
    }

    public void interact() {
        if (!simon.isCompleted() && !simon.isPuzzleSolved()) {
            simon.start();
            BaseLevelState.inTerminal = true;
        }
    }

    public void close() {
        if (simon.isActive()) {
            simon.close();
            BaseLevelState.inTerminal = false;
        } else {
        }
    }

    public boolean isActive() { return simon.isActive(); }
    public boolean isCompleted() { return simon.isCompleted(); }
    public boolean isSolved() { return simon.isPuzzleSolved(); }
    public void markSolved() { simon.setPuzzleSolved(true); BaseLevelState.inTerminal = false;}
    public void mousePressed(int x, int y) { simon.mousePressed(x, y); }
    public Point getPos() { return new Point(px, py); }

    public boolean playerNearby(int playerX, int playerY) {
        int radius = tileMap.getTileSize() * INTERACTION_RADIUS_IN_TILES;
        Rectangle tz = new Rectangle(
                px - radius/2 + tileMap.getTileSize()/2,
                py - radius/2 + tileMap.getTileSize()/2,
                radius,
                radius
        );
        return tz.contains(playerX, playerY);
    }

    public int getX() { return tileX; }
    public int getY() { return tileY; }
    public boolean isBlessingGiven() { return blessingGiven; }
    public void setBlessingGiven() { this.blessingGiven = true; }
}