package Entity;

import Main.GamePanel;
import TileMap.TileMap;
import TileMap.Tile;

import java.awt.*;
import java.util.ArrayList;
import java.awt.image.BufferedImage;

public abstract class MapObject {

    protected ArrayList<BufferedImage[]> sprites;

    // tile stuff
    protected TileMap tileMap;
    protected int tileSize;
    protected double xmap;
    protected double ymap;

    // position and vector
    protected double x;
    protected double y;
    protected double dx;
    protected double dy;

    // dimensions
    protected int width;
    protected int height;

    // collision box
    protected int cwidth;
    protected int cheight;

    // collision
    protected int currRow;
    protected int currCol;
    protected double xdest;
    protected double ydest;
    protected double xtemp;
    protected double ytemp;
    protected boolean topLeft;
    protected boolean topRight;
    protected boolean bottomLeft;
    protected boolean bottomRight;

    // animation
    protected Animation animation;
    protected int currentAction;
    protected int previousAction;
    public boolean facingRight;

    // movement
    protected boolean left;
    protected boolean right;
    protected boolean up;
    protected boolean down;
    protected boolean jumping;
    protected boolean falling;

    // movement attributes
    protected double moveSpeed;
    protected double maxSpeed;
    protected double stopSpeed;
    protected double fallSpeed;
    protected double maxFallSpeed;
    protected double jumpStart;
    protected double stopJumpSpeed;

    protected boolean outOfMap;

    // constructor
    public MapObject(TileMap tm) {
        tileMap = tm;
        tileSize = tm.getTileSize();
    }

    public boolean intersects(MapObject o) {
        Rectangle r1 = getRectangle();
        Rectangle r2 = o.getRectangle();
        return r1.intersects(r2);
    }

    public Rectangle getRectangle() {
        return new Rectangle((int) x - cwidth, (int) y - cheight, cwidth, cheight);
    }

    public void calculateCorners(double x, double y) {
        int tileSize = tileMap.getTileSize();
        int numRows = tileMap.getNumRows();
        int numCols = tileMap.getNumCols();

        int leftTile = (int) (x - cwidth / 2) / tileSize;
        int rightTile = (int) (x + cwidth / 2 - 1) / tileSize;
        int topTile = (int) (y - cheight / 2) / tileSize;
        int bottomTile = (int) (y + cheight / 2 - 1) / tileSize;

        // If out of bounds (below map), respawn
        if (bottomTile >= numRows) {
            outOfMap = true;
            return;

        } else if (topTile < 0) {
            outOfMap = false;
            topLeft = false;
            topRight = false;
            bottomLeft = false;
            bottomRight = false;
            return;

        } else if (leftTile < 0 || rightTile >= numCols) {
            outOfMap = false;
            topLeft = leftTile < 0 ? true : tileMap.getType(topTile, leftTile) == Tile.BLOCKED;
            topRight = rightTile >= numCols ? true : tileMap.getType(topTile, rightTile) == Tile.BLOCKED;
            bottomLeft = leftTile < 0 ? true : tileMap.getType(bottomTile, leftTile) == Tile.BLOCKED;
            bottomRight = rightTile >= numCols ? true : tileMap.getType(bottomTile, rightTile) == Tile.BLOCKED;
            return;
        }

        outOfMap = false;

        // Clamp tiles for tile access
        topTile = Math.max(0, Math.min(topTile, numRows - 1));
        bottomTile = Math.max(0, Math.min(bottomTile, numRows - 1));
        leftTile = Math.max(0, Math.min(leftTile, numCols - 1));
        rightTile = Math.max(0, Math.min(rightTile, numCols - 1));

        int tl = tileMap.getType(topTile, leftTile);
        int tr = tileMap.getType(topTile, rightTile);
        int bl = tileMap.getType(bottomTile, leftTile);
        int br = tileMap.getType(bottomTile, rightTile);

        topLeft = tl == Tile.BLOCKED;
        topRight = tr == Tile.BLOCKED;
        bottomLeft = bl == Tile.BLOCKED;
        bottomRight = br == Tile.BLOCKED;
    }

    public void checkTileMapCollision() {
        currCol = (int) x / tileSize;
        currRow = (int) y / tileSize;

        xdest = x + dx;
        ydest = y + dy;

        xtemp = x;
        ytemp = y;

        // fi
        calculateCorners(x, ydest);
        if (outOfMap) {
            return;
        }

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

        // Check horizontal movement
        calculateCorners(xdest, y);
        if (outOfMap) {
            System.out.println("MapObject: checkTileMapCollision early exit due to outOfMap (horizontal) at (x=" + x + ", y=" + y + ")");
            return;
        }

        if (dx < 0) {
            if (topLeft || bottomLeft) {
                dx = 0;
                xtemp = currCol * tileSize + cwidth / 2;
                System.out.println("MapObject: Blocked left movement at x=" + xtemp);
            } else {
                xtemp += dx;
            }
        }
        if (dx > 0) {
            if (topRight || bottomRight) {
                dx = 0;
                xtemp = (currCol + 1) * tileSize - cwidth / 2;
                System.out.println("MapObject: Blocked right movement at x=" + xtemp);
            } else {
                xtemp += dx;
            }
        }

        // Check if should start falling
        if (!falling) {
            calculateCorners(x, ydest + 1);
            if (outOfMap) {
                System.out.println("MapObject: checkTileMapCollision early exit due to outOfMap (falling) at (x=" + x + ", y=" + y + ")");
                return;
            }
            if (!bottomLeft && !bottomRight) {
                falling = true;
            }
        }

        // Clamp x position to map boundaries to reinforce left/right walls
        int minX = cwidth / 2;
        int maxX = tileMap.getWidth() - cwidth / 2;
        if (xtemp < minX) {
            xtemp = minX;
            dx = 0;
            System.out.println("MapObject: Clamped to left edge at x=" + xtemp);
        } else if (xtemp > maxX) {
            xtemp = maxX;
            dx = 0;
            System.out.println("MapObject: Clamped to right edge at x=" + xtemp);
        }
    }

    public int getx() { return (int) x; }
    public int gety() { return (int) y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getCWidth() { return cwidth; }
    public int getCHeight() { return cheight; }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void setVector(double dx, double dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public void setMapPosition() {
        xmap = tileMap.getx();
        ymap = tileMap.gety();
    }

    public void setLeft(boolean b) { left = b; }
    public void setRight(boolean b) { right = b; }
    public void setUp(boolean b) { up = b; }
    public void setDown(boolean b) { down = b; }
    public void setJumping(boolean b) { jumping = b; }

    public boolean notOnScreen() {
        return x + xmap + width < 0 || x + xmap - width > GamePanel.WIDTH || y + ymap + height < 0 || y + ymap - height > GamePanel.HEIGHT;
    }

    public void draw(Graphics2D g) {
        if (facingRight) {
            g.drawImage(animation.getImage(), (int) (x + xmap - width / 2), (int) (y + ymap - height / 2), null);
        } else {
            g.drawImage(animation.getImage(), (int) (x + xmap - width / 2 + width), (int) (y + ymap - height / 2), -width, height, null);
        }
    }
}