package Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// Stores player progress, keybinds, and completed levels for saving
public class GameData implements Serializable {
    // Tracks progress for each player class (level, XP)
    public Map<String, Map<String, Integer>> playerClassProgress;
    // Stores keybind mappings
    public Map<String, Integer> keybinds;
    // Keeps track of completed level IDs
    public Set<String> completedLevels;

    // Initializes empty data structures for game state
    public GameData() {
        playerClassProgress = new HashMap<>(); // Sets up class progress map
        keybinds = new HashMap<>(); // Sets up keybind map
        completedLevels = new HashSet<>(); // Sets up completed levels set
    }
}