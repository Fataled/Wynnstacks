package net.fataled.shadestepperqol.shadestepper_qol_huds.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

import static net.fataled.shadestepperqol.shadestepper_qol_huds.client.ChatReader.stripColors;

public class RaidCounter {
    private static final Logger LOGGER = LogManager.getLogger("ShadestepperHUD");
    RaidModel raidModel = new RaidModel();
    private static final Map<String, Boolean> raidFlags = new HashMap<>();

    static {
        raidFlags.put("The Nameless Anomaly", false);
        raidFlags.put("Orphion's Nexus of Light", false); // NOL
        raidFlags.put("The Canyon Colossus", false);  // TCC
        raidFlags.put("Nest of The Grootslang", false); // NoTG
    }

    private static final Map<String , Integer> raidCompletions = new HashMap<>();

    static {
        raidCompletions.put("TNA",0);
        raidCompletions.put("NoL",0);
        raidCompletions.put("TCC",0);
        raidCompletions.put("NoTG",0);

    }

    private static final Map<String , Integer> raidFails = new HashMap<>();

    static {
        raidFails.put("TNA",0);
        raidFails.put("NoL",0);
        raidFails.put("TCC",0);
        raidFails.put("NoTG",0);

    }
    private static final Map<String, String> titleToRaidCode = Map.of(
            "The Nameless Anomaly", "TNA",
            "Orphion's Nexus of Light", "NOL",
            "The Canyon Colossus", "TCC",
            "Nest of The Grootslang", "NoTG"
    );
    private static String lastCountedTitle = "";

    private long lastCompletionTime = 0;
    private static final long COOLDOWN_MS = 5000; // 5 seconds
    private final long now = System.currentTimeMillis();
    public void checkRaidCompletion() {
        String lastTitle = RaidModel.getLastTitle();

        if (!lastTitle.equalsIgnoreCase("Raid Completed!")) return;


        if (now - lastCompletionTime < COOLDOWN_MS) return;

        RaidKind activeRaid = RaidModel.getInstance().getCurrentRaid();
        if (activeRaid == null) {
           // LOGGER.warn("[Raid] 'Raid Completed!' shown, but currentRaid is null.");
            return;
        }

        String raidCode = titleToRaidCode.getOrDefault(activeRaid.getEntryTitleRaw(), "UNKNOWN");
        int current = raidCompletions.getOrDefault(raidCode, 0);
        raidCompletions.put(raidCode, current + 1);
        lastCompletionTime = now;

       // LOGGER.info("[Raid] Completion counted for: {}", activeRaid.getRaidName());
    }

    public void checkRaidFailed() {
        String lastTitle = RaidModel.getLastTitle();
        String Cleaned = stripColors(lastTitle);

        if (!Cleaned.equalsIgnoreCase("Raid Failed!")) return; // Not a new Fail event

        if (now - lastCompletionTime < COOLDOWN_MS) return;

        RaidKind activeRaid = RaidModel.getInstance().getCurrentRaid();
        if (activeRaid == null) {
            //LOGGER.warn("[Raid] 'Raid Failed!' shown, but currentRaid is null.");
            return;
        }

        String raidCode = titleToRaidCode.getOrDefault(activeRaid.getEntryTitleRaw(), "UNKNOWN");
        int current = raidFails.getOrDefault(raidCode, 0);
        raidFails.put(raidCode, current + 1);
        lastCompletionTime = now;

       // LOGGER.info("[Raid] Failure counted for: {}", activeRaid.getRaidName());
    }

    public static void setActiveRaid(String raidName) {
        raidFlags.replaceAll((name, oldVal) -> name.equals(raidName));
    }

    public static boolean isInRaid(String name) {
        return raidFlags.getOrDefault(name, false);
    }
    public static int[] wins(){
        return new int[] {raidCompletions.getOrDefault("TNA", 0),
                raidCompletions.getOrDefault("NOL", 0),
                raidCompletions.getOrDefault("TCC", 0),
                raidCompletions.getOrDefault("NoTG", 0)};
    }
    public static int[] fails(){
        return new int[] {raidFails.getOrDefault("TNA", 0),
                raidFails.getOrDefault("NOL", 0),
                raidFails.getOrDefault("TCC", 0),
                raidFails.getOrDefault("NoTG", 0)};
    }
}
