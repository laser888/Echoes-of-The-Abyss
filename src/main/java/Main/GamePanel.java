package Main;

import GameState.BaseLevelState;
import GameState.GameState;
import GameState.GameStateManager;
import Data.GameData;
import Data.SaveManager;
import Main.KeybindManager;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

// Manages game rendering and input
public class GamePanel extends JPanel implements Runnable, KeyListener, MouseListener, MouseMotionListener {
    // Dimensions
    public static final int WIDTH = 320; // Logical width
    public static final int HEIGHT = 240; // Logical height
    public static final int SCALE = 2; // Scaling factor

    // Game thread
    private Thread thread; // Main thread
    private boolean running; // Tracks running state
    private int FPS = 60; // Target FPS
    private long targetTime = 1000 / FPS; // Frame time in ms

    // Image
    private BufferedImage image; // Render buffer
    private Graphics2D g; // Graphics context

    // Game state
    private GameStateManager gsm; // State manager
    private KeybindManager keybindManager; // Keybind manager

    // FPS tracking
    private int frameCount = 0; // Frame counter
    private long fpsTimer = System.nanoTime(); // FPS timer
    private static int currentFPS; // Current FPS

    // Initializes panel
    public GamePanel() {
        super();
        setPreferredSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
        setFocusable(true);
        addMouseListener(this);
        addMouseMotionListener(this);
        requestFocus();
        setFocusTraversalKeysEnabled(false);
        setDoubleBuffered(true);
    }

    // Starts thread
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

    // Initializes game
    private void init() {
        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        g = (Graphics2D) image.getGraphics();
        running = true;
        GameData gameData = SaveManager.loadGame();
        keybindManager = new KeybindManager(gameData);
        gsm = new GameStateManager(keybindManager, this, gameData);
    }

    // Runs game loop
    public void run() {
        init();
        long start;
        long elapsed;
        long wait;
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

    // Updates game state
    private void update() {
        gsm.update();
    }

    // Paints panel
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        g2.drawImage(image, 0, 0, panelWidth, panelHeight, null);
        g2.dispose();
    }

    // Draws to buffer
    private void draw() {
        gsm.draw(g);
    }

    // Draws to screen
    private void drawToScreen() {
        Graphics g2 = getGraphics();
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        g2.drawImage(image, 0, 0, panelWidth, panelHeight, null);
        g2.dispose();
    }

    // Handles key typed
    public void keyTyped(KeyEvent key) {}

    // Handles key press
    public void keyPressed(KeyEvent key) {
        if (gsm == null) return;
        gsm.keyPressed(key.getKeyCode());
    }

    // Handles key release
    public void keyReleased(KeyEvent key) {
        if (gsm == null) return;
        gsm.keyReleased(key.getKeyCode());
    }

    // Handles mouse press
    public void mousePressed(MouseEvent e) {
        Object state = gsm.getCurrentState();
        if (state != null) {
            gsm.getCurrentState().mousePressed(e);
        } else {
            System.out.println("No current state!");
        }
    }

    // Handles mouse click
    public void mouseClicked(MouseEvent e) {}

    // Handles mouse release
    public void mouseReleased(MouseEvent e) {}

    // Handles mouse enter
    public void mouseEntered(MouseEvent e) {}

    // Handles mouse exit
    public void mouseExited(MouseEvent e) {}

    // Gets current FPS
    public static int getFPS() {
        return currentFPS;
    }

    // Gets keybind manager
    public KeybindManager getKeybindManager() {
        return keybindManager;
    }

    // Gets current panel width
    public int getCurrentWidth() {
        return getWidth();
    }

    // Gets current panel height
    public int getCurrentHeight() {
        return getHeight();
    }

    // Handles mouse move
    public void mouseMoved(MouseEvent e) {}

    // Handles mouse drag
    public void mouseDragged(MouseEvent e) {
        GameState state = gsm.getCurrentState();
        if (state instanceof BaseLevelState) {
            ((BaseLevelState) state).mouseDragged(e);
        }
    }
}