package GameState;

import Data.GameData;
import Entity.EntityManager;
import Entity.HUD;
import Entity.Player;
import Main.GameAction;
import Main.GamePanel;
import Score.ScoreData;
import Score.ScoreManager;
import TileMap.Background;
import TileMap.TileMap;
import Main.KeybindManager;
import Effects.DamageNumber;
import TileMap.TerminalTile;
import TileMap.TerminalTile;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.security.Key;
import java.util.ArrayList;

public abstract class BaseLevelState extends GameState {

    protected GamePanel gamePanel;
    protected TileMap tileMap;
    protected Background bg;
    protected Player player;
    protected HUD hud;
    protected EntityManager entityManager; // Manages enemies, explosions, damage numbers
    protected KeybindManager keybindManager;

    private boolean controlPressed = false;
    private final java.util.Deque<TileChange> undoStack = new java.util.ArrayDeque<>();


    // Score
    protected ScoreManager scoreManager;
    protected long levelStartTimeMillis;
    protected int enemiesKilledCount;
    protected int totalEnemiesAtStart;
    protected int puzzlesSolvedCount;
    protected int totalPuzzlesInLevel;
    protected int playerDeathCount;
    protected double parTimeSeconds;

    // UI
    protected boolean showStatsScreen = false;
    protected Font statsFont;
    protected Font pauseFont;
    protected Font statsTitleFont;
    private int currentChoice = 0;
    private String[] options = { "Main Menu", "Settings", "Return"};
    private boolean paused = false;

    // Chat
    public static boolean isTyping = false;
    public static StringBuilder typedText = new StringBuilder();
    protected ArrayList<String> chatHistory = new ArrayList<>();
    protected int chatIndex = -1;

    protected boolean editMode = false;
    protected int selectedTile = 1;

    public static boolean inTerminal = false;

    private boolean leftPressed = false, rightPressed = false, upPressed = false, downPressed = false;

    public BaseLevelState(GameStateManager gsm, GamePanel gamePanel) {
        this.gsm = gsm;
        this.gamePanel = gamePanel;
        this.keybindManager = gsm.getKeybindManager();
        this.scoreManager = new ScoreManager();
    }

    protected void initCommonLevelComponents() {
        this.hud = new HUD(player);
        this.entityManager = new EntityManager(player);
        paused = false;

        this.levelStartTimeMillis = System.currentTimeMillis();
        this.enemiesKilledCount = 0;
        this.puzzlesSolvedCount = 0;
        this.playerDeathCount = 0;

        this.statsFont = new Font("Arial", Font.PLAIN, 12);
        this.pauseFont = new Font("Arial", Font.PLAIN, 14);
        this.statsTitleFont = new Font("Arial", Font.BOLD, 16);
    }

    protected abstract void loadLevelSpecifics();     // Load map, background, set parTime, totalPuzzles
    protected abstract void populateLevelEntities();  // Create and add enemies, terminals (might change)
    protected abstract void updateLevelSpecificLogic(); // Level specific
    protected abstract void drawLevelSpecificElements(Graphics2D g); // Draw unique level elements

    @Override
    public void init() {
        this.isInitialized = false;
        //System.out.println("BaseLevelState: init() called for " + this.getClass().getSimpleName());
        loadLevelSpecifics();
        initCommonLevelComponents();
        populateLevelEntities();
        //System.out.println("BaseLevelState: init() finished for " + this.getClass().getSimpleName());
        setInitialized(true);
    }
    @Override
    public void update() {
        if (player == null) return;

        if(!paused) {
            if (!editMode) {

                player.update();

                tileMap.setPosition(GamePanel.WIDTH / 2.0 - player.getx(), GamePanel.HEIGHT / 2.0 - player.gety());

                if (bg != null) {
                    bg.setPosition(tileMap.getx(), tileMap.gety());
                }

                if (entityManager != null) {
                    entityManager.updateAll(tileMap, (this instanceof Level1State ? (Level1State) this : null));
                }

                updateLevelSpecificLogic();

            } else {
                // Camera
                int moveSpeed = 5;
                if (leftPressed) tileMap.setPosition(tileMap.getx() + moveSpeed, tileMap.gety());
                if (rightPressed) tileMap.setPosition(tileMap.getx() - moveSpeed, tileMap.gety());
                if (upPressed) tileMap.setPosition(tileMap.getx(), tileMap.gety() + moveSpeed);
                if (downPressed) tileMap.setPosition(tileMap.getx(), tileMap.gety() - moveSpeed);
            }
        }
    }

    @Override
    public void draw(Graphics2D g) {

        if (tileMap == null || player == null) return;
        if (bg != null) bg.draw(g);
        tileMap.draw(g);
        player.draw(g);

        if (editMode) {

            // Edit GUI
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(5, 5, 150, 40);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.drawString("EDIT MODE", 10, 20);
            g.drawString("Selected Tile: " + selectedTile, 10, 40);

            // Highlight
            Point mousePos = gamePanel.getMousePosition();
            if (mousePos != null) {
                int panelW = gamePanel.getCurrentWidth();
                int panelH = gamePanel.getCurrentHeight();
                float scaleX = (float) GamePanel.WIDTH  / panelW;
                float scaleY = (float) GamePanel.HEIGHT / panelH;
                int logicalX = (int) (mousePos.x * scaleX);
                int logicalY = (int) (mousePos.y * scaleY);
                int tileCol = (int) ((logicalX - tileMap.getx()) / tileMap.getTileSize());
                int tileRow = (int) ((logicalY - tileMap.gety()) / tileMap.getTileSize());

                if (tileRow >= 0 && tileRow < tileMap.getNumRows()

                        && tileCol >= 0 && tileCol < tileMap.getNumCols()) {

                    g.setColor(new Color(255, 255, 255, 100));

                    g.fillRect(
                            (int) (tileCol * tileMap.getTileSize() + tileMap.getx()),
                            (int) (tileRow * tileMap.getTileSize() + tileMap.gety()),
                            tileMap.getTileSize(),
                            tileMap.getTileSize()
                    );
                }
            }
        }

        if (entityManager != null) {
            entityManager.drawAll(g, tileMap);
        }

        drawLevelSpecificElements(g);

        if(!editMode) if (hud != null) hud.draw(g);

        if (showStatsScreen) {
            drawStatsScreen(g);
        }

        if(paused) {
            drawPauseScreen(g);
        }

        if (screenIsFlashing) {
            long elapsed = System.currentTimeMillis() - flashStartTime;
            if (elapsed < flashDuration) {

                g.setColor(new Color(0, 0, 0, 180));
                g.fillRect(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT);
            } else {
                screenIsFlashing = false;
            }
        }
    }

    public void startScreenFlash(long durationMillis) {
        this.screenIsFlashing = true;
        this.flashDuration = durationMillis;
        this.flashStartTime = System.currentTimeMillis();
    }

    protected void drawPauseScreen(Graphics2D g) {
        if (player == null) return;

        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT);

        int boxWidth = 200;
        int boxHeight = 180;
        int boxX = (GamePanel.WIDTH - boxWidth) / 2;
        int boxY = (GamePanel.HEIGHT - boxHeight) / 2;

        // Draw pause box
        g.setColor(new Color(30, 30, 30, 200));
        g.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 20, 20);
        g.setColor(Color.WHITE);
        g.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 20, 20);

        // Draw title
        g.setFont(statsTitleFont);
        String title = "GAME PAUSED";
        FontMetrics fmTitle = g.getFontMetrics();
        int titleWidth = fmTitle.stringWidth(title);
        int titleY = boxY + fmTitle.getAscent() + 15;
        g.drawString(title, boxX + (boxWidth - titleWidth) / 2, titleY);

        // Draw options
        g.setFont(pauseFont);
        FontMetrics fmOptions = g.getFontMetrics();
        int optionStartY = titleY + 30;

        for (int i = 0; i < options.length; i++) {
            String option = options[i];
            int textWidth = fmOptions.stringWidth(option);
            int x = boxX + (boxWidth - textWidth) / 2;
            int y = optionStartY + i * 25;

            if (i == currentChoice) {
                g.setColor(Color.YELLOW);
                // Optional underline:
                g.drawString(">", x - 15, y);
            } else {
                g.setColor(Color.LIGHT_GRAY);
            }

            g.drawString(option, x, y);
        }
    }

    private void select() {
        if(currentChoice == 0) {
            gsm.setState(GameStateManager.MENUSTATE);
        }
        if(currentChoice == 1) {
            gsm.setState(GameStateManager.SETTINGSSTATE);
        }
        if(currentChoice == 2) {
            paused = false;
        }
    }

    protected void drawStatsScreen(Graphics2D g) {
        if (player == null) return;

        // Fonts
        g.setFont(statsTitleFont);
        FontMetrics fmTitle = g.getFontMetrics();
        g.setFont(statsFont);
        FontMetrics fm = g.getFontMetrics();

        // Text content
        String title = "Player Stats";
        String[] stats = {
                "Health: " + player.getHealth() + " / " + player.getMaxHealth(),
                "Mana: " + player.getIntelligence() + " / " + player.getMaxIntelligence(),
                "Defence: " + player.getDefence(),
                "Strength: " + player.getStrength(),
                "Crit Chance: " + String.format("%.1f%%", player.getCritChance()),
                "Crit Damage: " + String.format("+%.1f%%", player.getCritDamage()),
                "Regen Rate: " + String.format("%.1f%%/s", player.getRegenRate()),
                "Intelligence: " + player.getIntelligence(),
                "Ability DMG: " + String.format("+%.1f%%", player.getAbilityDamageBonus()),
                "Class: " + player.getChosenClass().name(),
                "Level: " + player.getCurrentClassLevel(),
                "XP: " + player.getCurrentClassXP() + " / " + player.getCurrentClassXPToNextLevel()
        };

        // Padding & spacing
        int padding = 16;
        int lineSpacing = fm.getHeight();
        int titleSpacing = fmTitle.getHeight();
        int xpBarHeight = 10;

        int totalHeight = titleSpacing + padding + stats.length * lineSpacing + padding + xpBarHeight;

        // Box dimensions
        int boxWidth = 300;
        int boxHeight = totalHeight;
        int boxX = (GamePanel.WIDTH - boxWidth) / 2;
        int boxY = Math.max(10, (GamePanel.HEIGHT - boxHeight) / 2);  // never offscreen

        // Background box
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 16, 16);
        g.setColor(Color.WHITE);
        g.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 16, 16);

        // Title
        g.setFont(statsTitleFont);
        int titleX = boxX + (boxWidth - fmTitle.stringWidth(title)) / 2;
        int titleY = boxY + padding + fmTitle.getAscent();
        g.drawString(title, titleX, titleY);

        // Stat lines
        g.setFont(statsFont);
        int y = titleY + padding;
        for (String line : stats) {
            int lineX = boxX + padding;
            g.drawString(line, lineX, y);
            y += lineSpacing;
        }

        // XP progress bar
        int xpBarX = boxX + padding;
        int xpBarY = y;
        int xpBarWidth = boxWidth - 2 * padding;

        double percent = (double) player.getCurrentClassXP() / Math.max(1, player.getCurrentClassXPToNextLevel());
        int fill = (int) (xpBarWidth * percent);

        g.setColor(Color.DARK_GRAY);
        g.fillRect(xpBarX, xpBarY, xpBarWidth, xpBarHeight);
        g.setColor(new Color(0, 200, 0));
        g.fillRect(xpBarX, xpBarY, fill, xpBarHeight);
        g.setColor(Color.WHITE);
        g.drawRect(xpBarX, xpBarY, xpBarWidth, xpBarHeight);
    }

    protected boolean screenIsFlashing = false;
    protected long flashStartTime;
    protected long flashDuration;


    @Override
    public void keyPressed(int k) {
        if(k == KeyEvent.VK_ESCAPE && !showStatsScreen && !inTerminal) {
            paused = !paused;
        }
        if(!paused) {
            if (isTyping) {
                handleTypingInput(k);
                return;
            }
            if (k == KeyEvent.VK_F1) {

                editMode = !editMode;
                //System.out.println("Edit Mode: " + (editMode ? "ON" : "OFF"));

                if (editMode && player != null) {
                    player.setVector(0, 0);
                }
                return;
            }

            // saving
            if (k == KeyEvent.VK_CONTROL) controlPressed = true;
            if (editMode && controlPressed && k == KeyEvent.VK_S) {
                String savePath = "src/main/resources/Maps/edited.map";
                tileMap.saveMap(savePath);
                //System.out.println("Attempted to save map to " + savePath);
            }

            // Adds row
            if (editMode && k == KeyEvent.VK_R) {
                tileMap.addRow();
                tileMap.fixBounds();
                gamePanel.repaint();
            }

            // Adds column
            if (editMode && k == KeyEvent.VK_C) {
                tileMap.addColumn();
                tileMap.fixBounds();
                gamePanel.repaint();
            }

            if (editMode) {

                if (k == KeyEvent.VK_EQUALS || k == KeyEvent.VK_PLUS) {
                    selectedTile++;
                    if (selectedTile > 2 * tileMap.getNumTilesAcross() - 1) selectedTile = 1;
                    //System.out.println("Selected Tile ID: " + selectedTile);
                }

                if (k == KeyEvent.VK_MINUS) {
                    selectedTile--;
                    if (selectedTile < 1) selectedTile = 2 * tileMap.getNumTilesAcross() - 1;
                    //System.out.println("Selected Tile ID: " + selectedTile);
                }

                if (controlPressed && k == KeyEvent.VK_Z && !undoStack.isEmpty()) {
                    TileChange last = undoStack.pop();
                    tileMap.setTile(last.row, last.col, last.oldId);
                    gamePanel.repaint();
                    return;
                }

            }

            if (k == keybindManager.getKeyCode(GameAction.OPEN_CHAT)) {
                isTyping = true;
                typedText.setLength(0);
                chatIndex = chatHistory.size();
                return;
            }
            if (k == keybindManager.getKeyCode(GameAction.TAB_TOGGLE)) {
                showStatsScreen = !showStatsScreen;
                return;
            }
            if (k == KeyEvent.VK_ESCAPE && showStatsScreen) {
                showStatsScreen = false;
                return;
            }

            if (editMode) {
                if (k == keybindManager.getKeyCode(GameAction.MOVE_LEFT)) leftPressed = true;
                if (k == keybindManager.getKeyCode(GameAction.MOVE_RIGHT)) rightPressed = true;
                if (k == keybindManager.getKeyCode(GameAction.MOVE_UP)) upPressed = true;
                if (k == keybindManager.getKeyCode(GameAction.MOVE_DOWN)) downPressed = true;
            }

            if (player != null && !inTerminal) {
                if (k == keybindManager.getKeyCode(GameAction.MOVE_LEFT)) player.setLeft(true);
                if (k == keybindManager.getKeyCode(GameAction.MOVE_RIGHT)) player.setRight(true);
                if (k == keybindManager.getKeyCode(GameAction.JUMP)) player.setJumping(true);
                if (k == keybindManager.getKeyCode(GameAction.FIRE)) player.setFiring(true);
                if (k == keybindManager.getKeyCode(GameAction.SCRATCH)) player.setScratching(true);
                if (k == keybindManager.getKeyCode(GameAction.MOVE_UP)) player.setUp(true);
                if (k == keybindManager.getKeyCode(GameAction.MOVE_DOWN)) player.setDown(true);
                if (k == keybindManager.getKeyCode(GameAction.GLIDE)) player.setGliding(true);

            }
            handleLevelSpecificKeyPressed(k); // For level specific keybinds
        } else {
            if(k == KeyEvent.VK_ENTER) {
                select();
            }
            if(k == KeyEvent.VK_UP) {
                currentChoice--;
                if(currentChoice == -1) {
                    currentChoice = options.length - 1 ;
                }
            }
            if(k == KeyEvent.VK_DOWN) {
                currentChoice++;
                if(currentChoice == options.length) {
                    currentChoice = 0;
                }
            }
        }
    }

    protected abstract void handleLevelSpecificKeyPressed(int k);

    protected void handleTypingInput(int k) {
        if (k == KeyEvent.VK_ESCAPE) { isTyping = false; typedText.setLength(0); }

        else if (k == KeyEvent.VK_ENTER) {
                if (typedText.length() > 0) executeCommand(typedText.toString());
            isTyping = false; typedText.setLength(0);

        } else if (k == KeyEvent.VK_BACK_SPACE && typedText.length() > 0) {
            typedText.deleteCharAt(typedText.length() - 1);

        } else if (k == KeyEvent.VK_UP && !chatHistory.isEmpty()) { chatIndex--; typedText.setLength(0); typedText.append(chatHistory.get(chatIndex)); }
        else if (k == KeyEvent.VK_DOWN && !chatHistory.isEmpty()) { chatIndex++; typedText.setLength(0); typedText.append(chatHistory.get(chatIndex)); }
        else { char c = (char) k; if (Character.isLetterOrDigit(c) || c == ' ' || c == '/') typedText.append(c); }
    }


    @Override
    public void keyReleased(int k) {
        if (player != null) {
                if (editMode) {
                    if (k == keybindManager.getKeyCode(GameAction.MOVE_LEFT)) leftPressed = false;
                    if (k == keybindManager.getKeyCode(GameAction.MOVE_RIGHT)) rightPressed = false;
                    if (k == keybindManager.getKeyCode(GameAction.MOVE_UP)) upPressed = false;
                    if (k == keybindManager.getKeyCode(GameAction.MOVE_DOWN)) downPressed = false;
                }
                if (k == KeyEvent.VK_CONTROL) controlPressed = false;
                if (k == keybindManager.getKeyCode(GameAction.MOVE_LEFT)) player.setLeft(false);
                if (k == keybindManager.getKeyCode(GameAction.MOVE_RIGHT)) player.setRight(false);
                if (k == keybindManager.getKeyCode(GameAction.JUMP)) player.setJumping(false);
                if (k == keybindManager.getKeyCode(GameAction.FIRE)) player.setFiring(false);
                if (k == keybindManager.getKeyCode(GameAction.SCRATCH)) player.setScratching(false);
                if (k == keybindManager.getKeyCode(GameAction.MOVE_UP)) player.setUp(false);
                if (k == keybindManager.getKeyCode(GameAction.MOVE_DOWN)) player.setDown(false);
                if (k == keybindManager.getKeyCode(GameAction.GLIDE)) player.setGliding(false);

            }
            handleLevelSpecificKeyReleased(k);
    }
    protected abstract void handleLevelSpecificKeyReleased(int k);


    public void mousePressed(MouseEvent e) {
        if (!editMode) return;

        // LC
        if (e.getButton() == MouseEvent.BUTTON1) {
            paintTile(e, selectedTile);
            // RC
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            paintTile(e, 0);
        }
    }

    public void addDamageNumber(int damage, double x, double y, boolean isCrit) {
        if (entityManager != null) {
            String text = String.valueOf(damage);
            Color color = isCrit ? Color.YELLOW : Color.WHITE;
            entityManager.addDamageNumber(new DamageNumber(text, x, y, color, tileMap));
        }
    }

    public void recordPlayerDeath() {
        this.playerDeathCount++;
    }

    protected void levelComplete(int currentLevelId) {

        int nextLevelId = currentLevelId + 1;
        player.clearBlessings();

        puzzlesSolvedCount = 0;
        for (TerminalTile terminal : tileMap.getInteractiveTiles()) {
            if (terminal.isSolved()) {
                puzzlesSolvedCount++;
            }
        }
        totalPuzzlesInLevel = tileMap.getInteractiveTiles().size();
        long levelEndTimeMillis = System.currentTimeMillis();
        double actualTimeTakenSeconds = (levelEndTimeMillis - levelStartTimeMillis) / 1000.0;
        boolean playerDidNotDie = (playerDeathCount == 0);

        ScoreData finalScores = scoreManager.calculateScore(
                enemiesKilledCount,
                totalEnemiesAtStart,
                puzzlesSolvedCount,
                totalPuzzlesInLevel,
                parTimeSeconds,
                actualTimeTakenSeconds,
                playerDidNotDie
        );

        if (player != null) player.addXP(finalScores.xpAwarded);

        GameData gameData = gsm.getGameData();
        if (gameData != null) {

            String levelIdentifier = this.getClass().getSimpleName();

            gameData.completedLevels.add(levelIdentifier);
            System.out.println("Level Complete! Marking '" + levelIdentifier + "' as beaten.");
        }

        gsm.saveGameData();

        WinState win = (WinState) gsm.getState(GameStateManager.WINNINGSTATE);
        win.setScoreData(finalScores);
        win.setNextLevelState(nextLevelId);

        gsm.setState(GameStateManager.WINNINGSTATE);
    }

    protected void executeCommand(String command) {

        chatHistory.add(command);
        chatIndex = chatHistory.size();
        String[] token = command.trim().split(" ");
        if (token.length == 0 || token[0].isEmpty()) return;

        switch(token[0].toLowerCase()) {
            case "/tp":
                if(token.length == 3 && player != null) {
                    try { player.setPosition(Integer.parseInt(token[1]), Integer.parseInt(token[2])); }
                    catch (NumberFormatException e) { /* invalid coords */ }
                } break;
            case "/god": if(player != null) player.godMode(true); break;
            case "/stop": if(player != null) player.godMode(false); break;
            case "/cat":
                if (gsm != null) {
                    gsm.setState(GameStateManager.CATSTATE);
                }
                break;
            case "/level2state":
                gsm.setState(GameStateManager.LEVEL2STATE);
                break;
            case "/level1state":
                gsm.setState(GameStateManager.LEVEL1STATE);
                break;
            case "/edit":
                editMode = !editMode;
                break;
            default:
                handleLevelSpecificCommand(token);
        }
    }

    private void paintTile(MouseEvent e, int tileId) {
        int panelW = gamePanel.getCurrentWidth();
        int panelH = gamePanel.getCurrentHeight();

        float scaleX = (float) GamePanel.WIDTH / panelW;
        float scaleY = (float) GamePanel.HEIGHT / panelH;

        int logicalX = (int) (e.getX() * scaleX);
        int logicalY = (int) (e.getY() * scaleY);

        int tileCol = (int) ((logicalX - tileMap.getx()) / tileMap.getTileSize());
        int tileRow = (int) ((logicalY - tileMap.gety()) / tileMap.getTileSize());

        if (tileRow < 0 || tileRow >= tileMap.getNumRows() || tileCol < 0 || tileCol >= tileMap.getNumCols()) return;

        int old = tileMap.getMap()[tileRow][tileCol];

        if (old != tileId) {
            int px = tileCol * tileMap.getTileSize();
            int py = tileRow * tileMap.getTileSize();

            TerminalTile existing = null;
            for (TerminalTile t : tileMap.getInteractiveTiles()) {
                if (t.getPos().x == px && t.getPos().y == py) {
                    existing = t;
                    break;
                }
            }

            if (tileId == 27) {
                if (existing == null) {
                    int tileX = px / tileMap.getTileSize();
                    int tileY = py / tileMap.getTileSize();
                    TerminalTile newTerminal = new TerminalTile(px, py, 27, tileMap, gamePanel, tileX, tileY);
                    tileMap.getInteractiveTiles().add(newTerminal);
                }
                tileMap.setTile(tileRow, tileCol, 27);
            } else {
                if (existing != null) {
                    tileMap.getInteractiveTiles().remove(existing);
                }
                tileMap.setTile(tileRow, tileCol, tileId);
            }

            undoStack.push(new TileChange(tileRow, tileCol, old));
            gamePanel.repaint();
        }
    }

    public Player getPlayer() {
        return player;
    }

    private static class TileChange {
        final int row, col, oldId;

        TileChange(int r, int c, int o) { row = r; col = c; oldId = o; }
    }


    public void mouseDragged(MouseEvent e) {
        if (!editMode) return;

        int mods = e.getModifiersEx();

        if ((mods & MouseEvent.BUTTON1_DOWN_MASK) != 0) {
            paintTile(e, selectedTile);

        } else if ((mods & MouseEvent.BUTTON3_DOWN_MASK) != 0) {

            paintTile(e, 0);
        }
    }

    protected abstract void handleLevelSpecificCommand(String[] token);

    public abstract int getSpawnX();
    public abstract int getSpawnY();
    public EntityManager getEntityManager() {
        return this.entityManager;
    }
}
