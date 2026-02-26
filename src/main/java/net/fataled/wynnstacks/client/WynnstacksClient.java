package net.fataled.wynnstacks.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fataled.wynnstacks.client.HudConfig.HudConfig;
import net.fataled.wynnstacks.client.HudConfig.HudconfigManager;
import net.fataled.wynnstacks.client.Utilities.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fataled.wynnstacks.client.commands.commands;
import net.fataled.wynnstacks.client.rendering.HudRender;

public class WynnstacksClient implements ClientModInitializer {
    public static MySoundListener soundListener;
    private int Autosave = 0;
    private static final IgnPattern ign =  new IgnPattern();


    @Override
    public void onInitializeClient() {
        LoggerUtils.initLogger();
        HudconfigManager.load();
        KeybindManager.register();
        HudRender.registerHudCallback();

        // Set up sound listener after client starts
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            soundListener = new MySoundListener();
            client.getSoundManager().registerListener(soundListener);
            LoggerUtils.info("[Init] SoundListener registered.");
            commands.registerCommands();
        });

        // Tick countdown
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if(client.player == null || client.world == null) return;
            if (HudConfig.INSTANCE.showSatsujinHud && soundListener != null) {
                try {
                    soundListener.tick();
                    // (no logging each tick; too chatty)
                } catch (Exception e) {
                    LoggerUtils.error("SoundListener tick failed" + e);
                }
            }
            // Auto boss check when sound is triggered
            if(Autosave > 0) {
                Autosave--;
            }
            if(Autosave == 0) {
                HudconfigManager.save();
                Autosave = 6000;
            }
            if((client.world.getTime() % 20) == 0) ign.refreshIfChanged();
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            ign.resetPattern();
            client.execute(ign::resetPattern);
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, sender) -> ign.resetPattern());
    }
}
