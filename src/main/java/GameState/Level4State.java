package GameState;

import Blessing.Blessing;
import Entity.*;
import Entity.Enemies.Skeleton;
import Entity.Enemies.Slugger;
import Entity.Enemies.SluggerBoss;
import Entity.Enemies.Zombie;
import Main.GamePanel;
import TileMap.Background;
import TileMap.TileMap;
import TileMap.TerminalTile;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class Level4State extends BaseLevelState {

    private LevelConfiguration levelConfig;
    private boolean bossDoorIsOpen = false;
    private Point[] doorTileCoordinates;
    private Enemy keyMob;

    public Level4State(GameStateManager gsm, GamePanel gamePanel) {
        super(gsm, gamePanel);
    }

    @Override
    protected void loadLevelSpecifics() {

        this.tileMap = new TileMap(30, gamePanel);
        this.tileMap.loadTiles("/TileSets/grasstileset.gif");
        this.tileMap.loadMap("/Maps/level4-1.map");
        this.tileMap.setPosition(0, 0);
        this.tileMap.setTween(1);


        Point playerSpawn = new Point(100, 100);
        java.util.List<LevelConfiguration.EnemySpawnData> enemySpawns = new ArrayList<>();
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Zombie", new Point(200, 200)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Skeleton", new Point(150, 200)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Slugger", new Point(860, 200)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Slugger", new Point(1525, 200)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Slugger", new Point(1680, 200)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Slugger", new Point(1800, 200)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Slugger", new Point(2750, 200), true)); // Key Mob
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("SluggerBoss", new Point(3050, 200)));

        Point[] doorCoords = {new Point(96, 5), new Point(96, 6)};

        this.levelConfig = new LevelConfiguration(
                "Level 3 - Dungeony Dungeon",
                "/Maps/level4-1.map",
                "/TileSets/grasstileset.gif",
                "/Backgrounds/dungeonbg.gif",
                playerSpawn,
                enemySpawns,
                doorCoords,
                new Point(2750, 200),
                300.0
        );

        this.bg = new Background(levelConfig.getBackgroundPath(), 0.1);

        Player.PlayerClass selectedClass = gsm.getSelectedPlayerClass();
        this.player = new Player(tileMap, this, selectedClass, gsm, gsm.getGameData());
        this.player.setPosition(levelConfig.getPlayerSpawnPoint().x, levelConfig.getPlayerSpawnPoint().y);

        this.doorTileCoordinates = levelConfig.getDoorCoordinates();
        this.bossDoorIsOpen = false;
        setDoorState(false);
        this.parTimeSeconds = levelConfig.getParTimeSeconds();
    }

    @Override
    protected void populateLevelEntities() {

        for (LevelConfiguration.EnemySpawnData spawnData : levelConfig.getEnemySpawns()) {
            Enemy enemy = null;
            switch (spawnData.enemyType) {
                case "Slugger":
                    enemy = new Slugger(tileMap);
                    if (spawnData.isKeyMob) { this.keyMob = enemy; }
                    break;
                case "Zombie":
                    enemy = new Zombie(tileMap);
                    break;
                case "Skeleton":
                    enemy = new Skeleton(tileMap, player);
                    break;
                case "SluggerBoss":
                    enemy = new SluggerBoss(tileMap, player);
                    break;
            }
            if (enemy != null) {
                enemy.setPosition(spawnData.position.x, spawnData.position.y);
                entityManager.addEnemy(enemy);
            }
        }
        this.totalEnemiesAtStart = entityManager.getEnemies().size();
    }

    @Override
    protected void updateLevelSpecificLogic() {

        tileMap.updateInteractive();

        for (TerminalTile t : tileMap.getInteractiveTiles()) {
            if (t.isCompleted() && !t.isBlessingGiven()) {
                t.setBlessingGiven();
                Blessing b = Blessing.rollRandomBlessing();
                System.out.println("DEBUG: rolled → " + b.getType() + " = " + b.getValue());
                player.applyBlessings(b);
                t.markSolved();
                t.close();
            }
        }

        if(entityManager != null) {

            List<Enemy> currentEnemies = entityManager.getEnemies();
            for (int i = currentEnemies.size() - 1; i >= 0; i--) {
                Enemy e = currentEnemies.get(i);
                if (e.isDead()) {
                    enemiesKilledCount++;
                    if (e == keyMob && !bossDoorIsOpen) {
                        openBossDoor();
                        keyMob = null;
                    }
                    entityManager.addExplosion(new Explosion(tileMap, e.getx(), e.gety()));
                    currentEnemies.remove(i);

                    if (e instanceof SluggerBoss) {
                        levelComplete();
                    }
                }

                if (e instanceof Skeleton) {
                    Skeleton skeleton = (Skeleton) e;
                    ArrayList<Arrow> skeletonArrows = skeleton.getArrows();
                    for (int j = skeletonArrows.size() - 1; j >= 0; j--) {
                        Arrow arrow = skeletonArrows.get(j);
                        if (arrow.isEnemyArrow() && player != null && arrow.intersects(player)) {
                            player.hit(skeleton.getDamage());
                            arrow.setHit();
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void drawLevelSpecificElements(Graphics2D g) {
        tileMap.drawInteractive(g, player);
    }

    @Override
    protected void handleLevelSpecificKeyPressed(int k) {
        tileMap.handleKeyPress(k, player);
    }

    protected void handleLevelSpecificKeyReleased(int k) {}

    public void mousePressed(MouseEvent e) {

        int mx = e.getX()/GamePanel.SCALE;
        int my = e.getY()/GamePanel.SCALE;
        tileMap.handleMouse(mx, my);
        super.mousePressed(e);
    }

    @Override
    protected void handleLevelSpecificCommand(String[] token) {

        switch(token[0].toLowerCase()) {
            case "/getkey":
            case "/opendoor":
                if (!bossDoorIsOpen) {
                    openBossDoor();
                    //System.out.println("Level 1 Cheat: Door opened.");
                }
                break;
        }
    }

    private void openBossDoor() {
        if (!bossDoorIsOpen) {
            //System.out.println("Level 1: Boss door is opening!");
            bossDoorIsOpen = true;
            setDoorState(true);
        }
    }

    private void setDoorState(boolean open) {
        if (doorTileCoordinates != null && tileMap != null) {
            for (Point p : doorTileCoordinates) {

                if (open) {
                    if (p.y == 5) tileMap.setTile(p.y, p.x, 17);
                    else tileMap.setTile(p.y, p.x, 0);
                } else {
                    if (p.y == 5) tileMap.setTile(p.y, p.x, 33);
                    else tileMap.setTile(p.y, p.x, 34);
                }
            }
        }
    }

    @Override
    public int getSpawnX() {
        return (levelConfig != null) ? levelConfig.getPlayerSpawnPoint().x : 100;
    }

    @Override
    public int getSpawnY() {
        return (levelConfig != null) ? levelConfig.getPlayerSpawnPoint().y : 100;
    }
}
