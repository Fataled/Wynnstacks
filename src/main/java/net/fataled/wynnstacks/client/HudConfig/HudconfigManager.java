package net.fataled.wynnstacks.client.HudConfig;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.LinkedHashMap;


public class HudconfigManager {

    private static final Logger LOGGER = LogManager.getLogger("HudconfigManager");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_PATH = new File(
            FabricLoader.getInstance().getConfigDir().toFile(),
            HudConfig.CONFIG_FILE
    );


    public static void load() {
        if (CONFIG_PATH.exists()) {
            try (FileReader reader = new FileReader(CONFIG_PATH)) {
                HudConfig.INSTANCE = GSON.fromJson(reader, HudConfig.class);
                if (HudConfig.INSTANCE.chosenSymbols == null) {
                    HudConfig.INSTANCE.chosenSymbols = new LinkedHashMap<>();
                }
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
        HudConfig.INSTANCE.debuffGradient = false;
        HudConfig.INSTANCE.satsujinGradient = false;
        HudConfig.INSTANCE.raidCounterGradient = false;

        HudConfig.INSTANCE.x = 0;
        HudConfig.INSTANCE.y = 10;
        HudConfig.INSTANCE.maxTargetDistance = 24.0f;
        HudConfig.INSTANCE.range = 24.0f;
        HudConfig.INSTANCE.coneAngleDeg = 60;
        HudConfig.INSTANCE.ignorePlayers = true;
        HudConfig.INSTANCE.debug = false;
        HudConfig.INSTANCE.showHud = true;

        HudConfig.INSTANCE.AspectLvl2 = false;
        HudConfig.INSTANCE.Volume = 10f;
        HudConfig.INSTANCE.SatsujinX = 0;
        HudConfig.INSTANCE.SatsujinY = 50;
        HudConfig.INSTANCE.showSatsujinHud = true;

        HudConfig.INSTANCE.RaidCounterX = 0;
        HudConfig.INSTANCE.RaidCounterY = 0;
        HudConfig.INSTANCE.showRaidCounter = true;

        // Ensure map exists and has keys before replaceAll
        if (HudConfig.INSTANCE.chosenSymbols == null) {
            HudConfig.INSTANCE.chosenSymbols = new LinkedHashMap<>();
        }
        // Seed your default keys (strings are safest in JSON)
        String[] defaultCodes = {"0x271C","0x2248","0x2699","0x2620","0xE03A","0xE03F","0xE03D","0xE03C","0x2694"};
        for (String code : defaultCodes) {
            HudConfig.INSTANCE.chosenSymbols.putIfAbsent(code, true); // or false if you prefer
        }

        // Flip everything on (or off)
        HudConfig.INSTANCE.chosenSymbols.replaceAll((k, v) -> true);

        LOGGER.info("HUD config reset to default values.");
    }

    public static void resetAndSave() {
        reset();
        save();
    }
}
