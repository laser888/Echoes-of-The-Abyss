package GameState;

import Blessing.Blessing;
import Entity.*;
import Entity.Enemies.*;
import Entity.Enemies.Bosses.CloneBoss;
import Entity.Projectiles.Arrow;
import Entity.Projectiles.Card;
import Main.GamePanel;
import TileMap.Background;
import TileMap.TileMap;
import TileMap.TerminalTile;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

// Manages Level 2 state
public class Level2State extends BaseLevelState {
    private LevelConfiguration levelConfig; // Level settings
    private boolean bossDoorIsOpen; // Tracks boss door
    private Point[] doorTileCoordinates; // Door tiles
    private Enemy keyMob; // Key enemy
    private boolean blessingApplied = false; // Tracks blessing
    private String blessingText = null; // Blessing message
    private long blessingTextTimer = 0; // Blessing timer
    private static final long BLESSING_TEXT_DURATION_NANO = 3_000_000_000L; // Blessing display time
    private String bossHintText = null; // Boss hint
    private long bossHintTimer = 0; // Hint timer
    private static final long BOSS_HINT_DURATION_NANO = 5_000_000_000L; // Hint display time
    private ArrayList<CloneBoss> cloneGroup; // Clone bosses
    private CloneBoss initialBoss; // Initial boss
    private boolean bossActivated; // Tracks boss activation
    private boolean bossFightStarted; // Tracks boss fight
    private boolean inBossFight; // Tracks boss fight
    private List<Enemy> bosses; // Boss list

    // Initializes state
    public Level2State(GameStateManager gsm, GamePanel gamePanel) {
        super(gsm, gamePanel);
        cloneGroup = new ArrayList<>();
        this.bossActivated = false;
        this.bossDoorIsOpen = false;
        this.bossFightStarted = false;
        this.inBossFight = false;
        this.bosses = new ArrayList<>();
    }

    // Initializes level
    @Override
    public void init() {
        super.init();
        this.bossActivated = false;
        this.bossDoorIsOpen = false;
        this.bossFightStarted = false;
        this.inBossFight = false;
        cloneGroup.clear();
        this.bosses.clear();
        setDoorState(false);
    }

    // Loads level assets
    @Override
    protected void loadLevelSpecifics() {
        cloneGroup.clear();
        bossDoorIsOpen = false;
        bossActivated = false;
        bossFightStarted = false;
        initialBoss = null;
        blessingApplied = false;
        blessingText = null;
        bossHintText = null;
        keyMob = null;
        this.tileMap = new TileMap(30, gamePanel);
        this.tileMap.loadTiles("/TileSets/grasstileset.gif");
        this.tileMap.loadMap("/Maps/level2-1.map");
        this.tileMap.setPosition(0, 0);
        this.tileMap.setTween(1);
        Point playerSpawn = new Point(100, 100);
        java.util.List<LevelConfiguration.EnemySpawnData> enemySpawns = new ArrayList<>();
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Zombie", new Point(650, 170)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Zombie", new Point(780, 110)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Zombie", new Point(1200, 200)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Zombie", new Point(1250, 200)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Zombie", new Point(1305, 200)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Zombie", new Point(1310, 80)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Skeleton", new Point(1300, 200)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Zombie", new Point(1900, 110)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Zombie", new Point(2200, 200)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Skeleton", new Point(2300, 200)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Zombie", new Point(2830, 200), true));
        Point[] doorCoords = {new Point(96, 5), new Point(96, 6)};
        this.levelConfig = new LevelConfiguration(
                "Level 2 - Cassy Castle",
                "/Maps/level2-1.map",
                "/TileSets/grasstileset.gif",
                "/Backgrounds/castlebg.gif",
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
        cloneGroup.clear();
        entityManager.getEnemies().clear();
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
        if (bossDoorIsOpen && !bossActivated && player != null && player.getx() > 2940) {
            setDoorState(false);
            bossDoorIsOpen = false;
            String[] suits = {"Heart", "Diamond", "Spade"};
            int realSuitIndex = (int)(Math.random() * 3);
            String realSuit = suits[realSuitIndex];
            for (int i = 0; i < suits.length; i++) {
                CloneBoss cloneBoss = new CloneBoss(tileMap, player, suits[i], i == realSuitIndex, cloneGroup);
                double spawnX = 3050 + (50 * i);
                cloneBoss.setPosition(spawnX, 185);
                cloneGroup.add(cloneBoss);
                entityManager.addEnemy(cloneBoss);
                if (cloneBoss.isReal()) {
                    bosses.add(cloneBoss);
                }
                if (cloneBoss.isReal()) {
                    bossHintText = "The " + cloneBoss.getSuitType() + " Livid is the true boss!";
                    bossHintTimer = System.nanoTime();
                }
            }
            bossActivated = true;
            inBossFight = true;
        }
        if (bossHintText != null && (System.nanoTime() - bossHintTimer) > BOSS_HINT_DURATION_NANO) {
            bossHintText = null;
        }
        if (cloneGroup != null) {
            for (CloneBoss cloneBoss : cloneGroup) {
                if (cloneBoss.shouldBlindPlayer()) {
                    startScreenFlash(500);
                }
                if (cloneBoss.isDead() && cloneBoss.isReal()) {
                    bosses.remove(cloneBoss);
                    levelComplete(GameStateManager.LEVEL2STATE);
                }
            }
        }
        if (entityManager != null) {
            List<Enemy> currentEnemies = entityManager.getEnemies();
            for (int i = currentEnemies.size() - 1; i >= 0; i--) {
                Enemy e = currentEnemies.get(i);
                if (e.isDead()) {
                    if (e instanceof CloneBoss && !((CloneBoss)e).isReal()) {
                        currentEnemies.remove(i);
                        continue;
                    }
                    enemiesKilledCount++;
                    if (e == keyMob && !bossDoorIsOpen) {
                        openBossDoor();
                        keyMob = null;
                    }
                    entityManager.addExplosion(new Explosion(tileMap, e.getx(), e.gety()));
                    currentEnemies.remove(i);
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
                } else if (e instanceof CloneBoss) {
                    CloneBoss cloneBoss = (CloneBoss) e;
                    ArrayList<Card> cards = cloneBoss.getCards();
                    for (int j = cards.size() - 1; j >= 0; j--) {
                        Card card = cards.get(j);
                        if (player != null && card.intersects(player)) {
                            player.hit(cloneBoss.getDamage());
                            card.setHit();
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
        if (bossHintText != null) {
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.setColor(Color.RED);
            g.drawString(bossHintText, 80, 60);
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
    @Override
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
            case "/teleport":
                if (token.length == 3) {
                    try {
                        int x = Integer.parseInt(token[1]);
                        int y = Integer.parseInt(token[2]);
                        player.setPosition(x, y);
                    } catch (NumberFormatException e) {
                        System.out.println("Level 2: Invalid teleport coordinates.");
                    }
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
        return (inBossFight && bossActivated) ? 2950 : (levelConfig != null ? levelConfig.getPlayerSpawnPoint().x : 100);
    }

    // Gets spawn Y
    @Override
    public int getSpawnY() {
        return (inBossFight && bossActivated) ? 200 : (levelConfig != null ? levelConfig.getPlayerSpawnPoint().y : 100);
    }

    // Gets bosses
    public List<Enemy> getBosses() {
        return bosses;
    }
}