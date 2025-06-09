package GameState;

import Entity.*;
import Entity.Enemies.Skeleton;
import Entity.Enemies.Slugger;
import Entity.Enemies.SluggerBoss;
import Entity.Enemies.Zombie;
import Main.GamePanel;
import Terminals.SimonSays;
import TileMap.Background;
import TileMap.TileMap;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.List;

public class Level1State extends BaseLevelState {

    private LevelConfiguration levelConfig;

    private SimonSays terminal;
    private BufferedImage terminalTexture;
    private boolean bossDoorIsOpen = false;
    private Point[] doorTileCoordinates;
    private Enemy keyMob;

    public Level1State(GameStateManager gsm, GamePanel gamePanel) {
        super(gsm, gamePanel);
    }

    @Override
    protected void loadLevelSpecifics() {
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
                "Level 1 - Grassy Plains",
                "/Maps/level1-1.map",
                "/TileSets/grasstileset.gif",
                "/Backgrounds/stagebg1.gif",
                playerSpawn,
                enemySpawns,
                doorCoords,
                new Point(2750,200),
                300.0
        );

        this.tileMap = new TileMap(30);
        this.tileMap.loadTiles(levelConfig.getTileSetPath());
        this.tileMap.loadMap(levelConfig.getTileMapPath());
        this.tileMap.setPosition(0, 0);
        this.tileMap.setTween(1);

        this.bg = new Background(levelConfig.getBackgroundPath(), 0.1);

        Player.PlayerClass selectedClass = gsm.getSelectedPlayerClass();
        this.player = new Player(tileMap, this, selectedClass, gsm);
        this.player.setPosition(levelConfig.getPlayerSpawnPoint().x, levelConfig.getPlayerSpawnPoint().y);

        this.doorTileCoordinates = levelConfig.getDoorCoordinates();
        this.bossDoorIsOpen = false;
        setDoorState(false);

        this.parTimeSeconds = levelConfig.getParTimeSeconds();
        this.totalPuzzlesInLevel = 1;
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

        this.terminal = new SimonSays(350, 115, gamePanel);
        try {
            this.terminalTexture = ImageIO.read(getClass().getResourceAsStream("/Sprites/Terminal/terminal.png"));
        } catch (Exception e) {
            e.printStackTrace();
            this.terminalTexture = null;
        }
    }

    @Override
    protected void updateLevelSpecificLogic() {

        if (terminal != null) {
            terminal.update();
            if (terminal.isActive() && terminal.isCompleted()) {

                if (puzzlesSolvedCount == 0) {
                    puzzlesSolvedCount++;
                }
                terminal.close();
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

        if (terminal != null && terminalTexture != null) {
            int drawSize = 24;
            int tx = (int) (terminal.getTriggerZone().x + terminal.getTriggerZone().width / 2.0 + tileMap.getx());
            int ty = (int) (terminal.getTriggerZone().y + terminal.getTriggerZone().height / 2.0 + tileMap.gety());
            g.drawImage(terminalTexture, tx - drawSize / 2, ty - drawSize / 2, drawSize, drawSize, null);

            terminal.render(g);

            if (player != null && terminal.getTriggerZone().contains(player.getx(), player.gety()) && !terminal.isActive()) {
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.PLAIN, 12));

                g.drawString("Press E to interact",
                        (int) (player.getx() + tileMap.getx() - 30),
                        (int) (player.gety() + tileMap.gety() - 20));
            }
        }
    }

    @Override
    protected void handleLevelSpecificKeyPressed(int k) {
        if (k == KeyEvent.VK_E && terminal != null && player != null &&
                terminal.getTriggerZone().contains(player.getx(), player.gety()) &&
                !terminal.isActive() && !isTyping) {
            terminal.start();
        }
        if (k == KeyEvent.VK_ESCAPE && terminal != null && terminal.isActive()) {
            terminal.close();
        }
    }

    protected void handleLevelSpecificKeyReleased(int k) {}

    @Override
    public void mousePressed(MouseEvent e) {
        if (terminal != null && terminal.isActive()) {
            int mouseX = e.getX() / GamePanel.SCALE;
            int mouseY = e.getY() / GamePanel.SCALE;
            terminal.mousePressed(mouseX, mouseY);
        }
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
