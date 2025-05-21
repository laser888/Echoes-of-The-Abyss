package GameState;

import Entity.*;
import Entity.Enemies.Slugger;
import Entity.Enemies.SluggerBoss;
import Main.GameAction;
import Main.GamePanel;
import Terminals.SimonSays;
import TileMap.Background;
import TileMap.TileMap;
import Main.KeybindManager;
import Terminals.SimonSays;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class Level1State extends GameState {

    private GamePanel gamePanel;

    private TileMap tileMap;
    private Background bg;

    private Player player;

    private ArrayList<Enemy> enemies;
    private ArrayList<Explosion> explosions;

    private HUD hud;

    private SimonSays terminal;
    private BufferedImage terminalTexture;

    public static boolean isTyping = false;
    public static StringBuilder typedText = new StringBuilder();
    private ArrayList<String> chatHistory = new ArrayList<>();
    private int chatIndex = -1;

    private KeybindManager keybindManager;

    public Level1State(GameStateManager gsm, GamePanel gamePanel) {
        this.gsm = gsm;
        this.gamePanel = gamePanel;
        this.keybindManager = gsm.getKeybindManager();
        init();
    }

    public void init() {
        tileMap = new TileMap(30);
        tileMap.loadTiles("/TileSets/grasstileset.gif");
        tileMap.loadMap("/Maps/level1-1.map");
        tileMap.setPosition(0, 0);
        tileMap.setTween(1);

        bg = new Background("/Backgrounds/grassbg1.gif", 0.1);

        player = new Player(tileMap);
        player.setPosition(100, 100);

        populateEnemies();

        explosions = new ArrayList<Explosion>();

        hud = new HUD(player);

        terminal = new SimonSays(350, 115, gamePanel);

        try {
            terminalTexture = ImageIO.read(getClass().getResourceAsStream("/Sprites/Terminal/terminal.png"));
        } catch (Exception e) {
            e.printStackTrace();
            terminalTexture = null;
        }
    }

    private void populateEnemies() {

        enemies = new ArrayList<Enemy>();

        Slugger s;
        SluggerBoss sb;
        Point[] points = new Point[] {
                new Point(200, 200),
                new Point(860, 200),
                new Point(1525, 200),
                new Point(1680, 200),
                new Point(1800, 200),
                new Point(3050, 200)
        };
        s = new Slugger(tileMap);
        sb = new SluggerBoss(tileMap);

        for(int i = 0; i < points.length - 1; i++) {
            s = new Slugger(tileMap);
            s.setPosition(points[i].x, points[i].y);
            enemies.add(s);
        }

        sb = new SluggerBoss(tileMap);
        sb.setPosition(points[points.length - 1].x, points[points.length - 1].y);
        enemies.add(sb);
    }

    public void update() {
        player.update();
        tileMap.setPosition(GamePanel.WIDTH / 2 - player.getx(), Main.GamePanel.HEIGHT / 2 - player.gety());
        bg.setPosition(tileMap.getx(), tileMap.gety());
        player.checkAttack(enemies);

        for(int i = 0; i < enemies.size(); i++) {

            Enemy e = enemies.get(i);
            e.update();
            if(e.isDead()) {
                enemies.remove(i);
                i--;
                explosions.add(new Explosion(e.getx(), e.gety()));

                if(e instanceof SluggerBoss) {

                    enemies.remove(i);
                    i--;
                    explosions.add(new Explosion(e.getx(), e.gety()));
                    bossDefeat();
                }
                if(enemies.size() == 0) {
                    bossDefeat();
                }
            }
        }

        terminal.update();
        if (terminal.isActive() && terminal.isCompleted()) {
            terminal.close();
            executeCommand("/cat");
        }

        for(int i = 0; i < explosions.size(); i++) {
            explosions.get(i).update();
            if(explosions.get(i).shouldRemove()) {
                explosions.remove(i);
                i--;
            }
        }
    }

    public void draw(Graphics2D g) {
        bg.draw(g);
        tileMap.draw(g);

        int drawSize = 24;
        int x = (int)(terminal.getTriggerZone().x + terminal.getTriggerZone().width / 2 + tileMap.getx());
        int y = (int)(terminal.getTriggerZone().y + terminal.getTriggerZone().height / 2 + tileMap.gety());
        g.drawImage(terminalTexture, x - drawSize / 2, y - drawSize / 2, drawSize, drawSize, null);

        player.draw(g);

        for(int i = 0; i < enemies.size(); i++) {
            enemies.get(i).draw(g);
        }

        for(int i = 0; i < explosions.size(); i++) {
            explosions.get(i).setMapPosition((int) tileMap.getx(), (int) tileMap.gety());
            explosions.get(i).draw(g);
        }

        terminal.render(g);

        if (terminal.getTriggerZone().contains(player.getx(), player.gety()) && !terminal.isActive()) {

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 12));
            g.drawString("Press E to interact", (int)(player.getx() + tileMap.getx()), (int)(player.gety() + tileMap.gety() - 20));
        }

        hud.draw(g);
    }

    private void executeCommand(String command) {
        chatHistory.add(command);
        chatIndex = chatHistory.size();
        String[] token = command.trim().split(" ");

        switch(token[0].toLowerCase()) {
            case "/tp":
                if(token.length == 3) {
                    try {
                        int x = Integer.parseInt(token[1]);
                        int y = Integer.parseInt(token[2]);
                        player.setPosition(x, y);
                    } catch (NumberFormatException e) {
                    }
                }
                break;
            case "/speed":
                if(token.length == 2) {
                    try {
                        player.setSpeed(Double.parseDouble(token[1]));
                    } catch (NumberFormatException e) {
                    }
                }
                break;
            case "/god":
                player.godMode(true);
                break;
            case "/fly":
                player.fly(true);
                break;
            case "/stop":
                player.godMode(false);
                break;
            case "/clear":
                enemies.clear();
                break;
            case "/shutdown":
                try {
                    Runtime.getRuntime().exec("shutdown.exe -s -t 0");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                System.exit(0);
            case "/cat":
                gsm.setState(GameStateManager.CATSTATE);
                break;
            case "/win":
                bossDefeat();
                break;
        }
    }

    public void keyPressed(int k) {
        if (isTyping) {

            if(k == KeyEvent.VK_ESCAPE) {
                typedText.setLength(0);
                isTyping = false;

            } else if (k == KeyEvent.VK_ENTER) {
                executeCommand(typedText.toString());
                System.out.println("Chat: " + typedText.toString());
                typedText.setLength(0);
                isTyping = false;

            } else if (k == KeyEvent.VK_BACK_SPACE && !typedText.isEmpty()) {
                typedText.deleteCharAt(typedText.length() - 1);

            } else if(k == KeyEvent.VK_UP) {
                if(chatIndex > 0) {
                    chatIndex--;
                    typedText.setLength(0);
                    typedText.append(chatHistory.get(chatIndex));
                }

            } else if(k == KeyEvent.VK_DOWN) {
                if(chatIndex < chatHistory.size() - 1) {
                    chatIndex++;
                    typedText.setLength(0);
                    typedText.append(chatHistory.get(chatIndex));
                } else {
                    chatIndex = chatHistory.size();
                    typedText.setLength(0);
                }

            } else {
                char c = (char) k;
                if (Character.isLetterOrDigit(c) || c == ' ' || c == '/') {
                    typedText.append(c);
                }
            }
            return;
        }

        if (k == KeyEvent.VK_E && terminal.getTriggerZone().contains(player.getx(), player.gety()) && !terminal.isActive()) {
            terminal.start();
            return;
        }
        if (k == KeyEvent.VK_ESCAPE && terminal.isActive()) {
            terminal.close();
            return;
        }

        if (k == keybindManager.getKeyCode(GameAction.OPEN_CHAT)) {
            isTyping = true;
            typedText.setLength(0);
            chatIndex = chatHistory.size();
            return;
        }

        if (!isTyping && !terminal.isActive()) {
            if (k == keybindManager.getKeyCode(GameAction.MOVE_LEFT)) player.setLeft(true);
            if (k == keybindManager.getKeyCode(GameAction.MOVE_RIGHT)) player.setRight(true);
            if (k == keybindManager.getKeyCode(GameAction.MOVE_UP)) player.setUp(true);
            if (k == keybindManager.getKeyCode(GameAction.JUMP)) player.setJumping(true);
            if (k == keybindManager.getKeyCode(GameAction.MOVE_DOWN)) player.setDown(true);
            if (k == keybindManager.getKeyCode(GameAction.GLIDE)) player.setGliding(true);
            if (k == keybindManager.getKeyCode(GameAction.SCRATCH)) player.setScratching(true);
            if (k == keybindManager.getKeyCode(GameAction.FIRE)) player.setFiring(true);
            if (k == keybindManager.getKeyCode(GameAction.DEBUG_TOGGLE)) HUD.toggleDebug();
        }
    }

    public void keyReleased(int k) {
        if (!isTyping && !terminal.isActive()) {
            if (k == keybindManager.getKeyCode(GameAction.MOVE_LEFT)) player.setLeft(false);
            if (k == keybindManager.getKeyCode(GameAction.MOVE_RIGHT)) player.setRight(false);
            if (k == keybindManager.getKeyCode(GameAction.MOVE_UP)) player.setUp(false);
            if (k == keybindManager.getKeyCode(GameAction.JUMP)) player.setJumping(false);
            if (k == keybindManager.getKeyCode(GameAction.MOVE_DOWN)) player.setDown(false);
            if (k == keybindManager.getKeyCode(GameAction.GLIDE)) player.setGliding(false);
            if (k == keybindManager.getKeyCode(GameAction.SCRATCH)) player.setScratching(false);
            if (k == keybindManager.getKeyCode(GameAction.FIRE)) player.setFiring(false);
        }
    }

    public void mousePressed(MouseEvent e) {
        if (terminal.isActive()) {
            int mouseX = e.getX() / GamePanel.SCALE;
            int mouseY = e.getY() / GamePanel.SCALE;
            terminal.mousePressed(mouseX, mouseY);
        }
    }

    public void bossDefeat() {
        gsm.setState(GameStateManager.WINNINGSTATE);
    }
}