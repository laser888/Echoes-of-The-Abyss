package TileMap;

import GameState.BaseLevelState;
import Terminals.SimonSays;
import Main.GamePanel;

import java.awt.*;
import java.awt.image.BufferedImage;

// Handles puzzle interaction at a terminal location
public class TerminalTile {
    private SimonSays simon; // Simon Says puzzle
    private int px, py; // Pixel position
    private int tileId; // Tile index for rendering
    private TileMap tileMap; // Reference to tile map
    private Font font; // Font for prompt text
    private int tileX; // Grid X coordinate
    private int tileY; // Grid Y coordinate
    private static final int INTERACTION_RADIUS_IN_TILES = 2; // Interaction range
    private boolean blessingGiven = false; // Whether reward has been given
    private boolean isGhost; // Whether this is a visual-only terminal

    // Initializes TerminalTile
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

    // Overload: creates non-ghost terminal
    public TerminalTile(int px, int py, int tileId, TileMap tileMap, GamePanel gp, int tileX, int tileY) {
        this(px, py, tileId, tileMap, gp, tileX, tileY, false);
    }

    // Updates Simon Says puzzle
    public void update() {
        simon.update();
    }

    // Renders terminal or puzzle
    public void render(Graphics2D g, int camX, int camY) {
        if (isGhost && !simon.isActive()) return;

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

    // Draws "Press E" prompt
    public void drawPressEPrompt(Graphics2D g, int camX, int camY) {
        if (simon.isActive() || simon.isCompleted()) return;

        int drawX = px + camX;
        int drawY = py + camY - 20;
        int size = tileMap.getTileSize();

        g.setFont(font);
        g.setColor(isGhost ? new Color(255, 255, 255, 128) : Color.WHITE);

        String text = "Press E";
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(text);

        g.drawString(text, drawX + (size - textWidth) / 2, drawY);
    }

    // Starts puzzle interaction
    public void interact() {
        if (!simon.isCompleted() && !simon.isPuzzleSolved()) {
            simon.start();
            BaseLevelState.inTerminal = true;
        }
    }

    // Closes puzzle if active
    public void close() {
        if (simon.isActive()) {
            simon.close();
            BaseLevelState.inTerminal = false;
        }
    }

    // Returns true if puzzle is active
    public boolean isActive() { return simon.isActive(); }

    // Returns true if puzzle is finished
    public boolean isCompleted() { return simon.isCompleted(); }

    // Returns true if puzzle was solved
    public boolean isSolved() { return simon.isPuzzleSolved(); }

    // Marks puzzle as solved
    public void markSolved() {
        simon.setPuzzleSolved(true);
        BaseLevelState.inTerminal = false;
    }

    // Handles puzzle mouse press
    public void mousePressed(int x, int y) {
        simon.mousePressed(x, y);
    }

    // Returns pixel position
    public Point getPos() {
        return new Point(px, py);
    }

    // Checks if player is nearby
    public boolean playerNearby(int playerX, int playerY) {
        int radius = tileMap.getTileSize() * INTERACTION_RADIUS_IN_TILES;
        Rectangle tz = new Rectangle(
                px - radius / 2 + tileMap.getTileSize() / 2,
                py - radius / 2 + tileMap.getTileSize() / 2,
                radius,
                radius
        );
        return tz.contains(playerX, playerY);
    }

    // Returns grid X
    public int getX() { return tileX; }

    // Returns grid Y
    public int getY() { return tileY; }

    // Returns whether blessing was given
    public boolean isBlessingGiven() { return blessingGiven; }

    // Marks blessing as given
    public void setBlessingGiven() { this.blessingGiven = true; }
}
