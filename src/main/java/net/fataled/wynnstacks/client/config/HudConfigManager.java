package net.fataled.wynnstacks.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.fataled.wynnstacks.client.util.LoggerUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;

public class HudConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_PATH = new File(
            FabricLoader.getInstance().getConfigDir().toFile(),
            HudConfig.CONFIG_FILE);

    public static void load() {
        if (CONFIG_PATH.exists()) {
            try (var reader = Files.newBufferedReader(CONFIG_PATH.toPath(), StandardCharsets.UTF_8)) {
                HudConfig parsed = GSON.fromJson(reader, HudConfig.class);
                HudConfig.INSTANCE.copyFrom(parsed);
                HudConfig.INSTANCE.ensureDefaults();
                if (HudConfig.INSTANCE.chosenSymbols == null) {
                    HudConfig.INSTANCE.chosenSymbols = new LinkedHashMap<>();
                }
                LoggerUtils.info("HUD config loaded successfully.");
            } catch (IOException e) {
                LoggerUtils.error("Failed to load HUD config", e);
            }
        } else {
            LoggerUtils.warn("HUD config file not found. Creating default config.");
            reset();
            save();
        }
    }

    public static void save() {
        Path real = CONFIG_PATH.toPath();
        Path temp = real.resolveSibling(real.getFileName() + ".tmp");
        try (var writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
            GSON.toJson(HudConfig.INSTANCE, writer);
            LoggerUtils.info("HUD config saved to {}", real.getFileName());
        } catch (IOException e) {
            LoggerUtils.error("Failed to write temp config", e);
            try {
                Files.deleteIfExists(temp);
            } catch (IOException ignored) {
            }
            return;
        }
        try {
            Files.move(temp, real, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            LoggerUtils.error("Failed to swap config into place", e);
        }
    }

    public static void reset() {

        HudConfig.INSTANCE.x = 0;
        HudConfig.INSTANCE.y = 10;
        HudConfig.INSTANCE.maxTargetDistance = 24.0f;
        HudConfig.INSTANCE.range = 24.0f;
        HudConfig.INSTANCE.coneAngleDeg = 60;
        HudConfig.INSTANCE.ignorePlayers = true;
        HudConfig.INSTANCE.debug = false;
        HudConfig.INSTANCE.showHud = true;

        HudConfig.INSTANCE.aspectLvl2 = false;
        HudConfig.INSTANCE.volume = 10f;
        HudConfig.INSTANCE.satsujinX = 0;
        HudConfig.INSTANCE.satsujinY = 50;
        HudConfig.INSTANCE.showSatsujinHud = true;

        // Ensure map exists and has keys before replaceAll
        if (HudConfig.INSTANCE.chosenSymbols == null) {
            HudConfig.INSTANCE.chosenSymbols = new LinkedHashMap<>();
        }
        // Seed your default keys (strings are safest in JSON)
        var defaultCodes = HudConfig.defaultChosenSymbols().keySet();
        for (String code : defaultCodes) {
            HudConfig.INSTANCE.chosenSymbols.putIfAbsent(code, true); // or false if you prefer
        }

        // Flip everything on (or off)
        HudConfig.INSTANCE.chosenSymbols.replaceAll((k, v) -> true);

        LoggerUtils.info("HUD config reset to default values.");
    }

    public static void resetAndSave() {
        reset();
        save();
    }
}
