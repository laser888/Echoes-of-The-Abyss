package GameState;

import Blessing.Blessing;
import Entity.*;
import Entity.Enemies.*;
import Main.GamePanel;
import TileMap.Background;
import TileMap.TileMap;
import TileMap.TerminalTile;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class Level2State extends BaseLevelState {
    private LevelConfiguration levelConfig;
    private boolean bossDoorIsOpen;
    private Point[] doorTileCoordinates;
    private Enemy keyMob;
    private boolean blessingApplied = false;
    private String blessingText = null;
    private long blessingTextTimer = 0;
    private static final long BLESSING_TEXT_DURATION_NANO = 3_000_000_000L;
    private String bossHintText = null;
    private long bossHintTimer = 0;
    private static final long BOSS_HINT_DURATION_NANO = 5_000_000_000L;
    private ArrayList<Livid> lividGroup;
    private Livid initialBoss;
    private boolean bossActivated;
    private boolean bossFightStarted;
    private boolean inBossFight;

    public Level2State(GameStateManager gsm, GamePanel gamePanel) {
        super(gsm, gamePanel);
        lividGroup = new ArrayList<>();
        this.bossActivated = false;
        this.bossDoorIsOpen = false;
        this.bossFightStarted = false;
        this.inBossFight = false;
    }

    @Override
    public void init() {
        super.init();
        this.bossActivated = false;
        this.bossDoorIsOpen = false;
        this.bossFightStarted = false;
        this.inBossFight = false;
        lividGroup.clear();
        setDoorState(false);
    }

    @Override
    protected void loadLevelSpecifics() {
        lividGroup.clear();
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
    }

    @Override
    protected void populateLevelEntities() {
        lividGroup.clear();
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
        //System.out.println("Level 2: Populated " + totalEnemiesAtStart + " enemies (excluding Livids)");
    }

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
            //System.out.println("Level 2: Player died in boss fight, respawned at (" + getSpawnX() + ", " + getSpawnY() + ")");
        }

        if (bossDoorIsOpen && !bossActivated && player != null && player.getx() > 2940) {
            setDoorState(false);
            bossDoorIsOpen = false;
            String[] suits = {"Heart", "Diamond", "Spade"};
            int realSuitIndex = (int)(Math.random() * 3);
            String realSuit = suits[realSuitIndex];
            for (int i = 0; i < suits.length; i++) {
                Livid livid = new Livid(tileMap, player, suits[i], i == realSuitIndex, lividGroup);
                double spawnX = 3050 + (50 * i);
                livid.setPosition(spawnX, 185);
                lividGroup.add(livid);
                entityManager.addEnemy(livid);
                System.out.println("Level 2: Spawned Livid (" + livid.getSuitType() + ", " +
                        (livid.isReal() ? "real" : "clone") + ") at (" + livid.getx() + ", " + livid.gety() + ")");
                if (livid.isReal()) {
                    bossHintText = "The " + livid.getSuitType() + " Livid is the true boss!";
                    bossHintTimer = System.nanoTime();
                }
            }
            bossActivated = true;
            inBossFight = true;
            //System.out.println("Level 2: Door locked at x=2940, Livid group spawned, player.x=" + player.getx());
        }

        if (bossHintText != null && (System.nanoTime() - bossHintTimer) > BOSS_HINT_DURATION_NANO) {
            bossHintText = null;
        }

        if (lividGroup != null) {
            for (Livid livid : lividGroup) {
                if (livid.shouldBlindPlayer()) {
                    startScreenFlash(500);
                    System.out.println("Level 2: Livid triggered blind effect");
                }
                if (livid.isDead() && livid.isReal()) {
                    levelComplete(GameStateManager.LEVEL2STATE);
                }
            }
        }

        if (entityManager != null) {
            List<Enemy> currentEnemies = entityManager.getEnemies();
            for (int i = currentEnemies.size() - 1; i >= 0; i--) {
                Enemy e = currentEnemies.get(i);
                if (e.isDead()) {
                    if (e instanceof Livid && !((Livid)e).isReal()) {
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
                } else if (e instanceof Livid) {
                    Livid livid = (Livid) e;
                    ArrayList<CardProjectile> cards = livid.getCards();
                    for (int j = cards.size() - 1; j >= 0; j--) {
                        CardProjectile card = cards.get(j);
                        if (player != null && card.intersects(player)) {
                            player.hit(livid.getDamage());
                            card.setHit();
                        }
                    }
                }
            }
        }
    }

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
            g.drawString(bossHintText, 80, 40);
        }
    }

    @Override
    protected void handleLevelSpecificKeyPressed(int k) {
        tileMap.handleKeyPress(k, player);
    }

    @Override
    protected void handleLevelSpecificKeyReleased(int k) {}

    @Override
    public void mousePressed(MouseEvent e) {
        int mx = e.getX() / GamePanel.SCALE;
        int my = e.getY() / GamePanel.SCALE;
        tileMap.handleMouse(mx, my);
        super.mousePressed(e);
    }

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
                        System.out.println("Level 2: Teleported to (" + x + ", " + y + ")");
                    } catch (NumberFormatException e) {
                        System.out.println("Level 2: Invalid teleport coordinates.");
                    }
                }
                break;
        }
    }

    private void openBossDoor() {
        if (!bossDoorIsOpen) {
            bossDoorIsOpen = true;
            setDoorState(true);
            //System.out.println("Level 2: Boss door opened");
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
        return (inBossFight && bossActivated) ? 2950 : (levelConfig != null ? levelConfig.getPlayerSpawnPoint().x : 100);
    }

    @Override
    public int getSpawnY() {
        return (inBossFight && bossActivated) ? 200 : (levelConfig != null ? levelConfig.getPlayerSpawnPoint().y : 100);
    }
}