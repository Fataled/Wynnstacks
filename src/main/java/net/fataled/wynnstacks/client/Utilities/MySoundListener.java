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

public class MySoundListener implements SoundInstanceListener {

    private static final Logger LOGGER = LogManager.getLogger("Sound Listener");

    private static final Identifier SOUND_ONE = Identifier.of("minecraft:block.end_portal.spawn");
    private static final Identifier SOUND_TWO = Identifier.of("minecraft:block.enchantment_table.use");
    // How far apart the pair can be (in ticks)
    private static final int MAX_TICKS_BETWEEN = 10;
    private static final int MIN_GAP_BETWEEN_TRIGGERS = 3;
    // Coalesce multiple triggers that arrive within this window
    private static final int COALESCE_WINDOW_TICKS = 5;

    // Hard throttle heavy eval
    private static final int EVAL_COOLDOWN_TICKS = 5;

    // Light queue (sound thread -> client tick thread)
    private static final ConcurrentLinkedQueue<Long> triggerQueue = new ConcurrentLinkedQueue<>();

    private final MinecraftClient mc = MinecraftClient.getInstance();

    private long lastSoundOneTick = -1000;
    private long lastSoundTwoTick = -1000;
    private long lastTriggerSoundTick = -1000;

    private long lastProcessedTick = -1;
    private long lastEvalTick = -1000;

    // Countdown state
    private int countdownTicks = 0;
    private boolean isCountdownActive = false;
    public boolean hasSoundPlayed = false;

    // Tiny cache to avoid recomputing for the exact same looked-at entity every frame
    private int cachedEntityId = -1;
    private long cachedEntityTick = -1000;
    private static final int TARGET_CACHE_TICKS = 6;

   @Override
    public void onSoundPlayed(SoundInstance sound, WeightedSoundSet set, float volume) {
        if (mc.player == null || mc.world == null) return;

        final Identifier id = sound.getId();
        final long tick = mc.world.getTime();

        // optional distance gate (can comment out for testing)
        if (!sound.isRelative()) {
            double dx = sound.getX() - mc.player.getX();
            double dy = sound.getY() - mc.player.getY();
            double dz = sound.getZ() - mc.player.getZ();
            double max = Math.max(24.0, HudConfig.INSTANCE.range + 24.0);
            if ((dx*dx + dy*dy + dz*dz) > (max*max)) return;
        }

        // update whichever timestamp matches
        if (id.equals(SOUND_ONE)) {
            lastSoundOneTick = tick;
        } else if (id.equals(SOUND_TWO)) {
            lastSoundTwoTick = tick;
        } else {
            return;
        }

        // if both sounds are close in time, trigger once
        if (!isCountdownActive
                && Math.abs(lastSoundOneTick - lastSoundTwoTick) <= MAX_TICKS_BETWEEN
                && (tick - lastTriggerSoundTick) >= MIN_GAP_BETWEEN_TRIGGERS)
        {
            lastTriggerSoundTick = tick;
            triggerQueue.offer(tick);
            LOGGER.info("[SND] pair OK: one={}, two={}, Δ={}",
                    lastSoundOneTick, lastSoundTwoTick,
                    Math.abs(lastSoundOneTick - lastSoundTwoTick));
        }
    }

    public void tick() {
        if (mc.world == null) return;
        final long tick = mc.world.getTime();
        if (tick == lastProcessedTick) return;
        lastProcessedTick = tick;

        // Drain & coalesce triggers into a single "effective" trigger within window
        Long trigger = null;
        Long t;
        while ((t = triggerQueue.poll()) != null) {
            if (trigger == null || (tick - t) <= COALESCE_WINDOW_TICKS) {
                trigger = t; // keep most recent within window
            }
        }

        // No new trigger or we’re throttled / already active
        if (trigger == null || isCountdownActive || (tick - lastEvalTick) < EVAL_COOLDOWN_TICKS) {
            progressCountdown(tick);
            return;
        }

        lastEvalTick = tick;

        // Snapshot config once (avoid multiple volatile reads)
        final float range = (float) HudConfig.INSTANCE.range;
        final float cone = HudConfig.INSTANCE.coneAngleDeg;
        final boolean ignorePlayers = HudConfig.INSTANCE.ignorePlayers;

        Entity target = getTargetWithCache(range, cone, ignorePlayers, tick);
        if (target != null) {
            String label = MobLabelUtils.getEntityLabelName(target);
            List<String> stats = MobLabelUtils.getStatLines(target);
            if (shouldStart(label, stats)) {
                startCountdown();
                LOGGER.info("Starting countdown");
            }
        }

        progressCountdown(tick);
    }

    private void progressCountdown(long tick) {
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

    private Entity getTargetWithCache(float range, float cone, boolean ignorePlayers, long tick) {
        // Try a fresh raycast
        Entity e = RaycastUtils.getLookedAtEntity(mc, range, cone, ignorePlayers);
        if (e != null) {
            cachedEntityId = e.getId();
            cachedEntityTick = tick;
            return e;
        }

        // If raycast fails but cache is fresh, try to resolve the cached id (player moved slightly)
        if ((tick - cachedEntityTick) <= TARGET_CACHE_TICKS && cachedEntityId != -1) {
            if (mc.world != null) {
                Entity cached = mc.world.getEntityById(cachedEntityId);
                if (cached != null) return cached;
            }
        }
        // Expire cache
        cachedEntityId = -1;
        return null;
    }

    private boolean shouldStart(String label, List<String> stats) {
        if (label == null || stats == null || stats.isEmpty()) return false;

        // priority name check (lower once)
        String cleaned = label.toLowerCase(Locale.ROOT);
        boolean hasPriority = MobLabelUtils.PRIORITY_LABELS.stream().anyMatch(cleaned::contains);
        if (!hasPriority) return false;

        // cheap heart 4–9 check without regex
        for (String s : stats) {
            if (s == null || s.isEmpty()) continue;
            if (hasHeart4to9(s)) return true;
        }
        return false;
    }

    // Looks for U+271C '✜' followed by optional spaces and a digit 4–9.
    private static boolean hasHeart4to9(String s) {
        int len = s.length();
        for (int i = 0; i < len; ) {
            int cp = s.codePointAt(i);
            int next = i + Character.charCount(cp);
            if (cp == 0x271C) { // ✜
                // skip spaces
                int j = next;
                while (j < len) {
                    int cp2 = s.codePointAt(j);
                    if (!Character.isWhitespace(cp2)) {
                        // digit?
                        if (cp2 >= '4' && cp2 <= '9') return true;
                        break;
                    }
                    j += Character.charCount(cp2);
                }
            }
            i = next;
        }
        return false;
    }

    private void startCountdown() {
        countdownTicks = HudConfig.INSTANCE.AspectLvl2 ? 10 * 20 : 15 * 20;
        isCountdownActive = true;
        hasSoundPlayed = false;
        // LOGGER.info("[SoundListener] Countdown started: {} ticks", countdownTicks);
    }

    public int getCountdownTicks() { return countdownTicks; }

    public void playEndSound() {
        if (hasSoundPlayed) return;
        mc.getSoundManager().play(
                PositionedSoundInstance.master(SoundEvents.ITEM_TRIDENT_RETURN, 1.0f, HudConfig.INSTANCE.Volume)
        );
        hasSoundPlayed = true;
    }
}
