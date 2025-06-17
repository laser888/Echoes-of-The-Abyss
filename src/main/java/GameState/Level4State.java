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
    private boolean bossDoorIsOpen;
    private Point[] doorTileCoordinates;
    private Enemy keyMob;
    private boolean blessingApplied = false;
    private String blessingText = null;
    private long blessingTextTimer = 0;
    private static final long BLESSING_TEXT_DURATION_NANO = 3_000_000_000L;
    private boolean bossSpawned;
    private boolean inBossFight;

    public Level4State(GameStateManager gsm, GamePanel gamePanel) {
        super(gsm, gamePanel);
        this.bossSpawned = false;
        this.bossDoorIsOpen = false;
        this.inBossFight = false;
    }

    @Override
    public void init() {
        super.init();
        this.bossSpawned = false;
        this.bossDoorIsOpen = false;
        this.inBossFight = false;
        setDoorState(false);
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
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Zombie", new Point(250, 200)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Skeleton", new Point(300, 200)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Zombie", new Point(400, 170)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Zombie", new Point(580, 140)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Zombie", new Point(730, 110)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Zombie", new Point(860, 80)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Skeleton", new Point(900, 80)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Zombie", new Point(1050, 50)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Zombie", new Point(1200, 80)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Zombie", new Point(1350, 200)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Skeleton", new Point(1500, 200)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Zombie", new Point(1750, 200)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Skeleton", new Point(1800, 200)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Skeleton", new Point(1850, 200)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Skeleton", new Point(1900, 200)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Zombie", new Point(2150, 170)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Skeleton", new Point(2270, 230)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Zombie", new Point(2400, 50)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Zombie", new Point(2820, 200)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Zombie", new Point(3540, 200)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Zombie", new Point(3550, 200)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Zombie", new Point(4100, 170)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Zombie", new Point(4150, 170)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Zombie", new Point(4200, 170)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Zombie", new Point(4250, 170)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Zombie", new Point(4300, 170)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Skeleton", new Point(4350, 170)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Skeleton", new Point(4400, 170)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Skeleton", new Point(4450, 170)));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Slugger", new Point(4490, 170), true));
        enemySpawns.add(new LevelConfiguration.EnemySpawnData("Zombie", new Point(4500, 170)));



        Point[] doorCoords = {new Point(151, 4), new Point(151, 5)};

        this.levelConfig = new LevelConfiguration(
                "Level 4 - Dungeony Dungeon",
                "/Maps/level4-1.map",
                "/TileSets/grasstileset.gif",
                "/Backgrounds/dungeonbg.gif",
                playerSpawn,
                enemySpawns,
                doorCoords,
                new Point(4550, 170),
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
            //System.out.println("Level 4: Player died in boss fight, respawned at (" + getSpawnX() + ", " + getSpawnY() + ")");
        }

        if (bossDoorIsOpen && !bossSpawned && player != null && player.getx() > 2940) {
            setDoorState(false);
            bossDoorIsOpen = false;
            Enemy boss = new SluggerBoss(tileMap, player);
            boss.setPosition(4640, 170);
            entityManager.addEnemy(boss);
            bossSpawned = true;
            inBossFight = true;
            //System.out.println("Level 4: Door locked at x=2940, SluggerBoss spawned at (3050, 200), player.x=" + player.getx());
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
                        levelComplete(GameStateManager.LEVEL4STATE);
                    }
                }

                if (e instanceof Skeleton) {
                    Skeleton skeleton = (Skeleton) e;
                    ArrayList<Arrow> skeletonArrows = skeleton.getArrows();
                    for (int j = skeletonArrows.size() - 1; j >= 0; j--) {
                        Arrow arrow = skeletonArrows.get(j);
                        if (arrow.isEnemyArrow() && player != null && arrow.intersects(player)) {
                            System.out.println("Level 4: Arrow hit player at (" + arrow.getx() + "," + arrow.gety() + ")");
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

        if (blessingText != null) {
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.setColor(Color.YELLOW);
            g.drawString(blessingText, 80, 20);
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
                    System.out.println("Level 4: Cheat: Door opened");
                }
                break;
        }
    }

    private void openBossDoor() {
        if (!bossDoorIsOpen) {
            bossDoorIsOpen = true;
            setDoorState(true);
            //System.out.println("Level 4: Boss door opened");
        }
    }

    private void setDoorState(boolean open) {
        if (doorTileCoordinates != null && tileMap != null) {
            for (Point p : doorTileCoordinates) {
                if (open) {
                    if (p.y == 4) tileMap.setTile(p.y, p.x, 17);
                    else tileMap.setTile(p.y, p.x, 0);
                } else {
                    if (p.y == 4) tileMap.setTile(p.y, p.x, 33);
                    else tileMap.setTile(p.y, p.x, 34);
                }
            }
        }
    }

    @Override
    public int getSpawnX() {
        return (inBossFight && bossSpawned) ? 2950 : (levelConfig != null ? levelConfig.getPlayerSpawnPoint().x : 100);
    }

    @Override
    public int getSpawnY() {
        return (inBossFight && bossSpawned) ? 200 : (levelConfig != null ? levelConfig.getPlayerSpawnPoint().y : 100);
    }
}