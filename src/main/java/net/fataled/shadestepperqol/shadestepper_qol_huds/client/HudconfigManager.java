package net.fataled.shadestepperqol.shadestepper_qol_huds.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.lang.reflect.Field;

public class HudconfigManager {

    private static final Logger LOGGER = LogManager.getLogger("ShadestepperHUD");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_PATH = new File(
            FabricLoader.getInstance().getConfigDir().toFile(),
            HudConfig.CONFIG_FILE
    );

    public static void load() {
        if (CONFIG_PATH.exists()) {
            try (FileReader reader = new FileReader(CONFIG_PATH)) {
                HudConfig.INSTANCE = GSON.fromJson(reader, HudConfig.class);
                LOGGER.info("HUD config loaded successfully.");
            } catch (IOException e) {
                LOGGER.error("Failed to load HUD config: {}", e.getMessage());
            }
        } else {
            LOGGER.warn("HUD config file not found. Creating default config.");
            reset();
            save();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_PATH)) {
            GSON.toJson(HudConfig.INSTANCE, writer);
            LOGGER.info("HUD config saved to file.");
        } catch (IOException e) {
            LOGGER.error("Failed to save HUD config: {}", e.getMessage());
        }
    }

    public static void reset() {
        HudConfig.INSTANCE.x = 10;
        HudConfig.INSTANCE.y = 10;
        HudConfig.INSTANCE.color = 0xFFFFFF;
        HudConfig.INSTANCE.scale = 1.0f;
        HudConfig.INSTANCE.maxTargetDistance = 72.0f;
        HudConfig.INSTANCE.range = 72.0f;
        HudConfig.INSTANCE.coneAngleDeg = 6;
        HudConfig.INSTANCE.ignorePlayers = true;
        HudConfig.INSTANCE.debug = false;
        HudConfig.INSTANCE.showHud = true;
        HudConfig.INSTANCE.AspectLvl2 = false;
        HudConfig.INSTANCE.Volume = 10f;
        HudConfig.INSTANCE.sScale = 1f;
        HudConfig.INSTANCE.rcS = 1.0f;
        HudConfig.INSTANCE.rcX = 0;
        HudConfig.INSTANCE.rcY = 0;
        LOGGER.info("HUD config reset to default values.");
    }

    public static void resetAndSave() {
        reset();
        save();
    }

    public void saveSystem(){

        Gson gson = new Gson();
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("shadestepper_hud_config.json");
        try (Reader reader = Files.newBufferedReader(configPath)) {
            HudConfig fileConfig = gson.fromJson(reader, HudConfig.class);
            compareConfig(fileConfig, HudConfig.INSTANCE);
        } catch (IOException e) {
            LOGGER.warn("Failed to load config for comparison", e);
        }

    }

    private void compareConfig(HudConfig file, HudConfig Live){
        if (file == null || Live == null) return;

        Field[] fields = HudConfig.class.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object fileValue = field.get(file);
                Object liveValue = field.get(Live);

                if ((fileValue != null && !fileValue.equals(liveValue)) ||
                        (fileValue == null && liveValue != null)) {
                    save();
                }
            } catch (IllegalAccessException e) {
                LOGGER.warn("Unable to compare field: {}", field.getName(), e);
            }
        }

    }



}
