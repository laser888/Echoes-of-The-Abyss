package Main;

import Data.GameData;
import java.awt.event.KeyEvent;
import java.util.EnumMap;
import java.util.Map;

public class KeybindManager {

    private final GameData gameData;
    private final Map<GameAction, Integer> defaultKeybinds;
    private Map<GameAction, Integer> currentKeybinds;

    public KeybindManager(GameData gameData) {
        this.gameData = gameData;
        this.currentKeybinds = new EnumMap<>(GameAction.class);
        this.defaultKeybinds = new EnumMap<>(GameAction.class);

        initializeDefaults();

        loadKeybindsFromGameData();
    }

    private void initializeDefaults() {
        defaultKeybinds.put(GameAction.MOVE_LEFT, KeyEvent.VK_A);
        defaultKeybinds.put(GameAction.MOVE_RIGHT, KeyEvent.VK_D);
        defaultKeybinds.put(GameAction.MOVE_UP, KeyEvent.VK_W);
        defaultKeybinds.put(GameAction.JUMP, KeyEvent.VK_W);
        defaultKeybinds.put(GameAction.MOVE_DOWN, KeyEvent.VK_S);
        defaultKeybinds.put(GameAction.GLIDE, KeyEvent.VK_SHIFT);
        defaultKeybinds.put(GameAction.SCRATCH, KeyEvent.VK_R);
        defaultKeybinds.put(GameAction.FIRE, KeyEvent.VK_F);
        defaultKeybinds.put(GameAction.INTERACT, KeyEvent.VK_E);
        defaultKeybinds.put(GameAction.OPEN_CHAT, KeyEvent.VK_SLASH);
        defaultKeybinds.put(GameAction.DEBUG_TOGGLE, KeyEvent.VK_F3);
        defaultKeybinds.put(GameAction.TAB_TOGGLE, KeyEvent.VK_TAB);
    }

    private void loadKeybindsFromGameData() {
        //System.out.println("Loading keybinds, gameData=" + (gameData != null ? gameData : "null"));

        if (gameData == null || gameData.keybinds == null || gameData.keybinds.isEmpty()) {
            //System.out.println("No keybinds found in save data. Loading defaults.");

            resetToDefaults();
            return;
        }
       // System.out.println("Loading keybinds from save data: " + gameData.keybinds);

        for (GameAction action : GameAction.values()) {
            Integer keyCode = gameData.keybinds.get(action.name());
            if (keyCode != null) {
                currentKeybinds.put(action, keyCode);

            } else {
                currentKeybinds.put(action, defaultKeybinds.get(action));
            }
        }
        //System.out.println("Current keybinds after loading: " + currentKeybinds);
    }

    public void saveKeybindsToGameData() {
        if (gameData == null) {
            //System.err.println("Cannot save keybinds, GameData object is null.");
            return;
        }

        //System.out.println("Before saving: currentKeybinds=" + currentKeybinds);
        //System.out.println("Before saving: gameData.keybinds=" + gameData.keybinds);
        gameData.keybinds.clear();

        for (Map.Entry<GameAction, Integer> entry : currentKeybinds.entrySet()) {
            gameData.keybinds.put(entry.getKey().name(), entry.getValue());
        }
        //System.out.println("After saving: gameData.keybinds=" + gameData.keybinds);
    }

    public void resetToDefaults() {
        this.currentKeybinds.clear();
        this.currentKeybinds.putAll(defaultKeybinds);
        saveKeybindsToGameData();
    }

    public int getKeyCode(GameAction action) {
        return currentKeybinds.getOrDefault(action, -1);
    }

    public Map<GameAction, Integer> getAllKeybinds() {
        return new EnumMap<>(currentKeybinds);
    }

    public void setKeybind(GameAction action, int keyCode) {
        currentKeybinds.put(action, keyCode);
    }
}
