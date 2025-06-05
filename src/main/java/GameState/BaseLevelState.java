package GameState;

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

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public abstract class BaseLevelState extends GameState {

    protected GamePanel gamePanel;
    protected TileMap tileMap;
    protected Background bg;
    protected Player player;
    protected HUD hud;
    protected EntityManager entityManager; // Manages enemies, explosions, damage numbers
    protected KeybindManager keybindManager;

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
    protected Font statsTitleFont;

    // Chat
    public static boolean isTyping = false;
    public static StringBuilder typedText = new StringBuilder();
    protected ArrayList<String> chatHistory = new ArrayList<>();
    protected int chatIndex = -1;


    public BaseLevelState(GameStateManager gsm, GamePanel gamePanel) {
        this.gsm = gsm;
        this.gamePanel = gamePanel;
        this.keybindManager = gsm.getKeybindManager();
        this.scoreManager = new ScoreManager();
    }

    protected void initCommonLevelComponents() {
        this.hud = new HUD(player);
        this.entityManager = new EntityManager(player);

        this.levelStartTimeMillis = System.currentTimeMillis();
        this.enemiesKilledCount = 0;
        this.puzzlesSolvedCount = 0;
        this.playerDeathCount = 0;

        this.statsFont = new Font("Arial", Font.PLAIN, 12);
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

        player.update();

        tileMap.setPosition(
                GamePanel.WIDTH / 2.0 - player.getx(),
                GamePanel.HEIGHT / 2.0 - player.gety()
        );
        if (bg != null) {
            bg.setPosition(tileMap.getx(), tileMap.gety());
        }

        if (entityManager != null) {
            entityManager.updateAll(tileMap, (this instanceof Level1State ? (Level1State)this : null) ); // Pass context if needed
        }

        updateLevelSpecificLogic(); // Doors, terminals, skeletons (might change), bosses
    }

    @Override
    public void draw(Graphics2D g) {
        if (tileMap == null || player == null) return;

        if (bg != null) bg.draw(g);
        tileMap.draw(g);
        player.draw(g);

        if (entityManager != null) {
            entityManager.drawAll(g, tileMap);
        }

        drawLevelSpecificElements(g);

        if (hud != null) hud.draw(g);

        if (showStatsScreen) {
            drawStatsScreen(g);
        }
    }

    protected void drawStatsScreen(Graphics2D g) {
        if (player == null) return;
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT);

        int boxWidth = 220;
        int boxHeight = 245;
        int boxX = (GamePanel.WIDTH - boxWidth) / 2;
        int boxY = (GamePanel.HEIGHT - boxHeight) / 2;

        g.setColor(new Color(50, 50, 100, 200));
        g.fillRect(boxX, boxY, boxWidth, boxHeight);
        g.setColor(Color.WHITE);
        g.drawRect(boxX, boxY, boxWidth, boxHeight);

        g.setFont(statsTitleFont);
        String title = "Player Stats";
        FontMetrics fmTitle = g.getFontMetrics(statsTitleFont);
        int titleWidth = fmTitle.stringWidth(title);
        g.drawString(title, boxX + (boxWidth - titleWidth) / 2, boxY + fmTitle.getAscent() + 5);

        g.setFont(statsFont);
        FontMetrics fmStats = g.getFontMetrics(statsFont);
        int lineHeight = fmStats.getHeight() + 3;
        int lineY = boxY + fmTitle.getAscent() + 5 + fmTitle.getHeight() + 10;
        int paddingLeft = boxX + 15;
        g.setColor(Color.WHITE);

        g.drawString("Health: " + player.getHealth() + " / " + player.getMaxHealth(), paddingLeft, lineY); lineY += lineHeight;
        g.drawString("Mana: " + player.getIntelligence() + " / " + player.getMaxIntelligence(), paddingLeft, lineY); lineY += lineHeight;
        g.drawString("Defence: " + player.getDefence(), paddingLeft, lineY); lineY += lineHeight;
        g.drawString("Strength: " + player.getStrength(), paddingLeft, lineY); lineY += lineHeight;
        g.drawString("Crit Chance: " + String.format("%.1f%%", player.getCritChance()), paddingLeft, lineY); lineY += lineHeight;
        g.drawString("Crit Damage: " + String.format("+%.1f%%", player.getCritDamage()), paddingLeft, lineY); lineY += lineHeight;
        g.drawString("Regen: " + String.format("%.1f%%/s", player.getRegenRate()), paddingLeft, lineY); lineY += lineHeight;
        g.drawString("Intelligence: " + player.getIntelligence(), paddingLeft, lineY); lineY += lineHeight;
        g.drawString("Ability DMG Bonus: " + String.format("+%.1f%%", player.getAbilityDamageBonus()), paddingLeft, lineY); lineY += lineHeight;

        // Class info
        lineY += 5;
        g.setColor(Color.CYAN);
        g.drawString("Class: " + player.getChosenClass().name(), paddingLeft, lineY); lineY += lineHeight;
        g.drawString("Level: " + player.getCurrentClassLevel(), paddingLeft, lineY); lineY += lineHeight;
        g.drawString(String.format("XP: %d / %d", player.getCurrentClassXP(), player.getCurrentClassXPToNextLevel()), paddingLeft, lineY);
        lineY += lineHeight - 5;

        // XP Bar
        int barMaxWidth = boxWidth - 30;
        int barHeight = 10;
        int barX = paddingLeft;
        int barY = lineY;
        double xpProgressRatio = 0;
        if (player.getCurrentClassXPToNextLevel() > 0) {
            xpProgressRatio = (double) player.getCurrentClassXP() / player.getCurrentClassXPToNextLevel();
        }
        int currentProgressWidth = (int) (barMaxWidth * xpProgressRatio);
        g.setColor(new Color(70, 70, 70));
        g.fillRect(barX, barY, barMaxWidth, barHeight);
        g.setColor(new Color(50, 200, 50));
        g.fillRect(barX, barY, currentProgressWidth, barHeight);
        g.setColor(Color.WHITE);
        g.drawRect(barX, barY, barMaxWidth, barHeight);
        lineY += barHeight + 10;

//        g.setFont(new Font("Arial", Font.ITALIC, 10));
//        FontMetrics fmClose = g.getFontMetrics();
//        g.setColor(Color.LIGHT_GRAY);
//        String closeMsg = "Press " + KeyEvent.getKeyText(keybindManager.getKeyCode(GameAction.TAB_TOGGLE)) + " or ESC to close";
//        int closeMsgWidth = fmClose.stringWidth(closeMsg);
//        g.drawString(closeMsg, boxX + (boxWidth - closeMsgWidth) / 2, boxY + boxHeight - fmClose.getDescent() - 5);
    }


    @Override
    public void keyPressed(int k) {
        if (isTyping) {
            handleTypingInput(k);
            return;
        }

        // if (terminal != null && terminal.isActive()) {
        //    if (k == KeyEvent.VK_ESCAPE) terminal.close();
        //    terminal.keyPressed(k);
        //    return;
        // }

        if (k == keybindManager.getKeyCode(GameAction.OPEN_CHAT)) {
            isTyping = true; typedText.setLength(0); chatIndex = chatHistory.size(); return;
        }
        if (k == keybindManager.getKeyCode(GameAction.TAB_TOGGLE)) {
            showStatsScreen = !showStatsScreen; return;
        }
        if (k == KeyEvent.VK_ESCAPE && showStatsScreen) {
            showStatsScreen = false; return;
        }

        if (player != null && !showStatsScreen /* && (terminal == null || !terminal.isActive()) */) {
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


    @Override
    public abstract void mousePressed(MouseEvent e);

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

    protected void levelComplete() {
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

        GameState potentialWinState = gsm.getState(GameStateManager.WINNINGSTATE);
        if (potentialWinState instanceof WinState) {
            ((WinState) potentialWinState).setScoreData(finalScores);
        }
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
            case "/win": levelComplete(); break;
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
            default:
                handleLevelSpecificCommand(token);
        }
    }
    protected abstract void handleLevelSpecificCommand(String[] token);

    public abstract int getSpawnX();
    public abstract int getSpawnY();
}
