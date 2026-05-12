package net.fataled.wynnstacks.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fataled.wynnstacks.client.config.HudConfig;
import net.fataled.wynnstacks.client.config.HudConfigManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fataled.wynnstacks.client.label.IgnPattern;
import net.fataled.wynnstacks.client.input.KeybindManager;
import net.fataled.wynnstacks.client.util.LoggerUtils;
import net.fataled.wynnstacks.client.sound.SoundListener;
import net.fataled.wynnstacks.client.commands.Commands;
import net.fataled.wynnstacks.client.rendering.HudRender;

public class WynnstacksClient implements ClientModInitializer {
    public static SoundListener soundListener;
    private int autoSaveTicks = 6000;


    @Override
    public void onInitializeClient() {
        HudConfigManager.load();
        KeybindManager.register();
        HudRender.registerHudCallback();

        // Set up sound listener after client starts
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            soundListener = new SoundListener();
            client.getSoundManager().registerListener(soundListener);
            LoggerUtils.info("[Init] SoundListener registered.");
            Commands.registerCommands();
        });

        // Tick countdown
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if(client.player == null || client.world == null) return;
            if (HudConfig.INSTANCE.showSatsujinHud && soundListener != null) {
                try {
                    soundListener.tick();
                    // (no logging each tick; too chatty)
                } catch (Exception e) {
                    LoggerUtils.error("SoundListener tick failed", e);
                }
            }
            // Auto boss check when sound is triggered
            if(autoSaveTicks > 0) {
                autoSaveTicks--;
            }
            if(autoSaveTicks == 0) {
                HudConfigManager.save();
                autoSaveTicks = 6000;
            }
            if((client.world.getTime() % 20) == 0) IgnPattern.INSTANCE.refreshIfChanged();
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            client.execute(IgnPattern.INSTANCE::resetPattern);
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, sender) -> IgnPattern.INSTANCE.resetPattern());
    }
}
