package net.fataled.wynnstacks.client;

import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.DisplayEntity.TextDisplayEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.Arrays;

public class MobLabelUtils {
    private static final Logger LOGGER = LogManager.getLogger("ShadestepperHUD");
    private static final double LABEL_RADIUS_XZ = 8.0;
    private static final double LABEL_RADIUS_Y = 30.0;

    public static final List<String> PRIORITY_LABELS = List.of(
            // Legendary Island / Altar bosses (end-game)
            "mummyboard",
            "virus",
            "accipientis",
            "matrojan",
            "titanium",
            "death metal",
            "mechorrupter",
            "robob",
            "cybel",
            "legendary",
            "yahya",
            // Raid bosses
            "grootslang",
            "orphion",
            "colossus",
            "anomaly",
            "parasite",
            // Tower of Ascension / Other boss altars
            "argaddon",
            "witch",
            "guardian",
            "chained",
            "alkevö",
            "death",
            "strato",
            "qira",
            "aledar",
            "tasim",
            "psychomancer",
            // Test Dummy
            "combat"
    );

    private static final Set<Integer> STAT_SYMBOLS = Set.of(
            0x271C, 0x2248, 0x2699, 0x2620,
            0xE03A, 0xE03F, 0xE03D, 0xE03C, 0xE043, 0x2694
    );

    private static final Set<String> IGNORE_LABELS = Set.of(
            "weapon", "shop", "armoring", "identifier", "'s",
            "merchant", "market", "bank", "wood", "tailoring",
            "npc", "text display", "armor stand", "item display",
            "skeleton", "dernic", "dps", "loot", "totem", "damage", "arrow", "wolf",
            "slime", "arming", "lvl", "cooking", "rock", "blacksmith", "emerald", "experience orb", "bug", "Lv.","wybel"
    );

    private static final double MAX_HORIZONTAL_DISTANCE_SQ = 1.5 * 1.5;
    private static final double MIN_VERTICAL_OFFSET = 0.0;

    public static List<String> getStatLines(Entity mob) {
        Box box = mob.getBoundingBox().expand(LABEL_RADIUS_XZ, LABEL_RADIUS_Y, LABEL_RADIUS_XZ);
        Vec3d mobPos = mob.getPos();

        Optional<TextDisplayEntity> closestLabel = mob.getWorld()
                .getEntitiesByClass(TextDisplayEntity.class, box,
                        td -> {
                            String text = td.getText() != null ? td.getText().getString().trim() : "";
                            return !text.isBlank() && !isProbablyDamageLine(text);
                        })
                .stream()
                .min(Comparator.comparingDouble(td -> td.squaredDistanceTo(mobPos)));

        if (closestLabel.isEmpty()) {
            return List.of();
        }

        TextDisplayEntity label = closestLabel.get();
        Vec3d labelPos = label.getPos();
        //LOGGER.info("[HUD Debug] Using label '{}' at {}", label.getText().getString(), labelPos);

        if (isProbablyDamageLine(label.getText().getString())) {
            //LOGGER.info("[HUD Debug] Rejected full-line label as damage: '{}'", label.getText().getString());
            return List.of();
        }

        List<String> textLines = mob.getWorld()
                .getEntitiesByClass(TextDisplayEntity.class, box, td -> {
                    String text = td.getText() != null ? td.getText().getString().trim() : "";
                    boolean aligned = isAboveMob(mob.getPos(), td.getPos());
                    return !text.isBlank() && aligned && !isProbablyDamageLine(text);
                })
                .stream()
                .flatMap(td -> Arrays.stream(td.getText().getString().split("\\R")))
                .map(MobLabelUtils::stripColors)
                .map(MobLabelUtils::removeUnrenderableChars)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();

        List<String> statLines = textLines.stream()
                .filter(line -> line.codePoints().anyMatch(STAT_SYMBOLS::contains))
                .filter(line -> line.codePoints().anyMatch(Character::isDigit))
                .toList();

        return statLines;
    }

    private static boolean isAboveMob(Vec3d mobPos, Vec3d labelPos) {
        double dx = labelPos.x - mobPos.x;
        double dz = labelPos.z - mobPos.z;
        double horizontalSq = dx * dx + dz * dz;
        double verticalOffset = labelPos.y - mobPos.y;

        return horizontalSq <= MAX_HORIZONTAL_DISTANCE_SQ && verticalOffset >= MIN_VERTICAL_OFFSET;
    }

    public static String getEntityLabelName(Entity mob) {
        Box box = mob.getBoundingBox().expand(LABEL_RADIUS_XZ, LABEL_RADIUS_Y, LABEL_RADIUS_XZ);

        List<String> rawCandidates = mob.getWorld()
                .getEntitiesByClass(TextDisplayEntity.class, box,
                        td -> td.getText() != null && !td.getText().getString().isBlank())
                .stream()
                .flatMap(td -> Arrays.stream(td.getText().getString().split("\\R")))
                .map(MobLabelUtils::stripColors)
                .map(MobLabelUtils::removeUnrenderableChars)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();

        List<String> labelCandidates = rawCandidates.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .filter(s -> IGNORE_LABELS.stream().noneMatch(ign -> s.toLowerCase().contains(ign)))
                .filter(s -> !isProbablyDamageLine(s))
                //.filter(s -> s.startsWith("["))
                .toList();

        Optional<String> priorityMatch = getPriorityNameFromLines(labelCandidates);
        if (priorityMatch.isPresent()) {
            return priorityMatch.get();
        }

        if (!labelCandidates.isEmpty()) {
            return labelCandidates.getFirst();
        }

        String fallback = removeUnrenderableChars(stripColors(mob.getDisplayName().getString())).trim();
        if (!fallback.isBlank()
                && !fallback.startsWith("-")
                && !fallback.startsWith("+")
                && IGNORE_LABELS.stream().noneMatch(fallback.toLowerCase()::contains)) {
            return fallback;
        }

        return "";
    }

    private static Optional<String> getPriorityNameFromLines(List<String> lines) {
        return lines.stream()
                .map(String::toLowerCase)
                .filter(line -> PRIORITY_LABELS.stream().anyMatch(line::contains))
                .findFirst();
    }

    public static Optional<Entity> getHighestPriorityEntity(List<Entity> entities) {
        for (Entity entity : entities) {
            String label = getEntityLabelName(entity).toLowerCase();
            for (String keyword : PRIORITY_LABELS) {
                if (label.contains(keyword)) {
                    return Optional.of(entity);
                }
            }
        }

        for (Entity entity : entities) {
            String label = getEntityLabelName(entity);
            if (!label.isBlank()) {
                return Optional.of(entity);
            }
        }

        return Optional.empty();
    }

    public static String stripColors(String input) {
        return input.replaceAll("§[0-9a-fk-or]", "");
    }

    public static String removeUnrenderableChars(String input) {
        return input.codePoints()
                .filter(cp -> (cp >= 32 && cp <= 126)
                        || STAT_SYMBOLS.contains(cp)
                        || Character.isWhitespace(cp))
                .collect(StringBuilder::new,
                        StringBuilder::appendCodePoint,
                        StringBuilder::append)
                .toString();
    }

    public static boolean isProbablyDamageLine(String s) {
        if (s == null) return false;
        String line = s.trim();

        // Strong early check: short negative numbers with or without symbols
        if (line.matches("^-[\\d]+(\\s*[\\p{So}\\p{Punct}]*)?$") && line.length() <= 8) return true;

        if (line.matches("^+[\\d]+(\\s*[\\p{So}\\p{Punct}]*)?$") && line.length() <= 8) return true;

        // Check for damage-related keywords
        String lower = line.toLowerCase();
        if (lower.contains("lv.") || lower.contains("damage") || lower.contains("bleed") || lower.contains("burn")) {
            return true;
        }

        // Check for leading dash + stat symbol like ☠ or ✜
        if (line.startsWith("-") && line.codePoints().anyMatch(STAT_SYMBOLS::contains)) {
            return true;
        }
        if (line.startsWith("+") && line.codePoints().anyMatch(STAT_SYMBOLS::contains)) {
            return true;
        }
        if (line.startsWith("[") && line.codePoints().anyMatch(STAT_SYMBOLS::contains)) {
            return true;
        }


        return false;
    }
}