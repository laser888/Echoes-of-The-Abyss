package Data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;

public class SaveManager {
    private static final String SAVE_FILE_PATH = "game_progress.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void saveGame(GameData data) {

        if (data == null) {
            System.err.println("Cannot save null GameData");
            return;
        }

        try (Writer writer = new FileWriter(SAVE_FILE_PATH)) {
            String jsonOutput = gson.toJson(data);
            //System.out.println("Saving GameData to JSON: " + jsonOutput);
            gson.toJson(data, writer);
            //System.out.println("Game data saved successfully to " + SAVE_FILE_PATH);
            //System.out.println("Save file location: " + new File(SAVE_FILE_PATH).getAbsolutePath());

        } catch (IOException e) {
            System.err.println("Error: Failed to save game data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static GameData loadGame() {
        try (Reader reader = new FileReader(SAVE_FILE_PATH)) {
            GameData loadedData = gson.fromJson(reader, GameData.class);
            //System.out.println("Loaded GameData: " + (loadedData != null ? gson.toJson(loadedData) : "null"));
            return loadedData != null ? loadedData : new GameData();

        } catch (IOException e) {
            System.out.println("No save file found or error loading: " + e.getMessage());
            return new GameData();
        }
    }
}