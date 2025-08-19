package net.fataled.wynnstacks.client.HudConfig;

import com.google.gson.annotations.SerializedName;

import java.util.LinkedHashMap;
import java.util.Map;


public class HudConfig {
    public static HudConfig INSTANCE = new HudConfig();
    @SerializedName("x") public int x = 10;
    @SerializedName("y") public int y = 10;
    @SerializedName("color") public int color = 0xFFFFFF;
    @SerializedName("scale") public float scale = 1.0f;
    @SerializedName("Target Distance") public float maxTargetDistance = 72.0f;
    @SerializedName("debug") public boolean debug = false;
    @SerializedName("range") public double range = 24.0;   // blocks
    @SerializedName("coneAngleDeg") public int coneAngleDeg = 30;
    @SerializedName("ignorePlayers") public boolean ignorePlayers = true;
    @SerializedName("showHud") public boolean showHud = true;
    @SerializedName("showSatsujinHud") public boolean showSatsujinHud = true;
    @SerializedName("AspectLvl2") public boolean AspectLvl2 = false;
    @SerializedName("Satx") public int Satx = 0;
    @SerializedName("Saty") public int Saty = 40;
    @SerializedName("useEndSounds") public boolean useEndSounds = true;
    @SerializedName("Volume") public float Volume = 10f;
    @SerializedName("Satsujin Scale")public float sScale = 1.0f;
    @SerializedName("rcX") public int rcX = 0;
    @SerializedName("rcY") public int rcY = 0;
    @SerializedName("rcS") public float rcS = 1.0f;
    @SerializedName("lineGap") public int lineGap = 10;
    @SerializedName("showRaidCounter") public boolean showRaidCounter = true;
    @SerializedName("Chosen Symbols") public Map<String, Boolean> chosenSymbols = defaultChosenSymbols();
    private static Map<String, Boolean> defaultChosenSymbols() {
        Map<String, Boolean> map = new LinkedHashMap<>();
        map.put("0x271C", true);
        map.put("0x2248", true);
        map.put("0x2699", true);
        map.put("0x2620", true);
        map.put("0xE03A" , true);
        map.put("0xE03F" , true);
        map.put("0xE03D" , true);
        map.put("0xE03C" , true);
        map.put("0xE043" , true);
        map.put("0x2694" , true);
        return map;
    }

  public static final String CONFIG_FILE = "wynnstacks-config.json";
}
