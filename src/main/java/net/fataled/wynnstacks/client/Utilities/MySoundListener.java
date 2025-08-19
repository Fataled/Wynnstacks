package net.fataled.wynnstacks.client.Utilities;

import java.util.concurrent.ConcurrentLinkedQueue;

import net.fataled.wynnstacks.client.HudConfig.HudConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundInstanceListener;
import net.minecraft.client.sound.WeightedSoundSet;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class MySoundListener implements SoundInstanceListener {

    private static final ConcurrentLinkedQueue<Long> triggerQueue = new ConcurrentLinkedQueue<>();
    private volatile long lastProcessedTick = -1; // per-tick guard

    private static final Logger LOGGER = LogManager.getLogger("Sound Listener");
    private final MinecraftClient mc = MinecraftClient.getInstance();

    private static final Identifier SOUND_ONE =  Identifier.of("minecraft:block.end_portal.spawn");
    private static final Identifier SOUND_TWO =  Identifier.of("minecraft:block.enchantment_table.use");

    private static final int MAX_TICKS_BETWEEN = 10;

    private static final Pattern HEART_4_TO_9 = Pattern.compile("✜\\s*[4-9]");

    private long lastSoundOneTick = -100;
    private long lastSoundTwoTick = -100;
    private long lastTriggerSoundTick = -100;

    private int countdownTicks = 0;
    private boolean isCountdownActive = false;
    public boolean hasSoundPlayed = false;

    @Override
    public void onSoundPlayed(SoundInstance sound, WeightedSoundSet set, float volume) {
        if (mc.player == null || mc.world == null) return;

        final Identifier id = sound.getId();
        if (!id.equals(SOUND_ONE) && !id.equals(SOUND_TWO)) return;

        // Distance gating: ignore far/relative sounds to avoid spam
        if (!sound.isRelative()) {
            double dx = sound.getX() - mc.player.getX();
            double dy = sound.getY() - mc.player.getY();
            double dz = sound.getZ() - mc.player.getZ();
            double max = Math.max(8.0, HudConfig.INSTANCE.range + 12.0); // generous cushion
            if ((dx*dx + dy*dy + dz*dz) > (max*max)) return;
        }

        long tick = mc.world.getTime();

        // Update pair timestamps — this must stay cheap
        if (id.equals(SOUND_ONE)) lastSoundOneTick = tick;
        else                      lastSoundTwoTick = tick;

        // Minimal pairing logic here; NO raycasts/labels/stats on sound thread
        if (!isCountdownActive
                && Math.abs(lastSoundOneTick - lastSoundTwoTick) <= MAX_TICKS_BETWEEN
                && (tick - lastTriggerSoundTick) >= 5) // small cooldown to prevent burst spam
        {
            lastTriggerSoundTick = tick;
            triggerQueue.offer(tick);
        }
    }

    public void tick() {
        long tick = mc.world != null ? mc.world.getTime() : -1;

        // Drain one item per tick (or more if you want), but guard per tick
        Long queued = triggerQueue.poll();
        if (queued != null && tick != lastProcessedTick) {
            lastProcessedTick = tick;

            // Heavy work happens here on the main thread
            if (!isCountdownActive) {
                Entity nearby = RaycastUtils.getLookedAtEntity(
                        mc, HudConfig.INSTANCE.range, HudConfig.INSTANCE.coneAngleDeg, HudConfig.INSTANCE.ignorePlayers
                );
                if (nearby != null) {
                    String label = MobLabelUtils.getEntityLabelName(nearby);
                    List<String> stats = MobLabelUtils.getStatLines(nearby);
                    CheckAndStartCountdown(label, stats);
                }
            }
        }

        // Existing countdown logic
        if (countdownTicks > 0) {
            countdownTicks--;
            if (countdownTicks == 0) {
                isCountdownActive = false;
                if (HudConfig.INSTANCE.useEndSounds) {
                    playEndSound();
                }
            }
        }
    }

    public void CheckAndStartCountdown(String label, List<String> stats) {

        if(label == null || stats == null || stats.isEmpty()) return;
        String cleaned = label.toLowerCase(Locale.ROOT);

        boolean hasPriority = MobLabelUtils.PRIORITY_LABELS.stream().anyMatch(cleaned::contains);
        //LOGGER.info("name: {}", cleaned);
        if(!hasPriority) return;

        boolean hasMarks = false;

        for(String stat : stats) {
            if(stat != null && HEART_4_TO_9.matcher(stat).find()){hasMarks = true;}
        }
        //LOGGER.info("[In Check] Label: {} Stats: {}", label, stats);
        if(hasMarks) startCountdown();



    }

    private void startCountdown() {
        countdownTicks = HudConfig.INSTANCE.AspectLvl2 ? 10 * 20 : 15 * 20;
        isCountdownActive = true;
        hasSoundPlayed = false;
        //LOGGER.info("[SoundListener] Countdown started: {} ticks", countdownTicks);
    }

    public int getCountdownTicks() {
        return countdownTicks;
    }

    public void playEndSound() {
        if (hasSoundPlayed) return;
        //LOGGER.info("[SoundListener] Playing end sound.");
        mc.getSoundManager().play(
                PositionedSoundInstance.master(SoundEvents.ITEM_TRIDENT_RETURN, 1.0f, HudConfig.INSTANCE.Volume)
        );
        hasSoundPlayed = true;
    }
}
