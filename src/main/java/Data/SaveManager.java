package Data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;

public class SaveManager {
    private static final String SAVE_FILE_PATH = System.getProperty("user.home") + File.separator + "game_progress.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void saveGame(GameData data) {
        if (data == null) {
            System.err.println("Cannot save null GameData");
            return;
        }

        try (Writer writer = new FileWriter(SAVE_FILE_PATH)) {
            gson.toJson(data, writer);
            System.out.println("Game data saved to " + SAVE_FILE_PATH);
        } catch (IOException e) {
            System.err.println("Error saving game data: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public static GameData loadGame() {
        try (InputStream is = SaveManager.class.getClassLoader().getResourceAsStream("game_progress.json")) {
            if (is == null) {
                System.out.println("Save file not found in resources.");
                return new GameData();
            }

            InputStreamReader reader = new InputStreamReader(is);
            return gson.fromJson(reader, GameData.class);

        } catch (IOException e) {
            System.out.println("Error loading save file: " + e.getMessage());
            return new GameData();
        }
    }

}