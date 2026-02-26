package net.fataled.wynnstacks.Deprecated.raidRelated;

import net.fataled.wynnstacks.Deprecated.interfaces.RaidKind;
import net.fataled.wynnstacks.Deprecated.raidRelated.raids.NoL;
import net.fataled.wynnstacks.Deprecated.raidRelated.raids.NoTG;
import net.fataled.wynnstacks.Deprecated.raidRelated.raids.TCC;
import net.fataled.wynnstacks.Deprecated.raidRelated.raids.TNA;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;


public class raidRegistry {
    private static final Logger LOGGER = LogManager.getLogger("raidRegistry");
    public static final List<RaidKind> RAIDS = List.of(
            new NoL(),
            new NoTG(),
            new TCC(),
            new TNA()
    );

    public static Optional<RaidKind> fromRawTitle(String rawTitle){
        return RAIDS.stream()
                .peek(r -> LOGGER.info("[Raid Debug] Comparing '{}' vs '{}'",
                        normalizeTitle(r.getEntryTitleRaw()), normalizeTitle(rawTitle)))
                .filter(r -> normalizeTitle(r.getEntryTitleRaw()).equals(normalizeTitle(rawTitle)))
                .findFirst();
    }
    private static String normalizeTitle(String input) {
        return input
                .replace("’", "'")       // Convert curly apostrophe to straight
                .replaceAll("\\p{C}", "") // Remove invisible control characters
                .trim();
    }

}

