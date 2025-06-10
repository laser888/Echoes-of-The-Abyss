package TileMap;

import Main.GamePanel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class TileMap {

    // position
    private double x;
    private double y;

    // bounds
    private int xmin;
    private int ymin;
    private int xmax;
    private int ymax;

    private double tween;

    // map
    private int[][] map;
    private int tileSize;
    private int numRows;
    private int numCols;
    private int width;
    private int height;

    // tileset
    private BufferedImage tileset;
    private int numTilesAcross;
    private Tile[][] tiles;

    // drawing
    private int rowOffset;
    private int colOffset;
    private int numRowsToDraw;
    private int numColsToDraw;

    public TileMap(int tileSize) {
        this.tileSize = tileSize;
        numRowsToDraw = GamePanel.HEIGHT / tileSize + 2;
        numColsToDraw = GamePanel.WIDTH / tileSize + 2;
        tween = 0.07;
    }

    public void loadTiles(String s) {

        try {

            tileset = ImageIO.read(getClass().getResourceAsStream(s));
            numTilesAcross = tileset.getWidth() / tileSize;
            tiles = new Tile[2][numTilesAcross];

            BufferedImage subimage;
            for(int col = 0; col < numTilesAcross; col++) {
                subimage = tileset.getSubimage(col * tileSize, 0, tileSize, tileSize);
                tiles[0][col] = new Tile(subimage, Tile.NORMAL);
                subimage = tileset.getSubimage(col * tileSize, tileSize, tileSize, tileSize);
                tiles[1][col] = new Tile(subimage, Tile.BLOCKED);
            }

        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    public void loadMap(String s) {

        try {

            InputStream in = getClass().getResourceAsStream(s);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            numCols = Integer.parseInt((br.readLine()));
            numRows = Integer.parseInt(br.readLine());
            map = new int[numRows][numCols];
            width = numCols * tileSize;
            height = numRows * tileSize;

            xmin = GamePanel.WIDTH - width;
            xmax = 0;
            ymin = GamePanel.HEIGHT - height;
            ymax = 0;

            String delims = "\\s+";
            for(int row = 0; row < numRows; row++) {
                String line = br.readLine();
                String[] tokens = line.split(delims);

                for(int col = 0; col < numCols; col++) {
                    map[row][col] = Integer.parseInt(tokens[col]);
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getTileSize() { return tileSize;}
    public double getx() { return x; }
    public double gety() {return y; }
    public int getWidth() {return width; }
    public int getHeight() {return height; }

    public int getType(int row, int col) {
        int rc = map[row][col];
        int r = rc / numTilesAcross;
        int c = rc % numTilesAcross;
        return tiles[r][c].getType();
    }

    public void setPosition(double x, double y) {
        this.x += (x - this.x) * tween;
        this.y += (y - this.y) * tween;
        fixBounds();
        colOffset = (int) -this.x / tileSize;
        rowOffset = (int) -this.y / tileSize;

    }

    public void fixBounds() {
        if(x < xmin) x = xmin;
        if(y < ymin) y = ymin;
        if(x > xmax) x = xmax;
        if(y > ymax) y = ymax;
    }

    public void draw(Graphics2D g) {
        for (int row = rowOffset; row < rowOffset + numRowsToDraw; row++) {

            if (row >= numRows) break;
            if (row < 0) continue;

            for (int col = colOffset; col < colOffset + numColsToDraw; col++) {
                if (col >= numCols) break;
                if (col < 0) continue;
                if (map[row][col] == 0) continue;
                int rc = map[row][col];
                int r = rc / numTilesAcross;
                int c = rc % numTilesAcross;

                g.drawImage(tiles[r][c].getImage(), (int) x + col * tileSize, (int) y + row * tileSize, null);
            }
        }
    }

    public void setTween(double i) {
        tween = i;
    }

    public void setTile(int row, int col, int tileId) {
        if (row >= 0 && row < map.length && col >= 0 && col < map[0].length) {
            if (tileId >= 0 && tileId < 2 * numTilesAcross) {
                map[row][col] = tileId;
                ///System.out.println("Set tile at (" + row + ", " + col + ") to " + tileId);
            } else {
                System.out.println("Invalid tile ID: " + tileId);
            }
        } else {
            System.out.println("Invalid position: (" + row + ", " + col + ")");
        }
    }

    public void addRow() {
        int newRows = numRows + 1;
        int[][] newMap = new int[newRows][numCols];

        for (int row = 0; row < numRows; row++) {
            System.arraycopy(map[row], 0, newMap[row], 0, numCols);
        }

        for (int col = 0; col < numCols; col++) {
            newMap[numRows][col] = 0;
        }
        map = newMap;
        numRows = newRows;

        height = numRows * tileSize;
        ymin = GamePanel.HEIGHT - height;
        ymax = 0;

        fixBounds();
        //System.out.println("Added row, new size: " + numRows + "x" + numCols);
    }

    public void addColumn() {

        int newCols = numCols + 1;
        int[][] newMap = new int[numRows][newCols];

        for (int row = 0; row < numRows; row++) {
            System.arraycopy(map[row], 0, newMap[row], 0, numCols);
            newMap[row][numCols] = 0;
        }

        map = newMap;
        numCols = newCols;

        width = numCols * tileSize;
        xmin = GamePanel.WIDTH - width;
        xmax = 0;
        fixBounds();

        //System.out.println("Added column, new size: " + numRows + "x" + numCols);
    }


    public void saveMap(String filePath) {
        System.out.println("Attempting to save map to: " + filePath);
        try {

            BufferedWriter bw = new BufferedWriter(new FileWriter(filePath));

            bw.write(String.valueOf(numCols));
            bw.newLine();
            bw.write(String.valueOf(numRows));
            bw.newLine();

            for (int row = 0; row < numRows; row++) {
                StringBuilder line = new StringBuilder();
                for (int col = 0; col < numCols; col++) {
                    line.append(map[row][col]);
                    if (col < numCols - 1) {
                        line.append(" ");
                    }
                }
                bw.write(line.toString());
                bw.newLine();
            }

            bw.close();
            System.out.println("Map saved successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getNumTilesAcross() {
        return numTilesAcross;
    }

    public int[][] getMap() { return map; }
    public int getNumCols() { return numCols; }
    public int getNumRows() { return numRows; }
}
