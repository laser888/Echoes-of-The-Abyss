package Data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;

// Manages saving and loading game data
public class SaveManager {
    // Path to the save file in user’s home directory
    private static final String SAVE_FILE_PATH = System.getProperty("user.home") + File.separator + "game_progress.json";
    // Gson instance for JSON serialization
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // Saves game data to a JSON file
    public static void saveGame(GameData data) {
        if (data == null) {
            System.err.println("Cannot save null GameData");
            return;
        }

        try (Writer writer = new FileWriter(SAVE_FILE_PATH)) {
            // Converts data to JSON and writes to file
            gson.toJson(data, writer);
        } catch (IOException e) {
            System.err.println("Error saving game data: " + e.getMessage());
        }
    }

    // Loads game data from a JSON file
    public static GameData loadGame() {
        File saveFile = new File(SAVE_FILE_PATH);
        if (!saveFile.exists()) {
            // Returns new data if file doesn’t exist
            return new GameData();
        }

        try (Reader reader = new FileReader(saveFile)) {
            // Parses JSON into GameData object
            GameData data = gson.fromJson(reader, GameData.class);
            return (data != null) ? data : new GameData();
        } catch (IOException | com.google.gson.JsonSyntaxException e) {
            System.err.println("Error loading game data: " + e.getMessage());
            return new GameData();
        }
    }
}