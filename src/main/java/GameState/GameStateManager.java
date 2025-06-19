package GameState;

import Main.GamePanel;
import Main.KeybindManager;
import Data.GameData;
import Data.SaveManager;
import Entity.Player;
import java.util.ArrayList;

// Manages all game states and transitions
public class GameStateManager {

    private ArrayList<GameState> gameStates; // All game states
    private int currentState; // Index of current state
    private GameData gameData; // Save/load data
    private KeybindManager keybindManager; // Keybind system

    // State index constants
    public static final int MENUSTATE = 0;
    public static final int WINNINGSTATE = 1;
    public static final int INTROSTATE = 2;
    public static final int CATSTATE = 3;
    public static final int SETTINGSSTATE = 4;
    public static final int CLASSSELECTIONSTATE = 5;
    public static final int LevelSelectionState = 6;
    public static final int LEVEL1STATE = 7;
    public static final int LEVEL2STATE = 8;
    public static final int LEVEL3STATE = 9;
    public static final int LEVEL4STATE = 10;
    public static final int CREDITSSTATE = 11;

    private Entity.Player.PlayerClass currentPlayerClassSelection = Entity.Player.PlayerClass.NONE;

    // Initializes manager and loads all states
    public GameStateManager(KeybindManager kbm, GamePanel gamePanel, GameData gameData) {
        this.keybindManager = kbm;
        this.gameData = SaveManager.loadGame(); // Always loads from file

        gameStates = new ArrayList<>();
        currentState = MENUSTATE;

        gameStates.add(new MenuState(this, gamePanel));
        gameStates.add(new WinState(this, gamePanel));
        gameStates.add(new IntroState(this));
        gameStates.add(new CatState(this));
        gameStates.add(new SettingsState(this, gamePanel));
        gameStates.add(new ClassSelectionState(this, gamePanel));
        gameStates.add(new LevelSelectionState(this, gamePanel));
        gameStates.add(new Level1State(this, gamePanel));
        gameStates.add(new Level2State(this, gamePanel));
        gameStates.add(new Level3State(this, gamePanel));
        gameStates.add(new Level4State(this, gamePanel));
        gameStates.add(new CreditsState(this, gamePanel));
    }

    // Saves all relevant game data
    public void saveGameData() {
        keybindManager.saveKeybindsToGameData();

        GameState state = getCurrentState();

        if (state instanceof BaseLevelState) {
            Player player = ((BaseLevelState) state).getPlayer();

            if (player != null) {
                player.saveAllClassData();
            }
        }

        SaveManager.saveGame(gameData);
    }

    // Returns current save data
    public GameData getGameData() {
        return this.gameData;
    }

    // Sets selected class (before level starts)
    public void setSelectedPlayerClass(Entity.Player.PlayerClass playerClass) {
        this.currentPlayerClassSelection = playerClass;
    }

    // Returns selected class
    public Entity.Player.PlayerClass getSelectedPlayerClass() {
        return this.currentPlayerClassSelection;
    }

    // Returns game state by index
    public GameState getState(int stateIndex) {
        return gameStates.get(stateIndex);
    }

    // Returns keybind manager
    public KeybindManager getKeybindManager() {
        return keybindManager;
    }

    // Changes current state and initializes it
    public void setState(int state) {
        currentState = state;
        gameStates.get(currentState).init();
    }

    // Updates current state
    public void update() {
        gameStates.get(currentState).update();
    }

    // Draws current state
    public void draw(java.awt.Graphics2D g) {
        gameStates.get(currentState).draw(g);
    }

    // Forwards key press to current state
    public void keyPressed(int k) {
        gameStates.get(currentState).keyPressed(k);
    }

    // Forwards key release to current state
    public void keyReleased(int k) {
        gameStates.get(currentState).keyReleased(k);
    }

    // Returns current active state
    public GameState getCurrentState() {
        return gameStates.get(currentState);
    }

    // Saves progress to file
    public void saveProgress() {
        SaveManager.saveGame(this.gameData);
    }
}
