package Entity;

import Main.GamePanel;
import TileMap.TileMap;
import TileMap.Tile;

import java.awt.*;

// Manages map-based object behavior
public abstract class MapObject {
    protected TileMap tileMap; // Tile map reference
    protected int tileSize; // Tile size (pixels)
    protected double xmap; // Map offset X
    protected double ymap; // Map offset Y
    protected double x; // World X position
    protected double y; // World Y position
    protected double dx; // X velocity
    protected double dy; // Y velocity
    protected int width; // Sprite width
    protected int height; // Sprite height
    protected int cwidth; // Collision width
    protected int cheight; // Collision height
    protected int currRow; // Current tile row
    protected int currCol; // Current tile column
    protected double xdest; // Destination X
    protected double ydest; // Destination Y
    protected double xtemp; // Temporary X
    protected double ytemp; // Temporary Y
    protected boolean topLeft; // Top-left collision
    protected boolean topRight; // Top-right collision
    protected boolean bottomLeft; // Bottom-left collision
    protected boolean bottomRight; // Bottom-right collision
    protected Animation animation; // Object animation
    protected int currentAction; // Current animation action
    protected int previousAction; // Previous animation action
    public boolean facingRight; // Facing direction
    protected boolean left; // Moving left
    protected boolean right; // Moving right
    protected boolean up; // Moving up
    protected boolean down; // Moving down
    protected boolean jumping; // Jumping state
    protected boolean falling; // Falling state
    protected double moveSpeed; // Movement speed
    protected double maxSpeed; // Maximum speed
    protected double stopSpeed; // Deceleration speed
    protected double fallSpeed; // Fall speed
    protected double maxFallSpeed; // Maximum fall speed
    protected double jumpStart; // Jump velocity
    protected double stopJumpSpeed; // Jump deceleration
    protected boolean outOfMap; // Out-of-map flag

    // Initializes MapObject
    public MapObject(TileMap tm) {
        tileMap = tm;
        tileSize = tileMap.getTileSize();
    }

    // Checks collision with another object
    public boolean intersects(MapObject o) {
        if (o == null) return false;
        return getRectangle().intersects(o.getRectangle());
    }

    // Returns collision rectangle
    public Rectangle getRectangle() {
        return new Rectangle((int)x - cwidth / 2, (int)y - cheight / 2, cwidth, cheight);
    }

    // Calculates collision corners
    public void calculateCorners(double x, double y) {
        if (tileMap == null) {
            outOfMap = true;
            return;
        }
        int tileSize = tileMap.getTileSize();
        int numRows = tileMap.getNumRows();
        int numCols = tileMap.getNumCols();
        int leftTile = (int)(x - cwidth / 2) / tileSize;
        int rightTile = (int)(x + cwidth / 2 - 1) / tileSize;
        int topTile = (int)(y - cheight / 2) / tileSize;
        int bottomTile = (int)(y + cheight / 2 - 1) / tileSize;
        // Marks out of map
        if (bottomTile >= numRows) {
            outOfMap = true;
            return;
        }
        if (topTile < 0) {
            outOfMap = false;
            topLeft = false;
            topRight = false;
            bottomLeft = false;
            bottomRight = false;
            return;
        }
        // Handles left/right boundaries
        if (leftTile < 0 || rightTile >= numCols) {
            outOfMap = false;
            topLeft = leftTile < 0 ? true : tileMap.getType(topTile, leftTile) == Tile.BLOCKED;
            topRight = rightTile >= numCols ? true : tileMap.getType(topTile, rightTile) == Tile.BLOCKED;
            bottomLeft = leftTile < 0 ? true : tileMap.getType(bottomTile, leftTile) == Tile.BLOCKED;
            bottomRight = rightTile >= numCols ? true : tileMap.getType(bottomTile, rightTile) == Tile.BLOCKED;
            return;
        }
        outOfMap = false;
        // Clamps tile indices
        topTile = Math.max(0, Math.min(topTile, numRows - 1));
        bottomTile = Math.max(0, Math.min(bottomTile, numRows - 1));
        leftTile = Math.max(0, Math.min(leftTile, numCols - 1));
        rightTile = Math.max(0, Math.min(rightTile, numCols - 1));
        topLeft = tileMap.getType(topTile, leftTile) == Tile.BLOCKED;
        topRight = tileMap.getType(topTile, rightTile) == Tile.BLOCKED;
        bottomLeft = tileMap.getType(bottomTile, leftTile) == Tile.BLOCKED;
        bottomRight = tileMap.getType(bottomTile, rightTile) == Tile.BLOCKED;
    }

    // Checks tile map collision
    public void checkTileMapCollision() {
        if (tileMap == null) return;
        currCol = (int)x / tileSize;
        currRow = (int)y / tileSize;
        xdest = x + dx;
        ydest = y + dy;
        xtemp = x;
        ytemp = y;
        // Checks vertical movement
        calculateCorners(x, ydest);
        if (outOfMap) return;
        if (dy < 0) {
            if (topLeft || topRight) {
                dy = 0;
                ytemp = currRow * tileSize + cheight / 2;
            } else {
                ytemp += dy;
            }
        }
        if (dy > 0) {
            if (bottomLeft || bottomRight) {
                dy = 0;
                falling = false;
                ytemp = (currRow + 1) * tileSize - cheight / 2;
            } else {
                ytemp += dy;
            }
        }
        // Checks horizontal movement
        calculateCorners(xdest, y);
        if (outOfMap) return;
        if (dx < 0) {
            if (topLeft || bottomLeft) {
                dx = 0;
                xtemp = currCol * tileSize + cwidth / 2;
            } else {
                xtemp += dx;
            }
        }
        if (dx > 0) {
            if (topRight || bottomRight) {
                dx = 0;
                xtemp = (currCol + 1) * tileSize - cwidth / 2;
            } else {
                xtemp += dx;
            }
        }
        // Checks falling state
        if (!falling) {
            calculateCorners(x, ydest + 1);
            if (outOfMap) return;
            if (!bottomLeft && !bottomRight) {
                falling = true;
            }
        }
        // Clamps X position
        int minX = cwidth / 2;
        int maxX = tileMap.getWidth() - cwidth / 2;
        xtemp = Math.max(minX, Math.min(xtemp, maxX));
        if (xtemp == minX || xtemp == maxX) dx = 0;
    }

    // Gets X position
    public int getx() {
        return (int)x;
    }

    // Gets Y position
    public int gety() {
        return (int)y;
    }

    // Gets width
    public int getWidth() {
        return width;
    }

    // Gets height
    public int getHeight() {
        return height;
    }

    // Gets collision width
    public int getCWidth() {
        return cwidth;
    }

    // Gets collision height
    public int getCHeight() {
        return cheight;
    }

    // Sets position
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    // Sets velocity
    public void setVector(double dx, double dy) {
        this.dx = dx;
        this.dy = dy;
    }

    // Sets map offset
    public void setMapPosition() {
        if (tileMap == null) return;
        xmap = tileMap.getx();
        ymap = tileMap.gety();
    }

    // Sets left movement
    public void setLeft(boolean b) {
        left = b;
    }

    // Sets right movement
    public void setRight(boolean b) {
        right = b;
    }

    // Sets up movement
    public void setUp(boolean b) {
        up = b;
    }

    // Sets down movement
    public void setDown(boolean b) {
        down = b;
    }

    // Sets jumping state
    public void setJumping(boolean b) {
        jumping = b;
    }

    // Checks if object is off screen
    public boolean notOnScreen() {
        return x + xmap + width < 0 || x + xmap - width > GamePanel.WIDTH ||
                y + ymap + height < 0 || y + ymap - height > GamePanel.HEIGHT;
    }

    // Draws object
    public void draw(Graphics2D g) {
        if (g == null || animation == null || animation.getImage() == null) return;
        if (facingRight) {
            g.drawImage(animation.getImage(), (int)(x + xmap - width / 2), (int)(y + ymap - height / 2), null);
        } else {
            g.drawImage(animation.getImage(), (int)(x + xmap - width / 2 + width), (int)(y + ymap - height / 2), -width, height, null);
        }
    }
}