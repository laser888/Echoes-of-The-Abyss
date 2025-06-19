package GameState;

import Blessing.Blessing;
import Entity.*;
import Entity.Enemies.Skeleton;
import Entity.Enemies.Slugger;
import Entity.Enemies.Bosses.SluggerBoss;
import Entity.Enemies.Zombie;
import Entity.Projectiles.Arrow;
import Main.GamePanel;
import TileMap.Background;
import TileMap.TileMap;
import TileMap.TerminalTile;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

// Manages Level 1 state
public class Level1State extends BaseLevelState {
    private LevelConfiguration levelConfig; // Level settings
    private boolean bossDoorIsOpen; // Tracks boss door
    private Point[] doorTileCoordinates; // Door tiles
    private Enemy keyMob; // Key enemy
    private boolean blessingApplied = false; // Tracks blessing
    private String blessingText = null; // Blessing message
    private long blessingTextTimer = 0; // Blessing timer
    private static final long BLESSING_TEXT_DURATION_NANO = 3_000_000_000L; // Blessing display time
    private boolean bossSpawned; // Tracks boss spawn
    private boolean inBossFight; // Tracks boss fight
    private List<Enemy> bosses; // Boss list

    // Initializes state
    public Level1State(GameStateManager gsm, GamePanel gamePanel) {
        super(gsm, gamePanel);
        this.bossSpawned = false;
        this.bossDoorIsOpen = false;
        this.inBossFight = false;
        this.bosses = new ArrayList<>();
    }

    // Initializes level
    @Override
    public void init() {
        super.init();
        this.bossSpawned = false;
        this.bossDoorIsOpen = false;
        this.inBossFight = false;
        this.bosses.clear();
        setDoorState(false);
    }

    // Loads level assets
    @Override
    protected void loadLevelSpecifics() {
        this.tileMap = new TileMap(30, gamePanel);
        this.tileMap.loadTiles("/TileSets/grasstileset.gif");
        this.tileMap.loadMap("/Maps/level1-1.map");
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
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Slugger", new Point(2750, 200), true));

        Point[] doorCoords = {new Point(96, 5), new Point(96, 6)};

        this.levelConfig = new LevelConfiguration(
                "Level 1 - Grassy Plains",
                "/Maps/level1-1.map",
                "/TileSets/grasstileset.gif",
                "/Backgrounds/stagebg1.gif",
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
        setDoorState(false);
        this.parTimeSeconds = levelConfig.getParTimeSeconds();
        this.hud = new HUD(player, this);
    }

    // Adds level enemies
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
            }
            if (enemy != null) {
                enemy.setPosition(spawnData.position.x, spawnData.position.y);
                entityManager.addEnemy(enemy);
            }
        }
        this.totalEnemiesAtStart = entityManager.getEnemies().size();
    }

    // Updates level logic
    @Override
    protected void updateLevelSpecificLogic() {
        tileMap.updateInteractive();
        for (TerminalTile t : tileMap.getInteractiveTiles()) {
            if (t.isCompleted() && !t.isBlessingGiven()) {
                t.setBlessingGiven();
                Blessing b = Blessing.rollRandomBlessing();
                blessingText = b.getType() + ": +" + Math.round(b.getValue() * 100) / 100;
                blessingTextTimer = System.nanoTime();
                blessingApplied = true;
                player.applyBlessings(b);
                t.markSolved();
                t.close();
            }
        }
        if (blessingText != null && (System.nanoTime() - blessingTextTimer) > BLESSING_TEXT_DURATION_NANO) {
            blessingText = null;
        }
        if (player != null && player.isDead() && inBossFight) {
            player.respawn();
        }
        if (bossDoorIsOpen && !bossSpawned && player != null && player.getx() > 2940) {
            setDoorState(false);
            bossDoorIsOpen = false;
            Enemy boss = new SluggerBoss(tileMap, player);
            boss.setPosition(3050, 200);
            entityManager.addEnemy(boss);
            bosses.add(boss);
            bossSpawned = true;
            inBossFight = true;
        }
        if (entityManager != null) {
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
                        bosses.remove(e);
                        levelComplete(GameStateManager.LEVEL1STATE);
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

    // Draws level elements
    @Override
    protected void drawLevelSpecificElements(Graphics2D g) {
        tileMap.drawInteractive(g, player);
        if (blessingText != null) {
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.setColor(Color.YELLOW);
            g.drawString(blessingText, 80, 20);
        }
    }

    // Handles key press
    @Override
    protected void handleLevelSpecificKeyPressed(int k) {
        tileMap.handleKeyPress(k, player);
    }

    // Handles key release
    @Override
    protected void handleLevelSpecificKeyReleased(int k) {}

    // Handles mouse press
    public void mousePressed(MouseEvent e) {
        int mx = e.getX() / GamePanel.SCALE;
        int my = e.getY() / GamePanel.SCALE;
        tileMap.handleMouse(mx, my);
        super.mousePressed(e);
    }

    // Handles commands
    @Override
    protected void handleLevelSpecificCommand(String[] token) {
        switch (token[0].toLowerCase()) {
            case "/getkey":
            case "/opendoor":
                if (!bossDoorIsOpen) {
                    openBossDoor();
                }
                break;
        }
    }

    // Opens boss door
    private void openBossDoor() {
        if (!bossDoorIsOpen) {
            bossDoorIsOpen = true;
            setDoorState(true);
        }
    }

    // Sets door state
    private void setDoorState(boolean open) {
        if (doorTileCoordinates != null && tileMap != null) {
            for (Point p : doorTileCoordinates) {
                if (open) {
                    if (p.y == 5) tileMap.setTile(p.y, p.x, 22);
                    else tileMap.setTile(p.y, p.x, 0);
                } else {
                    if (p.y == 5) tileMap.setTile(p.y, p.x, 33);
                    else tileMap.setTile(p.y, p.x, 34);
                }
            }
        }
    }

    // Gets spawn X
    @Override
    public int getSpawnX() {
        return (inBossFight && bossSpawned) ? 2950 : (levelConfig != null ? levelConfig.getPlayerSpawnPoint().x : 100);
    }

    // Gets spawn Y
    @Override
    public int getSpawnY() {
        return (inBossFight && bossSpawned) ? 200 : (levelConfig != null ? levelConfig.getPlayerSpawnPoint().y : 100);
    }

    // Gets bosses
    public List<Enemy> getBosses() {
        return bosses;
    }
}