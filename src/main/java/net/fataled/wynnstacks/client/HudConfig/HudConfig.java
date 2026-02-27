package net.fataled.wynnstacks.client.HudConfig;

import com.google.gson.annotations.SerializedName;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;


public class HudConfig {
    public static final String DEBUFF      = "debuff";
    public static final String SATSUJIN    = "satsujin";
    public static final String RAIDCOUNTER = "raidCounter";

    public static HudConfig INSTANCE = new HudConfig();
    @SerializedName("DebuffGradient") public boolean debuffGradient = false;
    @SerializedName("SatsujinGradient") public boolean satsujinGradient = false;

    @SerializedName("x") public int x = 10;
    @SerializedName("y") public int y = 10;
    @SerializedName("Target Distance") public float maxTargetDistance = 72.0f;
    @SerializedName("debug") public boolean debug = false;
    @SerializedName("range") public double range = 24.0;   // blocks
    @SerializedName("coneAngleDeg") public int coneAngleDeg = 30;
    @SerializedName("ignorePlayers") public boolean ignorePlayers = true;
    @SerializedName("showHud") public boolean showHud = true;

    @SerializedName("showSatsujinHud") public boolean showSatsujinHud = true;
    @SerializedName("AspectLvl2") public boolean AspectLvl2 = false;
    @SerializedName("Satx") public int SatsujinX = 0;
    @SerializedName("Saty") public int SatsujinY = 40;
    @SerializedName("useEndSounds") public boolean useEndSounds = true;
    @SerializedName("Volume") public float Volume = 10f;


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

    @SerializedName("Text Profiles")
    public Map<String, Profile> profiles = defaultProfiles();

    public static final class Profile {
        // Fill
        public int solidRgb = 0xFFFFFF;

        // Outline
        public float outlineThicknessPx = 0f;
        public int outlineRgb = 0x101018;
        public boolean shadowOnMain = false;

        // Layout
        public String align = "LEFT"; // stored as string in JSON
        public float scale = 1.0f;

        // Normalize & sanitize (call after loading)
        public void normalize() {
            if (scale <= 0f) scale = 1.0f;
            align = align == null ? "LEFT" : align.toUpperCase(Locale.ROOT);
            solidRgb &= 0xFFFFFF;
            outlineRgb &= 0xFFFFFF;
            outlineThicknessPx = Math.max(0, outlineThicknessPx);
        }
    }

    private static Map<String, Profile> defaultProfiles() {
        Map<String, Profile> m = new LinkedHashMap<>();

        Profile debuff = new Profile();
        debuff.solidRgb = 0x0000F;
        debuff.outlineThicknessPx = 0f;
        debuff.outlineRgb = 0x0000F;
        m.put(DEBUFF, debuff);

        Profile satsu = new Profile();
        satsu.solidRgb = 0x0000F;
        satsu.outlineThicknessPx = 0f;
        satsu.outlineRgb = 0x0000F;
        m.put(SATSUJIN, satsu);

        return m;
    }

    public void ensureDefaults() {
        Map<String, Profile> defs = defaultProfiles();
        // add missing profiles
        for (var e : defs.entrySet()) profiles.putIfAbsent(e.getKey(), e.getValue());
        // normalize all
        for (var p : profiles.values()) p.normalize();
    }

    public Profile obtain(String key) {
        Profile p = profiles.get(key);
        if (p == null) {
            p = defaultProfiles().get(DEBUFF);
        }
        p.normalize();
        return p;
    }

  public static final String CONFIG_FILE = "wynnstacks-config.json";
}
