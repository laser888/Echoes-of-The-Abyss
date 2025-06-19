package GameState;

import Data.GameData;
import Entity.EntityManager;
import Entity.HUD;
import Entity.Player;
import Main.GameAction;
import Main.GamePanel;
import Score.ScoreData;
import Score.ScoreManager;
import Terminals.SimonSays;
import TileMap.Background;
import TileMap.TileMap;
import Main.KeybindManager;
import Effects.DamageNumber;
import TileMap.TerminalTile;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.security.Key;
import java.util.ArrayList;

// Base class for level states
public abstract class BaseLevelState extends GameState {
    protected GamePanel gamePanel; // Game panel reference
    protected TileMap tileMap; // Tile map
    protected Background bg; // Background
    protected Player player; // Player reference
    protected HUD hud; // Heads-up display
    protected EntityManager entityManager; // Manages enemies, explosions, damage numbers
    protected KeybindManager keybindManager; // Keybind manager

    private boolean controlPressed = false; // Control key state
    private final java.util.Deque<TileChange> undoStack = new java.util.ArrayDeque<>(); // Undo stack for tile edits

    // Score tracking
    protected ScoreManager scoreManager; // Score manager
    protected long levelStartTimeMillis; // Level start time
    protected int enemiesKilledCount; // Enemies killed
    protected int totalEnemiesAtStart; // Total enemies at start
    protected int puzzlesSolvedCount; // Puzzles solved
    protected int totalPuzzlesInLevel; // Total puzzles in level
    protected int playerDeathCount; // Player deaths
    protected double parTimeSeconds; // Par time for level

    // UI elements
    protected boolean showStatsScreen = false; // Stats screen visibility
    protected Font statsFont; // Stats font
    protected Font pauseFont; // Pause menu font
    protected Font statsTitleFont; // Stats title font
    private int currentChoice = 0; // Pause menu choice
    private String[] options = { "Main Menu", "Settings", "Return" }; // Pause menu options
    private boolean paused = false; // Pause state

    // Chat system
    public static boolean isTyping = false; // Typing state
    public static StringBuilder typedText = new StringBuilder(); // Typed text buffer
    protected ArrayList<String> chatHistory = new ArrayList<>(); // Chat history
    protected int chatIndex = -1; // Chat history index

    // Developer and edit tools
    protected boolean editMode = false; // Edit mode state
    private boolean devMode = false; // Developer mode state
    protected int selectedTile = 1; // Selected tile ID

    public static boolean inTerminal = false; // Terminal interaction state

    private boolean leftPressed = false, rightPressed = false, upPressed = false, downPressed = false; // Camera movement flags

    // Initializes state
    public BaseLevelState(GameStateManager gsm, GamePanel gamePanel) {
        this.gsm = gsm;
        this.gamePanel = gamePanel;
        this.keybindManager = gsm.getKeybindManager();
        this.scoreManager = new ScoreManager();
    }

    // Initializes common level components
    protected void initCommonLevelComponents() {
        this.hud = new HUD(player, this);
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

    // Loads level-specific assets
    protected abstract void loadLevelSpecifics();

    // Populates level entities
    protected abstract void populateLevelEntities();

    // Updates level-specific logic
    protected abstract void updateLevelSpecificLogic();

    // Draws level-specific elements
    protected abstract void drawLevelSpecificElements(Graphics2D g);

    // Initializes level
    @Override
    public void init() {
        this.isInitialized = false;
        loadLevelSpecifics();
        initCommonLevelComponents();
        populateLevelEntities();
        setInitialized(true);
    }

    // Updates state
    @Override
    public void update() {
        if (player == null) return;
        if (!paused) {
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
                // Moves camera in edit mode
                int moveSpeed = 5;
                if (leftPressed) tileMap.setPosition(tileMap.getx() + moveSpeed, tileMap.gety());
                if (rightPressed) tileMap.setPosition(tileMap.getx() - moveSpeed, tileMap.gety());
                if (upPressed) tileMap.setPosition(tileMap.getx(), tileMap.gety() + moveSpeed);
                if (downPressed) tileMap.setPosition(tileMap.getx(), tileMap.gety() - moveSpeed);
            }
        }
    }

    // Draws state
    @Override
    public void draw(Graphics2D g) {
        if (tileMap == null || player == null) return;
        if (bg != null) bg.draw(g);
        tileMap.draw(g);
        player.draw(g);
        if (editMode) {
            // Draws edit mode GUI
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(5, 5, 150, 40);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.drawString("EDIT MODE", 10, 20);
            g.drawString("Selected Tile: " + selectedTile, 10, 40);
            // Highlights tile under mouse
            Point mousePos = gamePanel.getMousePosition();
            if (mousePos != null) {
                int panelW = gamePanel.getCurrentWidth();
                int panelH = gamePanel.getCurrentHeight();
                float scaleX = (float) GamePanel.WIDTH / panelW;
                float scaleY = (float) GamePanel.HEIGHT / panelH;
                int logicalX = (int) (mousePos.x * scaleX);
                int logicalY = (int) (mousePos.y * scaleY);
                int tileCol = (int) ((logicalX - tileMap.getx()) / tileMap.getTileSize());
                int tileRow = (int) ((logicalY - tileMap.gety()) / tileMap.getTileSize());
                if (tileRow >= 0 && tileRow < tileMap.getNumRows() && tileCol >= 0 && tileCol < tileMap.getNumCols()) {
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
        if (!editMode && hud != null) hud.draw(g);
        if (showStatsScreen) {
            drawStatsScreen(g);
        }
        if (paused) {
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

    // Starts screen flash effect
    public void startScreenFlash(long durationMillis) {
        this.screenIsFlashing = true;
        this.flashDuration = durationMillis;
        this.flashStartTime = System.currentTimeMillis();
    }

    // Draws pause screen
    protected void drawPauseScreen(Graphics2D g) {
        if (player == null) return;
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT);
        int boxWidth = 200;
        int boxHeight = 180;
        int boxX = (GamePanel.WIDTH - boxWidth) / 2;
        int boxY = (GamePanel.HEIGHT - boxHeight) / 2;
        // Draws pause box
        g.setColor(new Color(30, 30, 30, 200));
        g.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 20, 20);
        g.setColor(Color.WHITE);
        g.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 20, 20);
        // Draws title
        g.setFont(statsTitleFont);
        String title = "GAME PAUSED";
        FontMetrics fmTitle = g.getFontMetrics();
        int titleWidth = fmTitle.stringWidth(title);
        int titleY = boxY + fmTitle.getAscent() + 15;
        g.drawString(title, boxX + (boxWidth - titleWidth) / 2, titleY);
        // Draws options
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
                g.drawString(">", x - 15, y);
            } else {
                g.setColor(Color.LIGHT_GRAY);
            }
            g.drawString(option, x, y);
        }
    }

    // Handles pause menu selection
    private void select() {
        if (currentChoice == 0) {
            gsm.setState(GameStateManager.MENUSTATE);
        }
        if (currentChoice == 1) {
            gsm.setState(GameStateManager.SETTINGSSTATE);
        }
        if (currentChoice == 2) {
            paused = false;
        }
    }

    // Draws stats screen
    protected void drawStatsScreen(Graphics2D g) {
        if (player == null) return;
        // Sets fonts
        g.setFont(statsTitleFont);
        FontMetrics fmTitle = g.getFontMetrics();
        g.setFont(statsFont);
        FontMetrics fm = g.getFontMetrics();
        // Prepares stats text
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
        // Calculates box dimensions
        int padding = 16;
        int lineSpacing = fm.getHeight();
        int titleSpacing = fmTitle.getHeight();
        int xpBarHeight = 10;
        int totalHeight = titleSpacing + padding + stats.length * lineSpacing + padding + xpBarHeight;
        int boxWidth = 300;
        int boxHeight = totalHeight;
        int boxX = (GamePanel.WIDTH - boxWidth) / 2;
        int boxY = Math.max(10, (GamePanel.HEIGHT - boxHeight) / 2);
        // Draws background box
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 16, 16);
        g.setColor(Color.WHITE);
        g.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 16, 16);
        // Draws title
        g.setFont(statsTitleFont);
        int titleX = boxX + (boxWidth - fmTitle.stringWidth(title)) / 2;
        int titleY = boxY + padding + fmTitle.getAscent();
        g.drawString(title, titleX, titleY);
        // Draws stat lines
        g.setFont(statsFont);
        int y = titleY + padding;
        for (String line : stats) {
            int lineX = boxX + padding;
            g.drawString(line, lineX, y);
            y += lineSpacing;
        }
        // Draws XP progress bar
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

    protected boolean screenIsFlashing = false; // Screen flash state
    protected long flashStartTime; // Flash start time
    protected long flashDuration; // Flash duration

    // Handles key press
    @Override
    public void keyPressed(int k) {
        if (k == KeyEvent.VK_ESCAPE && !showStatsScreen && !inTerminal) {
            paused = !paused;
        }
        if (!paused) {
            if (isTyping) {
                handleTypingInput(k);
                return;
            }
            // Toggles edit mode in dev mode
            if (devMode && k == KeyEvent.VK_F1) {
                editMode = !editMode;
                if (editMode && player != null) {
                    player.setVector(0, 0);
                }
                return;
            }
            // Tracks control key
            if (k == KeyEvent.VK_CONTROL) controlPressed = true;
            // Saves map in edit mode
            if (editMode && controlPressed && k == KeyEvent.VK_S) {
                String savePath = "src/main/resources/Maps/edited.map";
                tileMap.saveMap(savePath);
            }
            // Adds row in edit mode
            if (editMode && k == KeyEvent.VK_R) {
                tileMap.addRow();
                tileMap.fixBounds();
                gamePanel.repaint();
            }
            // Adds column in edit mode
            if (editMode && k == KeyEvent.VK_C) {
                tileMap.addColumn();
                tileMap.fixBounds();
                gamePanel.repaint();
            }
            if (editMode) {
                // Changes selected tile
                if (k == KeyEvent.VK_EQUALS || k == KeyEvent.VK_PLUS) {
                    selectedTile++;
                    if (selectedTile > 2 * tileMap.getNumTilesAcross() - 1) selectedTile = 1;
                }
                if (k == KeyEvent.VK_MINUS) {
                    selectedTile--;
                    if (selectedTile < 1) selectedTile = 2 * tileMap.getNumTilesAcross() - 1;
                }
                // Undoes tile change
                if (controlPressed && k == KeyEvent.VK_Z && !undoStack.isEmpty()) {
                    TileChange last = undoStack.pop();
                    tileMap.setTile(last.row, last.col, last.oldId);
                    gamePanel.repaint();
                    return;
                }
            }
            // Opens chat
            if (k == keybindManager.getKeyCode(GameAction.OPEN_CHAT)) {
                isTyping = true;
                typedText.setLength(0);
                chatIndex = chatHistory.size();
                return;
            }
            // Toggles stats screen
            if (k == keybindManager.getKeyCode(GameAction.TAB_TOGGLE)) {
                showStatsScreen = !showStatsScreen;
                return;
            }
            if (k == KeyEvent.VK_ESCAPE && showStatsScreen) {
                showStatsScreen = false;
                return;
            }
            // Handles camera movement in edit mode
            if (editMode) {
                if (k == keybindManager.getKeyCode(GameAction.MOVE_LEFT)) leftPressed = true;
                if (k == keybindManager.getKeyCode(GameAction.MOVE_RIGHT)) rightPressed = true;
                if (k == keybindManager.getKeyCode(GameAction.MOVE_UP)) upPressed = true;
                if (k == keybindManager.getKeyCode(GameAction.MOVE_DOWN)) downPressed = true;
            }
            // Handles player input
            if (player != null && !inTerminal) {
                if (k == keybindManager.getKeyCode(GameAction.MOVE_LEFT)) player.setLeft(true);
                if (k == keybindManager.getKeyCode(GameAction.MOVE_RIGHT)) player.setRight(true);
                if (k == keybindManager.getKeyCode(GameAction.JUMP)) player.setJumping(true);
                if (k == keybindManager.getKeyCode(GameAction.FIRE)) player.setFiring(true);
                if (k == keybindManager.getKeyCode(GameAction.SCRATCH)) player.setScratching(true);
                if (k == keybindManager.getKeyCode(GameAction.MOVE_UP)) player.setUp(true);
                if (k == keybindManager.getKeyCode(GameAction.MOVE_DOWN)) player.setDown(true);
                if (k == keybindManager.getKeyCode(GameAction.GLIDE)) player.setGliding(true);
                if (k == keybindManager.getKeyCode(GameAction.DEBUG_TOGGLE)) HUD.toggleDebug();
            }
            handleLevelSpecificKeyPressed(k);
        } else {
            // Handles pause menu navigation
            if (k == KeyEvent.VK_ENTER) {
                select();
            }
            if (k == KeyEvent.VK_UP) {
                currentChoice--;
                if (currentChoice == -1) {
                    currentChoice = options.length - 1;
                }
            }
            if (k == KeyEvent.VK_DOWN) {
                currentChoice++;
                if (currentChoice == options.length) {
                    currentChoice = 0;
                }
            }
        }
    }

    // Handles level-specific key press
    protected abstract void handleLevelSpecificKeyPressed(int k);

    // Handles chat input
    protected void handleTypingInput(int k) {
        if (k == KeyEvent.VK_ESCAPE) {
            isTyping = false;
            typedText.setLength(0);
            return;
        }
        if (k == KeyEvent.VK_ENTER) {
            if (typedText.length() > 0) {
                String command = typedText.toString();
                executeCommand(command);
                isTyping = false;
                typedText.setLength(0);
                if (!command.trim().isEmpty() && (chatHistory.isEmpty() || !chatHistory.get(chatHistory.size() - 1).equals(command))) {
                    chatHistory.add(command);
                }
            }
            isTyping = false;
            typedText.setLength(0);
            return;
        }
        if (k == KeyEvent.VK_BACK_SPACE) {
            if (typedText.length() > 0) {
                typedText.deleteCharAt(typedText.length() - 1);
            }
            return;
        }
        if (k == KeyEvent.VK_UP) {
            if (!chatHistory.isEmpty()) {
                if (chatIndex > 0) {
                    chatIndex--;
                }
                typedText.setLength(0);
                typedText.append(chatHistory.get(chatIndex));
            }
            return;
        }
        if (k == KeyEvent.VK_DOWN) {
            if (!chatHistory.isEmpty()) {
                if (chatIndex < chatHistory.size() - 1) {
                    chatIndex++;
                    typedText.setLength(0);
                    typedText.append(chatHistory.get(chatIndex));
                } else {
                    chatIndex = chatHistory.size();
                    typedText.setLength(0);
                }
            }
            return;
        }
        KeyEvent keyEvent = new KeyEvent(gamePanel, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, k, (char)k, KeyEvent.KEY_LOCATION_STANDARD);
        char keyChar = keyEvent.getKeyChar();
        if (keyChar != KeyEvent.CHAR_UNDEFINED && !Character.isISOControl(keyChar)) {
            typedText.append(keyChar);
        }
    }

    // Handles key release
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

    // Handles level-specific key release
    protected abstract void handleLevelSpecificKeyReleased(int k);

    // Handles mouse press
    public void mousePressed(MouseEvent e) {
        if (!editMode) return;
        // Paints tile
        if (e.getButton() == MouseEvent.BUTTON1) {
            paintTile(e, selectedTile);
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            paintTile(e, 0);
        }
    }

    // Adds damage number
    public void addDamageNumber(int damage, double x, double y, boolean isCrit) {
        if (entityManager != null) {
            String text = String.valueOf(damage);
            Color color = isCrit ? Color.YELLOW : Color.WHITE;
            entityManager.addDamageNumber(new DamageNumber(text, x, y, color, tileMap));
        }
    }

    // Records player death
    public void recordPlayerDeath() {
        this.playerDeathCount++;
    }

    // Completes level
    protected void levelComplete(int currentLevelId) {
        int nextLevelId = currentLevelId + 1;
        double xpMultiplier = 1.0;
        switch (currentLevelId) {
            case 6: xpMultiplier = 1.0; break;
            case 7: xpMultiplier = 2.0; break;
            case 8: xpMultiplier = 3.0; break;
            case 9: xpMultiplier = 4.0; break;
        }
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
                playerDidNotDie, xpMultiplier
        );
        if (player != null) player.addXP(finalScores.xpAwarded);
        GameData gameData = gsm.getGameData();
        if (gameData != null) {
            String levelIdentifier = this.getClass().getSimpleName();
            gameData.completedLevels.add(levelIdentifier);
        }
        gsm.saveGameData();
        WinState win = (WinState) gsm.getState(GameStateManager.WINNINGSTATE);
        win.setScoreData(finalScores);
        win.setNextLevelState(nextLevelId);
        gsm.setState(GameStateManager.WINNINGSTATE);
    }

    // Executes chat command
    protected void executeCommand(String command) {
        chatHistory.add(command);
        chatIndex = chatHistory.size();
        String[] token = command.trim().split(" ");
        if (token.length == 0 || token[0].isEmpty()) return;
        String primaryCommand = token[0].toLowerCase();
        if (primaryCommand.equals("/dev")) {
            devMode = !devMode;
            return;
        }
        if (devMode) {
            switch (primaryCommand) {
                case "/tp":
                    if (token.length == 3 && player != null) {
                        try { player.setPosition(Integer.parseInt(token[1]), Integer.parseInt(token[2])); }
                        catch (NumberFormatException e) {}
                    } break;
                case "/god": if (player != null) player.godMode(true); break;
                case "/stop": if (player != null) player.godMode(false); break;
                case "/cat":
                        gsm.setState(GameStateManager.CATSTATE);
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
        } else {
            System.out.println("Unknown command or developer mode is not enabled.");
        }
    }

    // Paints tile in edit mode
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

    // Gets player
    public Player getPlayer() {
        return player;
    }

    // Stores tile change
    private static class TileChange {
        final int row, col, oldId;
        TileChange(int r, int c, int o) { row = r; col = c; oldId = o; }
    }

    // Handles mouse drag
    public void mouseDragged(MouseEvent e) {
        if (!editMode) return;
        int mods = e.getModifiersEx();
        if ((mods & MouseEvent.BUTTON1_DOWN_MASK) != 0) {
            paintTile(e, selectedTile);
        } else if ((mods & MouseEvent.BUTTON3_DOWN_MASK) != 0) {
            paintTile(e, 0);
        }
    }

    // Handles level-specific command
    protected abstract void handleLevelSpecificCommand(String[] token);

    // Gets spawn X
    public abstract int getSpawnX();

    // Gets spawn Y
    public abstract int getSpawnY();

    // Gets entity manager
    public EntityManager getEntityManager() {
        return this.entityManager;
    }

    // Closes active terminal
    public void closeActiveTerminal() {
        tileMap.closeActiveTerminal();
        inTerminal = false;
    }
}