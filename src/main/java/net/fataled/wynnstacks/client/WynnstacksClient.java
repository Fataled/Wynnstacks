package net.fataled.wynnstacks.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fataled.wynnstacks.client.HudConfig.HudConfig;
import net.fataled.wynnstacks.client.HudConfig.HudconfigManager;
import net.fataled.wynnstacks.client.Utilities.*;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fataled.wynnstacks.client.raidRelated.RaidModel;
import net.fataled.wynnstacks.client.rendering.HudRender;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



public class WynnstacksClient implements ClientModInitializer {
    private static final Logger LOGGER = LogManager.getLogger("Wynnstacks");
    public static MySoundListener soundListener;
    private RaidCounter raidCounter;
    private int Autosave = 0;
    private static final IgnPattern ign =  new IgnPattern();

    @Override
    public void onInitializeClient() {
        RaidModel raidModel = new RaidModel();
        HudconfigManager.load();
        KeybindManager.register();
        raidCounter = new RaidCounter();
        HudRender.registerHudCallback();


        // Register resource pack
        ModContainer container = FabricLoader.getInstance()
                .getModContainer("wynnstacks")
                .orElseThrow(() -> new IllegalStateException("Mod container not found"));

        ResourceManagerHelper.registerBuiltinResourcePack(
                Identifier.of("wynnstacks", "wynnstacks"),
                container,
                ResourcePackActivationType.ALWAYS_ENABLED
        );

        // Set up sound listener after client starts
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            soundListener = new MySoundListener();
            client.getSoundManager().registerListener(soundListener);
            //LOGGER.info("[Init] SoundListener registered.");
        });

        // Tick countdown
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if(client.player == null || client.world == null) return;
            try{
                raidCounter.RaidChecks(client);
            } catch (Throwable t){
                WynnstacksClient.LOGGER.error("RaidCounter tick failed Check, {}", t.getMessage());
            }
            if (HudConfig.INSTANCE.showSatsujinHud && soundListener != null) {
                try {
                    soundListener.tick();
                    // (no logging each tick; too chatty)
                } catch (Exception e) {
                    WynnstacksClient.LOGGER.error("SoundListener tick failed", e);
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
