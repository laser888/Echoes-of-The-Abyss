package Data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

// Manages saving and loading game data
public class SaveManager {
    // Directory for saving game data (C:\Users<username>\.EchoesOfTheAbyss\game_progress.json)
    private static final String SAVE_DIR = System.getProperty("user.home") + File.separator + ".EchoesOfTheAbyss";
    // Path to the save file
    private static final String SAVE_FILE_PATH = SAVE_DIR + File.separator + "game_progress.json";
    // Resource path for default game data
    private static final String DEFAULT_RESOURCE_PATH = "/game_progress.json";
    // Gson instance for JSON serialization
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // Ensures save directory exists
    private static void ensureSaveDirectoryExists() {
        File saveDir = new File(SAVE_DIR);
        if (!saveDir.exists()) {
            boolean created = saveDir.mkdirs();
            if (!created) {
                System.err.println("Failed to create save directory: " + SAVE_DIR);
            }
        }
    }

    // Saves game data to a JSON file
    public static void saveGame(GameData data) {
        if (data == null) {
            System.err.println("Cannot save null GameData");
            return;
        }

        ensureSaveDirectoryExists();

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
        ensureSaveDirectoryExists();

        if (saveFile.exists()) {
            try (Reader reader = new FileReader(saveFile)) {
                // Parses JSON into GameData object
                GameData data = gson.fromJson(reader, GameData.class);
                return (data != null) ? data : new GameData();
            } catch (IOException | com.google.gson.JsonSyntaxException e) {
                System.err.println("Error loading game data: " + e.getMessage());
            }
        }

        // Loads default from resources
        try (InputStream inputStream = SaveManager.class.getResourceAsStream(DEFAULT_RESOURCE_PATH)) {
            if (inputStream != null) {
                try (Reader reader = new InputStreamReader(inputStream)) {
                    // Parses JSON from resources
                    GameData data = gson.fromJson(reader, GameData.class);
                    return (data != null) ? data : new GameData();
                }
            }
            // Returns new data if no default exists
            return new GameData();
        } catch (IOException | com.google.gson.JsonSyntaxException e) {
            System.err.println("Error loading default game data: " + e.getMessage());
            return new GameData();
        }
    }

    // Initializes save file from resources
    public static void initializeSaveFile() {
        File saveFile = new File(SAVE_FILE_PATH);
        if (!saveFile.exists()) {
            ensureSaveDirectoryExists();
            try (InputStream inputStream = SaveManager.class.getResourceAsStream(DEFAULT_RESOURCE_PATH)) {
                if (inputStream != null) {
                    // Copies default file to save directory
                    Files.copy(inputStream, Paths.get(SAVE_FILE_PATH));
                } else {
                    // Creates empty GameData file
                    saveGame(new GameData());
                }
            } catch (IOException e) {
                System.err.println("Error initializing save file: " + e.getMessage());
            }
        }
    }
}