package TileMap;

import java.awt.image.BufferedImage;

// Represents a single tile on the tile map
public class Tile {

    private BufferedImage image; // Tile sprite
    private int type; // Tile type (normal or blocked)

    // Tile types
    public static final int NORMAL = 0; // Walkable
    public static final int BLOCKED = 1; // Not walkable

    // Initializes tile with image and type
    public Tile(BufferedImage image, int type) {
        this.image = image;
        this.type = type;
    }

    // Returns tile image
    public BufferedImage getImage() {
        return image;
    }

    // Returns tile type
    public int getType() {
        return type;
    }
}
