package Main;

import java.awt.event.KeyEvent;
import java.io.*;
import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;

public class KeybindManager {

    private EnumMap<GameAction, Integer> keybinds;
    private final Properties properties;
    private final String configFile = "keybinds.properties";

    public KeybindManager() {
        keybinds = new EnumMap<>(GameAction.class);
        properties = new Properties();
        loadKeybinds();
    }

    private void setDefaultKeybinds() {

        keybinds.clear();

        keybinds.put(GameAction.MOVE_LEFT, KeyEvent.VK_A);
        keybinds.put(GameAction.MOVE_RIGHT, KeyEvent.VK_D);
        keybinds.put(GameAction.MOVE_UP, KeyEvent.VK_W);
        keybinds.put(GameAction.JUMP, KeyEvent.VK_W);
        keybinds.put(GameAction.MOVE_DOWN, KeyEvent.VK_S);
        keybinds.put(GameAction.GLIDE, KeyEvent.VK_SHIFT);
        keybinds.put(GameAction.SCRATCH, KeyEvent.VK_R);
        keybinds.put(GameAction.FIRE, KeyEvent.VK_F);
        keybinds.put(GameAction.INTERACT, KeyEvent.VK_E);
        keybinds.put(GameAction.OPEN_CHAT, KeyEvent.VK_SLASH);
        keybinds.put(GameAction.DEBUG_TOGGLE, KeyEvent.VK_F3);

        System.out.println("Default keybinds set.");
        saveKeybinds();
    }

    public void loadKeybinds() {
        File configFile = new File(this.configFile);

        if (!configFile.exists()) {
            System.out.println("Config file not found. Loading and saving default keybinds.");
            setDefaultKeybinds();
            return;
        }

        try (InputStream input = new FileInputStream(configFile)) {

            properties.load(input);
            keybinds.clear();
            boolean allKeysLoadedSuccessfully = true;

            for (GameAction action : GameAction.values()) {
                String keyCodeString = properties.getProperty(action.name());

                if (keyCodeString != null) {

                    try {
                        keybinds.put(action, Integer.parseInt(keyCodeString));
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing keycode for " + action.name() + ": " + keyCodeString + ". Using default for this action.");
                        setSpecificDefault(action);
                        allKeysLoadedSuccessfully = false;
                    }

                } else {
                    System.out.println("Keybind for " + action.name() + " not found in config. Using default for this action.");
                    setSpecificDefault(action);
                    allKeysLoadedSuccessfully = false;
                }
            }
            if (allKeysLoadedSuccessfully) {
                System.out.println("Keybinds loaded successfully from " + this.configFile);
            } else {
                System.out.println("Some keybinds were missing or invalid; defaults applied where necessary. Consider re-saving settings.");
                saveKeybinds();
            }


        } catch (IOException e) {
            System.err.println("Error loading keybinds from " + this.configFile + ". Loading default keybinds. " + e.getMessage());
            setDefaultKeybinds();
        }
    }

    private void setSpecificDefault(GameAction action) {

        switch (action) {

            case MOVE_LEFT: keybinds.put(GameAction.MOVE_LEFT, KeyEvent.VK_A); break;
            case MOVE_RIGHT: keybinds.put(GameAction.MOVE_RIGHT, KeyEvent.VK_D); break;
            case MOVE_UP: keybinds.put(GameAction.MOVE_UP, KeyEvent.VK_W); break;
            case JUMP: keybinds.put(GameAction.JUMP, KeyEvent.VK_W); break;
            case MOVE_DOWN: keybinds.put(GameAction.MOVE_DOWN, KeyEvent.VK_S); break;
            case GLIDE: keybinds.put(GameAction.GLIDE, KeyEvent.VK_SHIFT); break;
            case SCRATCH: keybinds.put(GameAction.SCRATCH, KeyEvent.VK_R); break;
            case FIRE: keybinds.put(GameAction.FIRE, KeyEvent.VK_F); break;
            case INTERACT: keybinds.put(GameAction.INTERACT, KeyEvent.VK_E); break;
            case OPEN_CHAT: keybinds.put(GameAction.OPEN_CHAT, KeyEvent.VK_SLASH); break;
            case DEBUG_TOGGLE: keybinds.put(GameAction.DEBUG_TOGGLE, KeyEvent.VK_F3); break;
            default: System.err.println("No specific default for action: " + action.name()); break;
        }
    }

    public void saveKeybinds() {

        properties.clear();

        for (Map.Entry<GameAction, Integer> entry : keybinds.entrySet()) {
            properties.setProperty(entry.getKey().name(), Integer.toString(entry.getValue()));
        }

        try (OutputStream output = new FileOutputStream(configFile)) {
            properties.store(output, "Keybinds");
            System.out.println("Keybinds saved to " + configFile);

        } catch (IOException e) {
            System.err.println("Error saving keybinds to " + configFile + ". " + e.getMessage());
            e.printStackTrace();
        }
    }

    public int getKeyCode(GameAction action) {
        return keybinds.getOrDefault(action, -1);
    }

    public boolean setKeybind(GameAction actionToChange, int newKeyCode) {

        for (Map.Entry<GameAction, Integer> entry : keybinds.entrySet()) {

            if (entry.getValue() == newKeyCode && entry.getKey() != actionToChange) {

                System.out.println("Warning: Key " + KeyEvent.getKeyText(newKeyCode) +
                        " is already bound to " + entry.getKey().name() +
                        ". Assigning it to " + actionToChange.name() + " as well.");
            }
        }

        keybinds.put(actionToChange, newKeyCode);
        System.out.println(actionToChange.name() + " bound to " + KeyEvent.getKeyText(newKeyCode));
        return true;
    }

    public Map<GameAction, Integer> getAllKeybinds() {
        return new EnumMap<>(keybinds);
    }

    public void resetToDefaults() {
        System.out.println("Resetting keybinds to default values.");
        setDefaultKeybinds();
    }
}