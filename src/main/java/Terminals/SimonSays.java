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
    private long lastInputTime;

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
        lastInputTime = 0;
    }

    private void resetGame() {
        sequence.clear();
        playerInput.clear();
        currentSequenceLength = 1;
        currentIndex = 0;
        sequence.add(new Point(random.nextInt(GRID_SIZE), random.nextInt(GRID_SIZE)));
        showingSequence = true;
        flashOn = true;
        awaitingInput = false;
        flashTimer = System.currentTimeMillis();
        System.out.println("Game reset: new sequence=" + sequence + ", length=" + currentSequenceLength);
    }

    public void start() {
        if (completed || puzzleSolved) {
            return;
        }
        active = true;
        resetGame();
    }

    private void generateSequence() {
        int row = random.nextInt(GRID_SIZE);
        int col = random.nextInt(GRID_SIZE);
        sequence.add(new Point(row, col));
        currentIndex = 0;
        showingSequence = true;
        flashOn = true;
        awaitingInput = false;
        flashTimer = System.currentTimeMillis();
        System.out.println("Sequence now: " + sequence);
    }

    public void update() {
        if (!active || completed || puzzleSolved) {
            return;
        }

        if (showingSequence) {
            long currentTime = System.currentTimeMillis();
            long flashDuration = flashOn ? 400 : 200;
            if (currentTime >= flashTimer + flashDuration) {
                flashOn = !flashOn;
                flashTimer = currentTime;

                if (flashOn) {
                    currentIndex++;
                    if (currentIndex >= currentSequenceLength) {
                        showingSequence = false;
                        awaitingInput = true;
                        currentIndex = 0;
                        playerInput.clear();
                        flashTimer = currentTime + 400;
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
                } else if (showingSequence && flashOn && currentIndex < sequence.size() &&
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
        if (!active || !awaitingInput || completed || puzzleSolved) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastInputTime < 200) {
            System.out.println("Click ignored: too soon after last input at " + lastInputTime);
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
            clickedCell = new Point(gridY, gridX);

            clickTimer = currentTime;
            lastInputTime = currentTime;
            playerInput.add(new Point(gridY, gridX));

            if (playerInput.get(playerInput.size() - 1).equals(sequence.get(playerInput.size() - 1))) {
                if (playerInput.size() == currentSequenceLength) {
                    currentSequenceLength++;
                    currentIndex = 0;
                    playerInput.clear();

                    if (currentSequenceLength > 5) {
                        completed = true;
                        puzzleSolved = true;
                        active = false;
                        showingSequence = false;
                        awaitingInput = false;
                        System.out.println("Terminal completed");
                    } else {
                        generateSequence();
                    }
                }
            } else {
                System.out.println("Incorrect sequence, resetting");
                resetGame();
            }
        } else {
            System.out.println("Click outside grid or in gap: gridX=" + gridX + ", gridY=" + gridY);
        }
    }

    public void close() {
        active = false;
        awaitingInput = false;
        showingSequence = false;
        System.out.println("SimonSays closed: completed=" + completed + ", puzzleSolved=" + puzzleSolved);
    }

    public boolean isActive() {
        return active;
    }

    public boolean isPuzzleSolved() {
        return puzzleSolved;
    }

    public void setPuzzleSolved(boolean solved) {
        this.puzzleSolved = solved;
        if (solved) {
            completed = true;
            active = false;
            System.out.println("SimonSays setPuzzleSolved: " + solved);
        }
    }

    public boolean isCompleted() {
        return completed;
    }

    public void markSolved() {
        setPuzzleSolved(true);
        System.out.println("SimonSays markSolved called");
    }

    public boolean isSolved() {
        return puzzleSolved;
    }
}