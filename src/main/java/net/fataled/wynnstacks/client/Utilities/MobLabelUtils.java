package net.fataled.wynnstacks.client.Utilities;

import net.fataled.wynnstacks.client.HudConfig.HudConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.DisplayEntity.TextDisplayEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MobLabelUtils {
    private static final Logger LOGGER = LogManager.getLogger("MobLabelUtils");

    private static final double LABEL_RADIUS_XZ = 8.0;
    private static final double LABEL_RADIUS_Y = 30.0;

    private static final double MAX_HORIZONTAL_DISTANCE_SQ = 1.5 * 1.5;
    private static final double MIN_VERTICAL_OFFSET = 0.0;

    // Keep all lower-case; we lower candidate strings once.
    public static final List<String> PRIORITY_LABELS = List.of(
            "mummyboard","virus","accipientis","matrojan","titanium","death metal","mechorrupter","robob","cybel","legendary","yahya",
            "grootslang","orphion","colossus","anomaly","parasite",
            "argaddon","witch","guardian","chained","alkevö","death","strato","qira","aledar","tasim","psychomancer",
            "combat"
    );

    private static final Set<String> IGNORE_LABELS = Set.of(
            "weapon","shop","armoring","identifier","'s","merchant","market","bank","wood","tailoring",
            "npc","text display","armor stand","item display","skeleton","dernic","dps","loot","totem","damage","arrow","wolf",
            "slime","arming","lvl","cooking","rock","blacksmith","emerald","experience orb","bug","lv.","wybel"
    );

    // Code points for stat symbols; keep as boxed ints unless you want to pull in fastutil IntSets.
    private static final Set<Integer> STAT_SYMBOLS = Set.of(
            0x271C, // ✜
            0x2248, // ≈
            0x2699, // ⚙
            0x2620, // ☠
            0xE03A, // Tricks
            0xE03F, // Drained
            0xE03D, // Enkindled
            0xE03C, // Confusion
            0xE043, // Contamination
            0x2694  // ⚔
    );

    // Precompiled patterns (avoid recompiling every call)
    private static final Pattern COLOR_CODES = Pattern.compile("§[0-9a-fk-or]");
    private static final Pattern LINE_SPLIT = Pattern.compile("\\R");
    private static final Pattern SHORT_NEG_NUM = Pattern.compile("^-\\d+(\\s*[\\p{So}\\p{Punct}]*)?$");
    private static final Pattern SHORT_POS_NUM = Pattern.compile("^\\+\\d+(\\s*[\\p{So}\\p{Punct}]*)?$"); // FIXED: escaped '+'
    private static final Pattern SECONDS_TAIL = Pattern.compile("\\b\\d+\\s*s\\b");

    private static final IgnPattern IGN_PATTERN = new IgnPattern(); // your existing impl

    /* =========================
       Public API
       ========================= */

    public static List<String> getStatLines(Entity mob) {
        final Vec3d mobPos = mob.getPos();
        final Box box = mob.getBoundingBox().expand(LABEL_RADIUS_XZ, LABEL_RADIUS_Y, LABEL_RADIUS_XZ);

        // Collect candidates once
        List<TextDisplayEntity> labels = mob.getWorld().getEntitiesByClass(
                TextDisplayEntity.class, box,
                td -> {
                    Text t = td.getText();
                    if (t == null) return false;
                    String s = t.getString();
                    if (s == null) return false;
                    s = s.trim();
                    return !s.isEmpty() && !isProbablyDamageLineFast(s);
                }
        );

        if (labels.isEmpty()) return List.of();

        // Closest label (kept in case you want it for debugging/heuristics)
        TextDisplayEntity closest = null;
        double bestDistSq = Double.MAX_VALUE;
        for (TextDisplayEntity td : labels) {
            double dsq = td.squaredDistanceTo(mobPos);
            if (dsq < bestDistSq) { bestDistSq = dsq; closest = td; }
        }
        if (closest == null) return List.of();

        // Build text lines aligned above mob
        LinkedHashSet<String> lines = new LinkedHashSet<>(); // dedupe, preserve order
        for (TextDisplayEntity td : labels) {
            if (!isAboveMob(mobPos, td.getPos())) continue;

            String raw = safeString(td.getText());
            if (raw.isEmpty()) continue;

            // split and normalize each physical line
            String[] parts = LINE_SPLIT.split(raw);
            for (String part : parts) {
                String stripped = stripColors(part);
                String cleaned = removeUnrenderableChars(stripped, /*allowStatSymbols*/ true).trim();
                if (!cleaned.isEmpty() && !isProbablyDamageLineFast(cleaned)) {
                    lines.add(cleaned);
                }
            }
        }
        if (lines.isEmpty()) return List.of();

        // Config-enabled symbols once
        final Set<Integer> enabled = enabledSymbols();
        final Set<Integer> allSyms = STAT_SYMBOLS;

        // Filter/stat-chunk stripping in one pass
        ArrayList<String> out = new ArrayList<>(lines.size());
        for (String line : lines) {
            String pruned = removeDisabledStatChunks(line, enabled, allSyms);
            if (pruned.isEmpty()) continue;

            // keep only lines that still have at least one stat symbol + a digit
            boolean hasSym = containsAnyCodepoint(pruned, allSyms);
            boolean hasDigit = containsDigit(pruned);
            if (hasSym && hasDigit) out.add(pruned);
        }
        return out;
    }

    public static String getEntityLabelName(Entity mob) {
        final Box box = mob.getBoundingBox().expand(LABEL_RADIUS_XZ, LABEL_RADIUS_Y, LABEL_RADIUS_XZ);

        List<TextDisplayEntity> labels = mob.getWorld().getEntitiesByClass(
                TextDisplayEntity.class, box,
                td -> td.getText() != null && !safeString(td.getText()).isEmpty()
        );
        if (labels.isEmpty()) return "";

        LinkedHashSet<String> rawLines = new LinkedHashSet<>();
        for (TextDisplayEntity td : labels) {
            String raw = safeString(td.getText());
            if (raw.isEmpty()) continue;
            String[] parts = LINE_SPLIT.split(raw);
            for (String part : parts) {
                String cleaned = removeUnrenderableChars(stripColors(part), true).trim();
                if (!cleaned.isEmpty()) rawLines.add(cleaned);
            }
        }
        if (rawLines.isEmpty()) return "";

        // Filter for candidates
        ArrayList<String> candidates = new ArrayList<>(rawLines.size());
        for (String s : rawLines) {
            String t = s.trim();
            if (t.isEmpty()) continue;
            String lower = t.toLowerCase(Locale.ROOT);

            if (SECONDS_TAIL.matcher(lower).find()) continue;
            if (lower.startsWith("x2")) continue;
            if (containsAny(lower, IGNORE_LABELS)) continue;
            if (IGN_PATTERN.getPattern().matcher(t).find()) continue;
            if (isProbablyDamageLineFast(t)) continue;

            candidates.add(t);
        }

        // Priority match
        for (String c : candidates) {
            String lower = c.toLowerCase(Locale.ROOT);
            if (containsAny(lower, PRIORITY_LABELS)) return c;
        }

        if (!candidates.isEmpty()) return candidates.getFirst();

        // Fallback to mob display name (safe)
        Text disp = mob.getDisplayName();
        String fb = disp != null ? disp.getString() : "";
        fb = removeUnrenderableChars(stripColors(fb), false).trim().toLowerCase(Locale.ROOT);

        if (!fb.isEmpty() && !fb.startsWith("-") && !fb.startsWith("+") && !containsAny(fb, IGNORE_LABELS)) {
            return fb;
        }
        return "";
    }

    /* =========================
       Helpers
       ========================= */

    private static boolean isAboveMob(Vec3d mobPos, Vec3d labelPos) {
        double dx = labelPos.x - mobPos.x;
        double dz = labelPos.z - mobPos.z;
        double horizontalSq = dx * dx + dz * dz;
        double verticalOffset = labelPos.y - mobPos.y;
        return horizontalSq <= MAX_HORIZONTAL_DISTANCE_SQ && verticalOffset >= MIN_VERTICAL_OFFSET;
    }

    public static String stripColors(String input) {
        if (input == null || input.isEmpty()) return "";
        return COLOR_CODES.matcher(input).replaceAll("");
    }

    public static String removeUnrenderableChars(String input, boolean allowStatSymbols) {
        if (input == null || input.isEmpty()) return "";
        final Set<Integer> allowed = allowStatSymbols ? STAT_SYMBOLS : Collections.emptySet();

        StringBuilder sb = new StringBuilder(input.length());
        input.codePoints().forEach(cp -> {
            if ((cp >= 32 && cp <= 126) || Character.isWhitespace(cp) || allowed.contains(cp)) {
                sb.appendCodePoint(cp);
            }
        });
        return sb.toString();
    }

    // Faster than running multiple regexes—cheap short-circuits first
    public static boolean isProbablyDamageLine(String s) {
        return isProbablyDamageLineFast(s);
    }

    private static boolean isProbablyDamageLineFast(String s) {
        if (s == null) return false;
        String line = s.trim();
        if (line.isEmpty()) return false;

        // Strong early checks: short +/- numbers
        if (line.length() <= 8 && (SHORT_NEG_NUM.matcher(line).matches() || SHORT_POS_NUM.matcher(line).matches()))
            return true;

        String lower = line.toLowerCase(Locale.ROOT);
        if (lower.contains("lv.") || lower.contains("damage") || lower.contains("bleed") || lower.contains("burn"))
            return true;

        // Parentheses fix for operator precedence:
        // trigger if starts with +/-/[ AND has a stat symbol somewhere
        return (line.startsWith("+") || line.startsWith("[") || line.startsWith("-"))
                && containsAnyCodepoint(line, STAT_SYMBOLS);
    }

    private static String safeString(Text t) {
        String s = (t == null) ? "" : t.getString();
        return (s == null) ? "" : s;
    }

    private static boolean containsAny(String haystackLower, Collection<String> needlesLower) {
        for (String n : needlesLower) {
            if (haystackLower.contains(n)) return true;
        }
        return false;
    }

    private static boolean containsAnyCodepoint(String s, Set<Integer> cps) {
        return s.codePoints().anyMatch(cps::contains);
    }

    private static boolean containsDigit(String s) {
        return s.codePoints().anyMatch(Character::isDigit);
    }

    private static int parseHex(String key) {
        String hex = key.startsWith("0x") ? key.substring(2) : key;
        return Integer.parseInt(hex, 16);
    }

    private static Set<Integer> enabledSymbols() {
        // Compute once per call; if HudConfig changes rarely, consider caching with an epoch/version.
        return HudConfig.INSTANCE.chosenSymbols.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(e -> parseHex(e.getKey()))
                .collect(Collectors.toSet());
    }

    private static String removeDisabledStatChunks(String line, Set<Integer> enabled, Set<Integer> allSymbols) {
        if (line == null || line.isEmpty()) return "";
        String[] tokens = line.split("\\s+");
        StringBuilder out = new StringBuilder(line.length());
        boolean skipping = false;

        for (String tok : tokens) {
            if (tok.isEmpty()) continue;

            int firstCp = tok.codePointAt(0);
            boolean startsWithSymbol = allSymbols.contains(firstCp);

            if (startsWithSymbol) {
                skipping = !enabled.contains(firstCp);
            }
            if (!skipping) {
                if (!out.isEmpty()) out.append(' ');
                out.append(tok);
            }
        }
        return out.toString().trim();
    }
}
