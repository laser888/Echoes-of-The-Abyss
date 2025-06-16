package Main;

import GameState.BaseLevelState;
import GameState.GameState;
import GameState.GameStateManager;

import Data.GameData;
import Data.SaveManager;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;

public class GamePanel extends JPanel implements Runnable, KeyListener, MouseListener, MouseMotionListener {

    // dimensions
    public static final int WIDTH = 320;
    public static final int HEIGHT = 240;
    public static final int SCALE = 2;

    // game thread
    private Thread thread;
    private boolean running;
    private int FPS = 60;
    private long targetTime = 1000 / FPS;

    // image
    private BufferedImage image;
    private Graphics2D g;

    // game state manager
    private GameStateManager gsm;

    private KeybindManager keybindManager;

    private int frameCount = 0;
    private long fpsTimer = System.nanoTime();
    private static int currentFPS;



    public GamePanel() {
        super();
        setPreferredSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
        setFocusable(true);
        addMouseListener(this);
        addMouseMotionListener(this);
        requestFocus();
        setFocusTraversalKeysEnabled(false); // this is stupid
        setDoubleBuffered(true);
    }

    public void addNotify() {
        super.addNotify();
        if (thread == null) {
            thread = new Thread(this);
            addKeyListener(this);
            addMouseListener(this);
            addMouseMotionListener(this);
            thread.start();
        }
    }

    private void init() {
        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        g = (Graphics2D) image.getGraphics();
        running = true;
        GameData gameData = SaveManager.loadGame();
        //System.out.println("GamePanel: Loaded GameData with keybinds: " + gameData.keybinds);
        keybindManager = new KeybindManager(gameData);
        gsm = new GameStateManager(keybindManager, this, gameData);
    }

    public void run() {
        init();

        long start;
        long elapsed;
        long wait;

        // game loop
        while (running) {
            start = System.nanoTime();

            frameCount++;
            if (System.nanoTime() - fpsTimer >= 1000000000) {
                currentFPS = frameCount;
                frameCount = 0;
                fpsTimer = System.nanoTime();
            }

            update();
            draw();
            repaint();

            elapsed = System.nanoTime() - start;
            wait = targetTime - elapsed / 1000000;

            if (wait < 0) {
                wait = 5;
            }

            try {
                Thread.sleep(wait);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void update() {
        gsm.update();
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        int panelWidth = getWidth();
        int panelHeight = getHeight();

        g2.drawImage(image, 0, 0, panelWidth, panelHeight, null);

        g2.dispose();
    }

    private void draw() {
        gsm.draw(g);
    }

    private void drawToScreen() {
        Graphics g2 = getGraphics();
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        g2.drawImage(image, 0, 0, panelWidth, panelHeight, null);
        g2.dispose();
    }

    public void keyTyped(KeyEvent key) {
    }

    public void keyPressed(KeyEvent key) {
        gsm.keyPressed(key.getKeyCode());
    }

    public void keyReleased(KeyEvent key) {
        if(gsm == null){ return;}
        gsm.keyReleased(key.getKeyCode());
    }

        public void mousePressed(MouseEvent e) {
            Object state = gsm.getCurrentState();
            //System.out.println("Current state: " + state);
            if (state != null) {
                gsm.getCurrentState().mousePressed(e);
            } else {
                System.out.println("No current state!");
            }
        }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public static int getFPS() {
        return currentFPS;
    }

    public KeybindManager getKeybindManager() {
        return keybindManager;
    }

    public int getCurrentWidth() {
        return getWidth();
    }

    public int getCurrentHeight() {
        return getHeight();
    }

    public void mouseMoved(MouseEvent e) { }

    @Override
    public void mouseDragged(MouseEvent e) {
        //System.out.println("GamePanel.mouseDragged fired: x=" + e.getX() + " y=" + e.getY());

        GameState state = gsm.getCurrentState();
        if (state instanceof BaseLevelState) {
            ((BaseLevelState)state).mouseDragged(e);
        }
    }


}