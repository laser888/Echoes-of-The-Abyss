package GameState;

import java.awt.Point;
import java.util.List;

// Stores configuration data for a level
public class LevelConfiguration {

    private String levelName; // Level name
    private String tileMapPath; // Path to .map file
    private String tileSetPath; // Path to tileset image
    private String backgroundPath; // Path to background image
    private Point playerSpawnPoint; // Where player starts
    private List<EnemySpawnData> enemySpawns; // List of enemies and positions
    private Point[] doorCoordinates; // Doors that unlock after key mob is killed
    private Point keyMobSpawnPoint; // Key mob location
    private double parTimeSeconds; // Target time for score

    // Holds info for spawning one enemy
    public static class EnemySpawnData {
        public String enemyType; // Enemy class name
        public Point position; // Spawn position
        public boolean isKeyMob; // If this enemy unlocks the doors

        // Creates enemy spawn entry
        public EnemySpawnData(String enemyType, Point position, boolean isKeyMob) {
            this.enemyType = enemyType;
            this.position = position;
            this.isKeyMob = isKeyMob;
        }

        // Creates non-key-mob spawn
        public EnemySpawnData(String enemyType, Point position) {
            this(enemyType, position, false);
        }
    }

    // Initializes full level configuration
    public LevelConfiguration(String levelName, String tileMapPath, String tileSetPath, String backgroundPath,
                              Point playerSpawnPoint, List<EnemySpawnData> enemySpawns,
                              Point[] doorCoordinates, Point keyMobSpawnPoint, double parTimeSeconds) {
        this.levelName = levelName;
        this.tileMapPath = tileMapPath;
        this.tileSetPath = tileSetPath;
        this.backgroundPath = backgroundPath;
        this.playerSpawnPoint = playerSpawnPoint;
        this.enemySpawns = enemySpawns;
        this.doorCoordinates = doorCoordinates;
        this.keyMobSpawnPoint = keyMobSpawnPoint;
        this.parTimeSeconds = parTimeSeconds;
    }

    public String getLevelName() { return levelName; }
    public String getTileMapPath() { return tileMapPath; }
    public String getTileSetPath() { return tileSetPath; }
    public String getBackgroundPath() { return backgroundPath; }
    public Point getPlayerSpawnPoint() { return playerSpawnPoint; }
    public List<EnemySpawnData> getEnemySpawns() { return enemySpawns; }
    public Point[] getDoorCoordinates() { return doorCoordinates; }
    public Point getKeyMobSpawnPoint() { return keyMobSpawnPoint; }
    public double getParTimeSeconds() { return parTimeSeconds; }
}
