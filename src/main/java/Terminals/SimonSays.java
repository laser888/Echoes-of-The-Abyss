package Terminals;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import Main.GamePanel;


public class SimonSays extends Terminal {

    private GamePanel gamePanel;

    private static final int GRID_SIZE = 3;
    private static final int CELL_SIZE = 50;
    private static final int GAP = 5;
    private static final int PANEL_SIZE = GRID_SIZE * CELL_SIZE + (GRID_SIZE + 1) * GAP;

    private boolean active;
    private List<Point> sequence;
    private List<Point> playerInput;
    private int currentSequenceLength;
    private int currentIndex;
    private long flashTimer;
    private boolean showingSequence;
    private boolean awaitingInput;
    private Random random;
    private boolean flashOn;
    private Point clickedCell;
    private long clickTimer;
    private boolean puzzleSolved = false;

    public SimonSays(int x, int y, GamePanel gamePanel, int tileSize) {

        this.gamePanel = gamePanel;
        int interactionRadius = tileSize * 2;
        this.triggerZone = new Rectangle(x - interactionRadius/2 + tileSize/2, y - interactionRadius/2 + tileSize/2, interactionRadius, interactionRadius);
        completed = false;
        active = false;
        sequence = new ArrayList<>();
        playerInput = new ArrayList<>();
        currentSequenceLength = 1;
        currentIndex = 0;
        flashTimer = 0;
        showingSequence = false;
        awaitingInput = false;
        random = new Random();
        flashOn = false;
        clickedCell = null;
        clickTimer = 0;
    }

    public void start() {

        active = true;
        completed = false;
        sequence.clear();
        playerInput.clear();
        currentSequenceLength = 1;
        currentIndex = 0;
        sequence.add(new Point(random.nextInt(GRID_SIZE), random.nextInt(GRID_SIZE)));
        showingSequence = true;
        flashOn = true;
        flashTimer = System.currentTimeMillis();
    }

    private void generateSequence() {
        sequence.add(new Point(random.nextInt(GRID_SIZE), random.nextInt(GRID_SIZE)));
    }

    public void update() {

        if (!active || completed) return;

        if (showingSequence) {

            long currentTime = System.currentTimeMillis();

            if (currentTime - flashTimer > (flashOn ? 500 : 200)) {

                flashOn = !flashOn;
                flashTimer = currentTime;

                if (flashOn) {
                    currentIndex++;

                    if (currentIndex >= currentSequenceLength) {

                        showingSequence = false;
                        awaitingInput = true;
                        currentIndex = 0;
                        playerInput.clear();
                        flashOn = false;
                    }
                }
            }
        }

        if (clickedCell != null && System.currentTimeMillis() - clickTimer > 200) {
            clickedCell = null;
        }
    }

    public void render(Graphics2D g) {

        if (!active) return;

        int offsetX = (GamePanel.WIDTH - PANEL_SIZE) / 2;
        int offsetY = (GamePanel.HEIGHT - PANEL_SIZE) / 2;

        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(offsetX, offsetY, PANEL_SIZE, PANEL_SIZE);

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {

                int x = offsetX + GAP + col * (CELL_SIZE + GAP);
                int y = offsetY + GAP + row * (CELL_SIZE + GAP);

                if (clickedCell != null && clickedCell.x == row && clickedCell.y == col) {
                    g.setColor(Color.GREEN);
                }

                else if (showingSequence && flashOn && currentIndex < sequence.size() &&
                        sequence.get(currentIndex).x == row && sequence.get(currentIndex).y == col) {
                    g.setColor(Color.YELLOW);
                } else {
                    g.setColor(Color.GRAY);
                }

                g.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                g.setColor(Color.BLACK);
                g.drawRect(x, y, CELL_SIZE, CELL_SIZE);
            }
        }
    }

    public void mousePressed(int x, int y) {
        if (!active || !awaitingInput || completed) {
           // System.out.println("Mouse click ignored: active=" + active + ", awaitingInput=" + awaitingInput + ", completed=" + completed);
            return;
        }

        int panelWidth = gamePanel.getCurrentWidth();
        int panelHeight = gamePanel.getCurrentHeight();

        float scaleX = (float) (GamePanel.WIDTH * GamePanel.SCALE) / panelWidth;
        float scaleY = (float) (GamePanel.HEIGHT * GamePanel.SCALE) / panelHeight;
        int logicalX = (int) (x * scaleX);
        int logicalY = (int) (y * scaleY);

        int offsetX = (GamePanel.WIDTH - PANEL_SIZE) / 2;
        int offsetY = (GamePanel.HEIGHT - PANEL_SIZE) / 2;

        int gridX = (logicalX - offsetX - GAP) / (CELL_SIZE + GAP);
        int gridY = (logicalY - offsetY - GAP) / (CELL_SIZE + GAP);

        if (gridX >= 0 && gridX < GRID_SIZE && gridY >= 0 && gridY < GRID_SIZE &&
                (logicalX - offsetX - GAP) % (CELL_SIZE + GAP) < CELL_SIZE &&
                (logicalY - offsetY - GAP) % (CELL_SIZE + GAP) < CELL_SIZE) {

            //System.out.println("Valid click registered at grid position: ("+gridY+","+gridX+")");
            clickedCell = new Point(gridY, gridX);
            clickTimer = System.currentTimeMillis();
            playerInput.add(new Point(gridY, gridX));

            if (playerInput.get(playerInput.size() - 1).equals(sequence.get(playerInput.size() - 1))) {
                if (playerInput.size() == currentSequenceLength) {
                    currentSequenceLength++;
                    currentIndex = 0;
                    playerInput.clear();

                    if (currentSequenceLength > 5) {
                        completed = true;
                        showingSequence = false;
                        awaitingInput = false;
                        System.out.println("Terminal completed");
                    } else {
                        generateSequence();
                        showingSequence = true;
                        flashOn = true;
                        awaitingInput = false;
                        flashTimer = System.currentTimeMillis();
                    }
                }
            } else {
                System.out.println("Incorrect sequence, resetting");
                currentSequenceLength = 1;
                currentIndex = 0;
                playerInput.clear();
                sequence.clear();
                sequence.add(new Point(random.nextInt(GRID_SIZE), random.nextInt(GRID_SIZE)));
                showingSequence = true;
                flashOn = true;
                awaitingInput = false;
                flashTimer = System.currentTimeMillis();
            }
        } else {
            System.out.println("Click outside grid or in gap: gridX=" + gridX + ", gridY=" + gridY);
        }
    }

    public void close() {

        active = false;
        awaitingInput = false;
        showingSequence = false;
    }

    public boolean isActive() {
        return active;
    }
    public boolean isPuzzleSolved() {
        return puzzleSolved;
    }

    public void setPuzzleSolved(boolean solved) {
        this.puzzleSolved = solved;
    }

    public boolean isCompleted() {
        return completed;
    }
}