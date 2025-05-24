package GameState;

import Main.GamePanel;
import Main.KeybindManager;

import java.util.ArrayList;


public class GameStateManager {

    private ArrayList<GameState> gameStates;
    private int currentState;
    private KeybindManager keybindManager;

    public static final int WINNINGSTATE = 2;
    public static final int MENUSTATE = 0;
    public static final int LEVEL1STATE = 1;
    public static final int CATSTATE = 3;
    public static final int SETTINGSSTATE = 4;


    public GameStateManager(KeybindManager kbm, GamePanel gamePanel) {
        this.keybindManager = kbm;
        gameStates = new ArrayList<GameState>();

        currentState = MENUSTATE;
        gameStates.add(new MenuState(this, gamePanel));
        gameStates.add(new Level1State(this, gamePanel));
        gameStates.add(new WinState(this, gamePanel));
        gameStates.add(new CatState(this));
        gameStates.add(new SettingsState(this, gamePanel));
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

}
