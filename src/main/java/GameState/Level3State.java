package GameState;

import Blessing.Blessing;
import Entity.*;
import Entity.Enemies.Skeleton;
import Entity.Enemies.Slugger;
import Entity.Enemies.Bosses.WizardBoss;
import Entity.Enemies.Zombie;
import Entity.Projectiles.Arrow;
import Entity.Projectiles.Fire;
import Entity.Projectiles.Lightning;
import Main.GamePanel;
import TileMap.Background;
import TileMap.TileMap;
import TileMap.TerminalTile;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

// Manages Level 3 state
public class Level3State extends BaseLevelState {
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
    public Level3State(GameStateManager gsm, GamePanel gamePanel) {
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
        this.tileMap.loadMap("/Maps/level3-1.map");
        this.tileMap.setPosition(0, 0);
        this.tileMap.setTween(1);
        Point playerSpawn = new Point(100, 100);
        java.util.List<LevelConfiguration.EnemySpawnData> enemySpawns = new ArrayList<>();
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Zombie", new Point(100, 200)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Zombie", new Point(200, 200)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Zombie", new Point(200, 80)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Skeleton", new Point(250, 200)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Zombie", new Point(670, 170)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Zombie", new Point(780, 200)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Zombie", new Point(825, 200)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Skeleton", new Point(870, 200)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Zombie", new Point(1000, 200)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Skeleton", new Point(1050, 200)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Zombie", new Point(1480, 200)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Zombie", new Point(1500, 200)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Skeleton", new Point(1600, 200)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Skeleton", new Point(1620, 200)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Zombie", new Point(1700, 200)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Zombie", new Point(1750, 200)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Skeleton", new Point(1800, 200)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Skeleton", new Point(1840, 200)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Zombie", new Point(2300, 200)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Zombie", new Point(2400, 200)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Zombie", new Point(2445, 200)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Skeleton", new Point(2460, 200)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Zombie", new Point(2820, 200), true));
        Point[] doorCoords = {new Point(95, 5), new Point(95, 6)};
        this.levelConfig = new LevelConfiguration(
                "Level 3 - Dungeony Dungeon",
                "/Maps/level3-1.map",
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
                    if (spawnData.isKeyMob) { this.keyMob = enemy; }
                    break;
                case "Skeleton":
                    enemy = new Skeleton(tileMap, player);
                    if (spawnData.isKeyMob) { this.keyMob = enemy; }
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
            Enemy boss = new WizardBoss(tileMap, player);
            boss.setPosition(3050, 185);
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
                    if (e instanceof WizardBoss) {
                        bosses.remove(e);
                        levelComplete(GameStateManager.LEVEL3STATE);
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
                if (e instanceof WizardBoss) {
                    WizardBoss wb = (WizardBoss) e;
                    for (int j = wb.getLightningStrikes().size() - 1; j >= 0; j--) {
                        Lightning l = wb.getLightningStrikes().get(j);
                        if (player != null && l.intersects(player)) {
                            player.hit(l.getDamage());
                            l.setHit();
                        }
                    }
                    for (int j = wb.getFireWaves().size() - 1; j >= 0; j--) {
                        Fire f = wb.getFireWaves().get(j);
                        if (player != null && f.intersects(player)) {
                            player.hit(f.getDamage());
                            f.setHit();
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
        if (entityManager != null) {
            for (Enemy e : entityManager.getEnemies()) {
                if (e instanceof WizardBoss) {
                    WizardBoss wb = (WizardBoss) e;
                    for (Lightning l : wb.getLightningStrikes()) {
                        l.setMapPosition();
                        l.draw(g);
                    }
                    for (Fire f : wb.getFireWaves()) {
                        f.setMapPosition();
                        f.draw(g);
                    }
                }
            }
        }
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
    protected void handleLevelSpecificKeyReleased(int k) {}

    // Handles mouse press
    public void mousePressed(MouseEvent e) {
        int mx = e.getX()/GamePanel.SCALE;
        int my = e.getY()/GamePanel.SCALE;
        tileMap.handleMouse(mx, my);
        super.mousePressed(e);
    }

    // Handles commands
    @Override
    protected void handleLevelSpecificCommand(String[] token) {
        switch(token[0].toLowerCase()) {
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
                    if (p.y == 5) tileMap.setTile(p.y, p.x, 17);
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