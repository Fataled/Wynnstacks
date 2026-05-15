//TODO make sure to make a way to update / validate the config file

package net.fataled.wynnstacks.client.config;

import com.google.gson.annotations.SerializedName;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class HudConfig {
    public static final String DEBUFF = "debuff";
    public static final String SATSUJIN = "satsujin";
    public static final String MARKED = "marked";
    public static final String DISCOMBOBULATE = "discombobulate";
    public static final String POISON = "poison";
    public static final String TRICKS = "tricks";
    public static final String DRAINED = "drained";
    public static final String ENKINDLED = "enkindled";
    public static final String CONFUSION = "confusion";
    public static final String CONTAMINATION = "contamination";
    public static final String WEAKENED = "weakened";
    public static final String TWILIGHT = "twilight";
    public static final String VULNERABLE = "Vulnerable";

    private static final AtomicInteger SYMBOL_VERSION = new AtomicInteger(0);

    public static void bumpSymbolVersion() {
        SYMBOL_VERSION.incrementAndGet();
    }

    public static int symbolVersion() {
        return SYMBOL_VERSION.get();
    }

    public static final HudConfig INSTANCE = new HudConfig();

    @SerializedName("debugMode")
    public boolean debugMode = false;
    @SerializedName("x")
    public int x = 10;
    @SerializedName("y")
    public int y = 10;
    @SerializedName("Target Distance")
    public float maxTargetDistance = 72.0f; // This is used raycastutils
    @SerializedName("debug")
    public boolean debug = false;
    @SerializedName("range")
    public double range = 24.0; // This is used for audio
    @SerializedName("coneAngleDeg")
    public int coneAngleDeg = 30;
    @SerializedName("ignorePlayers")
    public boolean ignorePlayers = true;
    @SerializedName("showHud")
    public boolean showHud = true;

    @SerializedName("showSatsujinHud")
    public boolean showSatsujinHud = true;
    @SerializedName("AspectLvl2")
    public boolean aspectLvl2 = false;
    @SerializedName("Satx")
    public int satsujinX = 0;
    @SerializedName("Saty")
    public int satsujinY = 40;
    @SerializedName("useEndSounds")
    public boolean useEndSounds = true;
    @SerializedName("Volume")
    public float volume = 10f;

    @SerializedName("Chosen Symbols")
    public Map<String, Boolean> chosenSymbols = defaultChosenSymbols();

    public static Map<String, Boolean> defaultChosenSymbols() {
        Map<String, Boolean> map = new LinkedHashMap<>();

        map.put("0x271C", true);
        map.put("0x2699", true);
        map.put("0x2620", true);
        map.put("0xE03A", true);
        map.put("0xE03F", true);
        map.put("0xE03D", true);
        map.put("0xE03C", true);
        map.put("0xE043", true);
        map.put("0x2694", true);
        map.put("0xE04B", true);
        map.put("0xE015", true);
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

        // Layout
        public String align = "LEFT"; // stored as string in JSON
        public float scale = 1.0f;

        // Normalize & sanitize (call after loading)
        public void normalize() {
            if (scale <= 0f)
                scale = 1.0f;
            align = align == null ? "LEFT" : align.toUpperCase(Locale.ROOT);
            solidRgb &= 0xFFFFFF;
            outlineRgb &= 0xFFFFFF;
            outlineThicknessPx = Math.max(0, outlineThicknessPx);
        }
    }

    private static Map<String, Profile> defaultProfiles() {
        Map<String, Profile> m = new LinkedHashMap<>();

        Profile debuff = new Profile();
        debuff.solidRgb = 0xFFFFFF;
        debuff.outlineThicknessPx = 0f;
        debuff.outlineRgb = 0x0000F;
        m.put(DEBUFF, debuff);

        Profile satsu = new Profile();
        satsu.solidRgb = 0xFFFFFF;
        satsu.outlineThicknessPx = 0f;
        satsu.outlineRgb = 0x0000F;
        m.put(SATSUJIN, satsu);

        Profile marked = new Profile();
        marked.solidRgb = 0xFF1F1F;
        marked.outlineThicknessPx = 0f;
        marked.outlineRgb = 0x0000F;
        m.put(MARKED, marked);

        Profile discombobulate = new Profile();
        discombobulate.solidRgb = 0xFFFFFF;
        discombobulate.outlineThicknessPx = 0f;
        discombobulate.outlineRgb = 0x0000F;
        m.put(DISCOMBOBULATE, discombobulate);

        Profile poison = new Profile();
        poison.solidRgb = 0xFFFFFF;
        poison.outlineThicknessPx = 0f;
        poison.outlineRgb = 0x0000F;
        m.put(POISON, poison);

        Profile tricks = new Profile();
        tricks.solidRgb = 0xFFFFFF;
        tricks.outlineThicknessPx = 0f;
        tricks.outlineRgb = 0x0000F;
        m.put(TRICKS, tricks);

        Profile drained = new Profile();
        drained.solidRgb = 0xFFFFFF;
        drained.outlineThicknessPx = 0f;
        drained.outlineRgb = 0x0000F;
        m.put(DRAINED, drained);

        Profile enkindled = new Profile();
        enkindled.solidRgb = 0xFFFFFF;
        enkindled.outlineThicknessPx = 0f;
        enkindled.outlineRgb = 0x0000F;
        m.put(ENKINDLED, enkindled);

        Profile confusion = new Profile();
        confusion.solidRgb = 0xFFFFFF;
        confusion.outlineThicknessPx = 0f;
        confusion.outlineRgb = 0x0000F;
        m.put(CONFUSION, confusion);

        Profile contamination = new Profile();
        contamination.solidRgb = 0xFFFFFF;
        contamination.outlineThicknessPx = 0f;
        contamination.outlineRgb = 0x0000F;
        m.put(CONTAMINATION, contamination);

        Profile weakened = new Profile();
        weakened.solidRgb = 0xFFFFFF;
        weakened.outlineThicknessPx = 0f;
        weakened.outlineRgb = 0x0000F;
        m.put(WEAKENED, weakened);

        Profile twilight = new Profile();
        twilight.solidRgb = 0xFFFFFF;
        twilight.outlineThicknessPx = 0f;
        twilight.outlineRgb = 0x0000F;
        m.put(TWILIGHT, twilight);

        Profile vulnerable = new Profile();
        vulnerable.solidRgb = 0xFFFFFF;
        vulnerable.outlineThicknessPx = 0f;
        vulnerable.outlineRgb = 0x0000F;
        m.put(VULNERABLE, vulnerable);

        return m;
    }

    public void ensureDefaults() {
        Map<String, Profile> defs = defaultProfiles();
        // add missing profiles
        for (var e : defs.entrySet())
            profiles.putIfAbsent(e.getKey(), e.getValue());
        // normalize all
        for (var p : profiles.values())
            p.normalize();
    }

    /**
     * Copies state from a deserialized snapshot into this singleton instance,
     * so callers don't need to reassign the INSTANCE reference.
     */
    public void copyFrom(HudConfig src) {
        if (src == null)
            return;
        this.x = src.x;
        this.y = src.y;
        this.maxTargetDistance = src.maxTargetDistance;
        this.debug = src.debug;
        this.range = src.range;
        this.coneAngleDeg = src.coneAngleDeg;
        this.ignorePlayers = src.ignorePlayers;
        this.showHud = src.showHud;
        this.showSatsujinHud = src.showSatsujinHud;
        this.aspectLvl2 = src.aspectLvl2;
        this.satsujinX = src.satsujinX;
        this.satsujinY = src.satsujinY;
        this.useEndSounds = src.useEndSounds;
        this.volume = src.volume;
        if (src.chosenSymbols != null)
            this.chosenSymbols = new LinkedHashMap<>(src.chosenSymbols);
        if (src.profiles != null)
            this.profiles = new LinkedHashMap<>(src.profiles);
        for (var p : src.profiles.values())
            p.normalize();
        ;
    }

    public Profile obtain(String key) {
        Profile p = profiles.get(key);
        if (p == null)
            throw new IllegalStateException("missing profile: " + key);
        return p;
    }

    public static final String CONFIG_FILE = "wynnstacks-config.json";
}
