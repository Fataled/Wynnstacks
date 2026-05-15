package net.fataled.wynnstacks.client.label;

import net.fataled.wynnstacks.client.config.HudConfig;
import net.fataled.wynnstacks.client.util.LoggerUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.DisplayEntity.TextDisplayEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.*;
import java.util.regex.Pattern;

public class MobLabelUtils {

    private static final double LABEL_RADIUS_XZ = 8.0;
    private static final double LABEL_RADIUS_Y = 30.0;

    private static final double MAX_HORIZONTAL_DISTANCE_SQ = 1.5 * 1.5;
    private static final double MIN_VERTICAL_OFFSET = 0.0;

    // Keep all lower-case; we lower candidate strings once.
    public static final List<String> PRIORITY_LABELS = List.of(
            "mummyboard", "virus", "accipientis", "matrojan", "titanium", "death metal", "mechorrupter", "robob",
            "cybel", "legendary", "yahya",
            "grootslang", "orphion", "colossus", "anomaly", "parasite",
            "argaddon", "witch", "guardian", "chained", "alkevö", "death", "strato", "qira", "aledar", "tasim",
            "psychomancer",
            "dummy");

    private volatile static int cachedSymbolVersion = -1;
    private volatile static int[] cachedEnabledSorted = new int[0];

    // Code points for stat symbols; keep as boxed ints unless you want to pull in
    // fastutil IntSets.
    // Just remove Winded
    // New fruma debudd hypoxia doesn't work for now
    // Not new but add whipped for summoner
    // Bleeding for aco not needed to be refreshed to frequently
    private static final int[] STAT_SYMBOLS_SORTED = {
            0x2620, 0x2694, 0x2699, 0x271C, 0xE015,
            0xE03A, 0xE03C, 0xE03D, 0xE03F, 0xE043, 0xE04B
    };

    // Precompiled patterns (avoid recompiling every call)
    private static final Pattern COLOR_CODES = Pattern.compile("§[0-9a-fk-or]");
    private static final Pattern LINE_SPLIT = Pattern.compile("\\R");
    private static final Pattern SHORT_NEG_NUM = Pattern.compile("^-\\d+(\\s*[\\p{So}\\p{Punct}]*)?$");
    private static final Pattern SHORT_POS_NUM = Pattern.compile("^\\+\\d+(\\s*[\\p{So}\\p{Punct}]*)?$"); // FIXED:
                                                                                                          // escaped '+'
    private static final Pattern SECONDS_TAIL = Pattern.compile("\\b\\d+\\s*s\\b");
    private static final Pattern PRIORITY_REGEX = Pattern.compile(
            String.join("|", PRIORITY_LABELS),
            Pattern.CASE_INSENSITIVE);

    /*
     * =========================
     * Public API
     * =========================
     */

    public static boolean isPriority(String label) {
        return PRIORITY_REGEX.matcher(label).find();
    }

    public static List<String> getStatLines(Entity mob) {
        final Vec3d mobPos = mob.getEntityPos();
        final Box box = mob.getBoundingBox().expand(LABEL_RADIUS_XZ, LABEL_RADIUS_Y, LABEL_RADIUS_XZ);

        // Collect candidates once
        List<TextDisplayEntity> labels = mob.getEntityWorld().getEntitiesByClass(
                TextDisplayEntity.class, box,
                td -> {
                    Text t = td.getText();
                    if (t == null)
                        return false;
                    String s = t.getString();
                    if (s == null)
                        return false;
                    s = s.trim();
                    return !s.isEmpty() && !isProbablyDamageLineFast(s);
                });

        if (labels.isEmpty())
            return List.of();

        // Build text lines aligned above mob
        LinkedHashSet<String> lines = new LinkedHashSet<>(); // dedupe, preserve order
        for (TextDisplayEntity td : labels) {
            if (!isAboveMob(mobPos, td.getEntityPos()))
                continue;

            String raw = safeString(td.getText());
            // LoggerUtils.info("[WynnStacks] Raw: " + raw);
            if (raw.isEmpty())
                continue;

            // split and normalize each physical line
            String[] parts = LINE_SPLIT.split(raw);
            for (String part : parts) {
                String stripped = stripColors(part);
                // LoggerUtils.info("[Wynnstacks] stripped: " + stripped);
                // once done testing / comment it out
                if (HudConfig.INSTANCE.debugMode) {
                    LoggerUtils.info("Before removing unrenderable chars {}", stripped);
                }
                String cleaned = removeUnrenderableChars(stripped, true).trim();
                if (HudConfig.INSTANCE.debugMode) {
                    LoggerUtils.info("possible statline: {}", cleaned);
                }
                if (!cleaned.isEmpty() && !isProbablyDamageLineFast(cleaned)) {
                    lines.add(cleaned);
                }
            }
        }
        if (lines.isEmpty())
            return List.of();

        // Config-enabled symbols once
        final int[] enabled = enabledSymbols();

        // Filter/stat-chunk stripping in one pass
        ArrayList<String> out = new ArrayList<>(lines.size());
        for (String line : lines) {
            String pruned = removeDisabledStatChunks(line, enabled);
            if (pruned.isEmpty())
                continue;

            // keep only lines that still have at least one stat symbol + a digit
            boolean hasSym = containsAnyCodepoint(pruned);
            boolean hasDigit = containsDigit(pruned);
            if (HudConfig.INSTANCE.debugMode) {
                LoggerUtils.info("pruned: {}", pruned);
            }
            if (hasSym && hasDigit)
                out.add(pruned);
        }
        return out;
    }

    public static String getEntityLabelName(Entity mob) {
        final Box box = mob.getBoundingBox().expand(LABEL_RADIUS_XZ, LABEL_RADIUS_Y, LABEL_RADIUS_XZ);

        List<TextDisplayEntity> labels = mob.getEntityWorld().getEntitiesByClass(
                TextDisplayEntity.class, box,
                td -> td.getText() != null && !safeString(td.getText()).isEmpty());
        if (labels.isEmpty())
            return "";

        LinkedHashSet<String> rawLines = new LinkedHashSet<>();
        for (TextDisplayEntity td : labels) {
            String raw = safeString(td.getText());
            if (raw.isEmpty())
                continue;
            String[] parts = LINE_SPLIT.split(raw);
            for (String part : parts) {
                String cleaned = removeUnrenderableChars(stripColors(part), true).trim();
                if (!cleaned.isEmpty())
                    rawLines.add(cleaned);
            }
        }
        if (rawLines.isEmpty())
            return "";

        // Filter for candidates
        ArrayList<String> candidates = new ArrayList<>(rawLines.size());
        for (String s : rawLines) {
            String t = s.trim();
            if (t.isEmpty())
                continue;
            String lower = t.toLowerCase(Locale.ROOT);

            if (SECONDS_TAIL.matcher(lower).find())
                continue;
            if (lower.startsWith("x2"))
                continue;
            if (IgnPattern.INSTANCE.getPattern().matcher(t).find())
                continue;
            if (isProbablyDamageLineFast(t))
                continue;

            candidates.add(t);
        }

        // Priority match
        for (String c : candidates) {
            String lower = c.toLowerCase(Locale.ROOT);
            if (isPriority(lower))
                return c;
        }

        if (!candidates.isEmpty())
            return candidates.getFirst();

        // Fallback to mob display name (safe)
        Text disp = mob.getDisplayName();
        String fb = disp != null ? disp.getString() : "";
        fb = removeUnrenderableChars(stripColors(fb), false).trim().toLowerCase(Locale.ROOT);

        if (!fb.isEmpty() && !fb.startsWith("-") && !fb.startsWith("+")) {
            return fb;
        }
        return "";
    }

    /*
     * =========================
     * Helpers
     * =========================
     */

    private static boolean isAboveMob(Vec3d mobPos, Vec3d labelPos) {
        double dx = labelPos.x - mobPos.x;
        double dz = labelPos.z - mobPos.z;
        double horizontalSq = dx * dx + dz * dz;
        double verticalOffset = labelPos.y - mobPos.y;
        return horizontalSq <= MAX_HORIZONTAL_DISTANCE_SQ && verticalOffset >= MIN_VERTICAL_OFFSET;
    }

    public static String stripColors(String input) {
        if (input == null || input.isEmpty())
            return "";
        return COLOR_CODES.matcher(input).replaceAll("");
    }

    public static String removeUnrenderableChars(String input, boolean allowStatSymbols) {
        if (input == null || input.isEmpty())
            return "";

        int len = input.length();

        int debugPoint = 0;
        int firstBad = -1;
        int i = 0;
        while (i < len) {
            int codePoint = input.codePointAt(i);
            debugPoint = codePoint;
            if (!isKeepable(codePoint, allowStatSymbols)) {
                firstBad = i;
                break;
            }
            i += Character.charCount(codePoint);
        }
        if (firstBad < 0) {
            LoggerUtils.info("Removed: {} {} ", input, debugPoint);
            return input;
        }

        StringBuilder sb = new StringBuilder(len);

        sb.append(input, 0, firstBad);

        i = firstBad;
        while (i < len) {
            int codePoint = input.codePointAt(i);
            int width = Character.charCount(codePoint);

            boolean keep = isKeepable(codePoint, allowStatSymbols);

            if (keep)
                sb.appendCodePoint(codePoint);

            i += width;
        }
        return sb.toString();
    }

    private static boolean isKeepable(int codePoint, boolean allowStatSymbols) {
        return (codePoint >= 32 && codePoint <= 126)
                || Character.isWhitespace(codePoint) || (allowStatSymbols && isStatSymbol(codePoint));
    }

    private static boolean isStatSymbol(int codePoint) {
        return Arrays.binarySearch(STAT_SYMBOLS_SORTED, codePoint) >= 0;
    }

    // Faster than running multiple regexes—cheap short-circuits first
    public static boolean isProbablyDamageLine(String s) {
        return isProbablyDamageLineFast(s);
    }

    private static boolean isProbablyDamageLineFast(String s) {
        if (s == null)
            return false;
        String line = s.trim();
        if (line.isEmpty())
            return false;

        // Strong early checks: short +/- numbers
        if (line.length() <= 8 && (SHORT_NEG_NUM.matcher(line).matches() || SHORT_POS_NUM.matcher(line).matches()))
            return true;

        String lower = line.toLowerCase(Locale.ROOT);
        if (lower.contains("lv.") || lower.contains("damage") || lower.contains("bleed") || lower.contains("burn"))
            return true;

        // Parentheses fix for operator precedence:
        // trigger if starts with +/-/[ AND has a stat symbol somewhere
        return (line.startsWith("+") || line.startsWith("[") || line.startsWith("-"))
                && containsAnyCodepoint(line);
    }

    private static String safeString(Text t) {
        String s = (t == null) ? "" : t.getString();
        return (s == null) ? "" : s;
    }

    private static boolean containsAnyCodepoint(String s) {
        return s.codePoints().anyMatch(MobLabelUtils::isStatSymbol);
    }

    private static boolean containsDigit(String s) {
        return s.codePoints().anyMatch(Character::isDigit);
    }

    private static int parseHex(String key) {
        String hex = key.startsWith("0x") ? key.substring(2) : key;
        return Integer.parseInt(hex, 16);
    }

    private static int[] enabledSymbols() {
        int v = HudConfig.symbolVersion();
        if (v != cachedSymbolVersion) {
            cachedEnabledSorted = HudConfig.INSTANCE.chosenSymbols.entrySet().stream()
                    .filter(Map.Entry::getValue)
                    .mapToInt(e -> parseHex(e.getKey()))
                    .sorted()
                    .toArray();
            cachedSymbolVersion = v;
        }
        return cachedEnabledSorted;
    }

    private static String removeDisabledStatChunks(String line, int[] enabled) {
        if (line == null || line.isEmpty())
            return "";
        int len = line.length();
        StringBuilder out = new StringBuilder(len);
        boolean skipping = false;
        boolean firstOut = true;
        int i = 0;

        while (i < len) {
            while (i < len && Character.isWhitespace(line.charAt(i)))
                i++;
            if (i >= len)
                break;

            int tokenStart = i;
            int firstCodePoint = line.codePointAt(i);

            while (i < len && !Character.isWhitespace(line.charAt(i)))
                i++;

            boolean startsWithSymbol = Arrays.binarySearch(STAT_SYMBOLS_SORTED, firstCodePoint) >= 0;
            if (startsWithSymbol) {
                skipping = Arrays.binarySearch(enabled, firstCodePoint) < 0;
            }
            if (!skipping) {
                if (!firstOut)
                    out.append(' ');
                out.append(line, tokenStart, i);
                firstOut = false;
            }
        }

        return out.toString();
    }
}
