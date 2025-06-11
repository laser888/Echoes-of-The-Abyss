package GameState;

import java.awt.Point;
import java.util.List;

public class LevelConfiguration {
    private String levelName;
    private String tileMapPath;
    private String tileSetPath;
    private String backgroundPath;
    private Point playerSpawnPoint;
    private List<EnemySpawnData> enemySpawns;
    private Point[] doorCoordinates;
    private Point keyMobSpawnPoint;
    private double parTimeSeconds;

    public static class EnemySpawnData {
        public String enemyType;
        public Point position;
        public boolean isKeyMob;

        public EnemySpawnData(String enemyType, Point position, boolean isKeyMob) {
            this.enemyType = enemyType;
            this.position = position;
            this.isKeyMob = isKeyMob;
        }
        public EnemySpawnData(String enemyType, Point position) {
            this(enemyType, position, false);
        }
    }

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