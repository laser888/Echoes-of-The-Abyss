package Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GameData implements Serializable {

    public Map<String, Map<String, Integer>> playerClassProgress;

    public Map<String, Integer> keybinds;

    public Set<String> completedLevels;

    public GameData() {
        playerClassProgress = new HashMap<>();
        completedLevels = new HashSet<>();
        keybinds = new HashMap<>();
    }
}