package net.fataled.shadestepperqol.shadestepper_qol_huds.client;

import net.fataled.shadestepperqol.shadestepper_qol_huds.event.TitleHandler;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class RaidModel {

    private static final Logger LOGGER = LogManager.getLogger("ShadestepperHUD");

    private static RaidModel instance;  // <-- static reference for global access
    private RaidKind currentRaid;

    public RaidModel() {
        TitleHandler.register(this::onTitleSet);
        instance = this;
    }

    private void onTitleSet(String rawTitle) {

        String cleaned = Text.literal(rawTitle).getString();
        String Final = stripColors(cleaned);

    List<String> raidTitles = List.of("The Nameless Anomaly", "Orphion's Nexus of Light", "The Canyon Colossus", "Nest of The Grootslangs", "Raid Completed!", "Raid Failed");

    boolean isRaidTitle = raidTitles.stream().anyMatch(Final::contains);

    if(!isRaidTitle) return;

    raidRegistry.fromRawTitle(Final).ifPresentOrElse(
            raid -> {
                this.currentRaid = raid;
                RaidCounter.setActiveRaid(raid.getEntryTitleRaw());
                setLastTitle(Final);
                LOGGER.info("[Raid] Current raid set: {}", raid.getRaidName());
            },
            () -> LOGGER.info("[Raid] Unknown Raid: {}", Final)
    );
}

    public static RaidModel getInstance() {
        return instance;
    }

    public RaidKind getCurrentRaid() {
        return currentRaid;
    }
    private static String lastTitle = "";

    public static void setLastTitle(String title) {
        lastTitle = title;
    }

    public static String getLastTitle() {
        return lastTitle;
    }
    public static String stripColors(String input) {
        return input.replaceAll("§[0-9a-fk-or]", "");
    }
}
