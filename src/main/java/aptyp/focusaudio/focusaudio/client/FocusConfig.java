package aptyp.focusaudio.focusaudio.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FocusConfig {
    private static final File FILE = FabricLoader.getInstance().getConfigDir().resolve("focusaudio.json").toFile();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static FocusConfig instance = new FocusConfig();

    public float focusMultiplier = 2.0f;
    public float backgroundVolume = 0.1f;
    public int focusAngle = 15;

    public static void save() {
        try (FileWriter writer = new FileWriter(FILE)) {
            GSON.toJson(instance, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void load() {
        if (FILE.exists()) {
            try (FileReader reader = new FileReader(FILE)) {
                instance = GSON.fromJson(reader, FocusConfig.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
