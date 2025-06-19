package Main;

import Data.GameData;
import java.awt.event.KeyEvent;
import java.util.EnumMap;
import java.util.Map;

// Manages keybindings for game actions
public class KeybindManager {

    private final GameData gameData; // Reference to save data
    private final Map<GameAction, Integer> defaultKeybinds; // Default keybinds
    private Map<GameAction, Integer> currentKeybinds; // Active keybinds

    // Initializes keybinds using GameData
    public KeybindManager(GameData gameData) {
        this.gameData = gameData;
        this.currentKeybinds = new EnumMap<>(GameAction.class);
        this.defaultKeybinds = new EnumMap<>(GameAction.class);

        initializeDefaults();
        loadKeybindsFromGameData();
    }

    // Sets default keybinds
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

    // Loads saved keybinds or uses defaults if missing
    private void loadKeybindsFromGameData() {
        if (gameData == null || gameData.keybinds == null || gameData.keybinds.isEmpty()) {
            resetToDefaults();
            return;
        }

        for (GameAction action : GameAction.values()) {
            Integer keyCode = gameData.keybinds.get(action.name());
            if (keyCode != null) {
                currentKeybinds.put(action, keyCode);
            } else {
                currentKeybinds.put(action, defaultKeybinds.get(action));
            }
        }
    }

    // Saves current keybinds to GameData
    public void saveKeybindsToGameData() {
        if (gameData == null) return;

        gameData.keybinds.clear();
        for (Map.Entry<GameAction, Integer> entry : currentKeybinds.entrySet()) {
            gameData.keybinds.put(entry.getKey().name(), entry.getValue());
        }
    }

    // Resets keybinds to defaults and saves them
    public void resetToDefaults() {
        this.currentKeybinds.clear();
        this.currentKeybinds.putAll(defaultKeybinds);
        saveKeybindsToGameData();
    }

    // Returns key code for action
    public int getKeyCode(GameAction action) {
        return currentKeybinds.getOrDefault(action, -1);
    }

    // Returns all active keybinds
    public Map<GameAction, Integer> getAllKeybinds() {
        return new EnumMap<>(currentKeybinds);
    }

    // Sets keybind for a specific action
    public void setKeybind(GameAction action, int keyCode) {
        currentKeybinds.put(action, keyCode);
    }
}
