package GameState;

import Main.GamePanel;
import Main.KeybindManager;
import Data.GameData;
import Data.SaveManager;
import Entity.Player;
import java.util.ArrayList;

public class GameStateManager {

    private ArrayList<GameState> gameStates;
    private int currentState;
    private GameData gameData;
    private KeybindManager keybindManager;

    private int currentLevel = 0;

    public static final int MENUSTATE = 0;
    public static final int WINNINGSTATE = 1;
    public static final int CATSTATE = 2;
    public static final int SETTINGSSTATE = 3;
    public static final int CLASSSELECTIONSTATE = 4;
    public static final int LevelSelectionState = 5;
    public static final int LEVEL1STATE = 6;
    public static final int LEVEL2STATE = 7;
    public static final int LEVEL3STATE = 8;
    public static final int LEVEL4STATE = 9;

    private Entity.Player.PlayerClass currentPlayerClassSelection = Entity.Player.PlayerClass.NONE;

    public GameStateManager(KeybindManager kbm, GamePanel gamePanel, GameData gameData) {
        this.keybindManager = kbm;
        this.gameData = gameData;

        this.gameData = SaveManager.loadGame();
        gameStates = new ArrayList<GameState>();
        currentState = MENUSTATE;
        gameStates.add(new MenuState(this, gamePanel));
        gameStates.add(new WinState(this, gamePanel));
        gameStates.add(new CatState(this));
        gameStates.add(new SettingsState(this, gamePanel));
        gameStates.add(new ClassSelectionState(this, gamePanel));
        gameStates.add(new LevelSelectionState(this, gamePanel));
        gameStates.add(new Level1State(this, gamePanel));
        gameStates.add(new Level2State(this, gamePanel));
        gameStates.add(new Level3State(this, gamePanel));
        gameStates.add(new Level4State(this, gamePanel));

    }

    public void saveGameData() {

        keybindManager.saveKeybindsToGameData();
        //System.out.println("GameStateManager: Keybinds before save: " + gameData.keybinds);

        GameState state = getCurrentState();

        if (state instanceof BaseLevelState) {
            Player player = ((BaseLevelState) state).getPlayer();

            if (player != null) {
                player.saveAllClassData();
                //System.out.println("GameStateManager: Player progress updated: " + gameData.playerClassProgress);
            }
        }

        SaveManager.saveGame(gameData);
    }

    public GameData getGameData() {
        return this.gameData;
    }

    public void setSelectedPlayerClass(Entity.Player.PlayerClass playerClass) {
        this.currentPlayerClassSelection = playerClass;
    }

    public Entity.Player.PlayerClass getSelectedPlayerClass() {
        return this.currentPlayerClassSelection;
    }

    public GameState getState(int stateIndex) {
            return gameStates.get(stateIndex);
    }

    public KeybindManager getKeybindManager() {
        return keybindManager;
    }

    public void setState(int state) {
        currentState = state;
        gameStates.get(currentState).init();
    }

    public void update() {
        gameStates.get(currentState).update();
    }

    public void draw(java.awt.Graphics2D g) {
        gameStates.get(currentState).draw(g);
    }

    public void keyPressed(int k) {
        gameStates.get(currentState).keyPressed(k);
    }

    public void keyReleased(int k) {
        gameStates.get(currentState).keyReleased(k);
    }

    public GameState getCurrentState() {
        return gameStates.get(currentState);
    }

    public void goToNextLevel() {
        currentLevel++;
        switch (currentLevel) {
            case 1: setState(LEVEL2STATE); break;
            case 2: setState(LEVEL3STATE); break;
            case 3: setState(LEVEL4STATE); break;
        }
    }

    public void saveProgress() {
        SaveManager.saveGame(this.gameData);
    }

}
