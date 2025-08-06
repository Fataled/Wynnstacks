package net.fataled.shadestepperqol.shadestepper_qol_huds.client;

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
import java.util.Optional;

public class MySoundListener implements SoundInstanceListener {
    private static final Logger LOGGER = LogManager.getLogger("ShadestepperHUD");
    private final MinecraftClient mc = MinecraftClient.getInstance();

    private static final Identifier SOUND_ONE =  Identifier.of("minecraft:block.end_portal.spawn");
    private static final Identifier SOUND_TWO =  Identifier.of("minecraft:block.enchantment_table.use");

    private static final int MAX_TICKS_BETWEEN = 10;

    private long lastSoundOneTick = -100;
    private long lastSoundTwoTick = -100;
    private long lastTriggerSoundTick = -100;

    private int countdownTicks = 0;
    private boolean isCountdownActive = false;
    public boolean hasSoundPlayed = false;

    @Override
    public void onSoundPlayed(SoundInstance sound, WeightedSoundSet soundSet, float volume) {
        if (mc.player == null || mc.world == null) return;

        Identifier id = sound.getId();
        if (!id.equals(SOUND_ONE) && !id.equals(SOUND_TWO)) return;

        long currentTick = mc.world.getTime();
        // cooldown + distance checks…
        // (same as before, updating lastSoundOneTick/lastSoundTwoTick)
        if (!isCountdownActive) {
        // once you’ve updated lastSoundOneTick & lastSoundTwoTick:
            if (Math.abs(lastSoundOneTick - lastSoundTwoTick) <= MAX_TICKS_BETWEEN) {
                // **valid sound pair!**
                //LOGGER.info("[SoundListener] Trigger pair matched at tick {}", currentTick);
                lastTriggerSoundTick = currentTick;

                // now *immediately* check labels and start countdown if appropriate:
                // — you may need to schedule this onto the render thread,
                //   but for simplicity you can do it here if thread-safe.

                // ① look for any high-priority entity in range
                List<Entity> nearby = mc.world.getOtherEntities(
                        mc.player,
                        mc.player.getBoundingBox().expand(HudConfig.INSTANCE.range),
                        Entity::isAlive
                );
                Optional<Entity> priority = MobLabelUtils.getHighestPriorityEntity(nearby);

                String label = null;
                if (priority.isPresent()) {
                    label = MobLabelUtils.getEntityLabelName(priority.get());
                } else {
                    // ② fallback to ray cast
                    Entity looked = RaycastUtils.getLookedAtEntity(
                            mc, HudConfig.INSTANCE.range, HudConfig.INSTANCE.coneAngleDeg, HudConfig.INSTANCE.ignorePlayers
                    );
                    if (looked != null) {
                        label = MobLabelUtils.getEntityLabelName(looked);
                    }
                }

                // ③ if label matches your boss keywords, start the countdown
                if (label != null) {
                    String cleaned = label.toLowerCase();
                    for (String kw : MobLabelUtils.PRIORITY_LABELS) {
                        if (cleaned.contains(kw)) {
                            //LOGGER.info("[SoundListener] Matched priority label: {} → starting countdown", kw);
                            startCountdown();
                            break;
                        }
                    }
                }
            }
        }
    }

    private void startCountdown() {
        countdownTicks = HudConfig.INSTANCE.AspectLvl2 ? 10 * 20 : 15 * 20;
        isCountdownActive = true;
        hasSoundPlayed = false;
        //LOGGER.info("[SoundListener] Countdown started: {} ticks", countdownTicks);
    }

    public void tick() {
        if (countdownTicks > 0) {
            countdownTicks--;
            if (countdownTicks == 0) {
                isCountdownActive = false;
                //LOGGER.info("[SoundListener] Countdown ended.");
                if (HudConfig.INSTANCE.useEndSounds) {
                    playEndSound();
                }
            }
        }
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
