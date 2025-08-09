package net.fataled.wynnstacks.client;

import net.fataled.wynnstacks.client.HudConfig.HudConfig;
import net.fataled.wynnstacks.client.HudConfig.HudconfigManager;
import net.fataled.wynnstacks.client.Utilities.*;
import net.minecraft.text.Text;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;


public class WynnstacksClient implements ClientModInitializer {
    private static final Logger LOGGER = LogManager.getLogger("Wynnstacks");
    public static MySoundListener soundListener;
    private static RaidModel raidModel;
    private RaidCounter raidCounter;

    @Override
    public void onInitializeClient() {
        HudconfigManager.load();
        KeybindManager.register();
        HudconfigManager saveSystem = new HudconfigManager();
        raidModel = new RaidModel();
        raidCounter = new RaidCounter();

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
            LOGGER.info("[Init] SoundListener registered.");
        });

        // Tick countdown
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (soundListener != null) {
                soundListener.tick();
            }
            if(raidCounter != null){
               raidCounter.checkRaidCompletion();
               raidCounter.checkRaidFailed();
            }
            // Auto boss check when sound is triggered

        });
        // Render HUD
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null || mc.world == null) return;

            if(mc.world.getTime() % 6000 == 0 ){
               saveSystem.saveSystem();
               LOGGER.info("Periodic Save done");
            }

            MatrixStack ms = drawContext.getMatrices();
            TextRenderer font = mc.textRenderer;

            int[] wins = RaidCounter.wins();
            int[] fails = RaidCounter.fails();

            String statsText =
                    "TNA:   " + wins[0] + " / " + fails[0] + " \n" +
                    "NoL:   " + wins[1] + " / " + fails[1] + " \n" +
                    "TCC:   " + wins[2] + " / " + fails[2] + " \n" +
                    "NoTG:  " + wins[3] + " / " + fails[3];

            String[] lines = statsText.split("\n");
            drawContext.getMatrices().push();
            drawContext.getMatrices().scale(HudConfig.INSTANCE.rcS, HudConfig.INSTANCE.rcS, 1.0f);

            if(HudConfig.INSTANCE.showRaidCounter) {
                for (int i = 0; i < lines.length; i++) {
                    int y = HudConfig.INSTANCE.rcY + i * HudConfig.INSTANCE.lineGap;
                    drawContext.drawTextWithShadow(font, lines[i], HudConfig.INSTANCE.rcX, y, HudConfig.INSTANCE.color);
                }
                drawContext.getMatrices().pop();
            }
            Entity target = RaycastUtils.getLookedAtEntity(
                    mc,
                    HudConfig.INSTANCE.range,
                    HudConfig.INSTANCE.coneAngleDeg,
                    HudConfig.INSTANCE.ignorePlayers
            );

            List<String> statLines = (target != null)
                    ? MobLabelUtils.getStatLines(target)
                    : List.of();
            if (statLines.isEmpty()) return;

            // Get and prepend label
            String label = MobLabelUtils.removeUnrenderableChars(
                    MobLabelUtils.getEntityLabelName(target)
            ).trim();

            if (!label.isBlank()) {
                statLines = new ArrayList<>(statLines);
                statLines.addFirst(label);
            }

            ms.push();
            ms.scale(HudConfig.INSTANCE.scale, HudConfig.INSTANCE.scale, 1.0F);

            int x = HudConfig.INSTANCE.x;
            int y = HudConfig.INSTANCE.y;
            int color = HudConfig.INSTANCE.color;



            if (HudConfig.INSTANCE.showHud) {
                for (String line : statLines) {
                    String cleaned = MobLabelUtils.removeUnrenderableChars(line).trim();
                    if (!cleaned.isEmpty()) {
                        drawContext.drawTextWithShadow(font, Text.literal(cleaned), x, y, color);
                        y += (int) HudConfig.INSTANCE.scale * 10;
                    }
                }
                ms.pop();

                if (soundListener.getCountdownTicks() > 0) {
                    drawContext.getMatrices().push();
                    drawContext.getMatrices().scale(HudConfig.INSTANCE.sScale, HudConfig.INSTANCE.sScale, 1.0f);
                    drawContext.drawTextWithShadow(
                            font,
                            Text.literal("Satsujin Timer: " + soundListener.getCountdownTicks() / 20 + "s"),
                            HudConfig.INSTANCE.Satx,
                            HudConfig.INSTANCE.Saty,
                            HudConfig.INSTANCE.color
                    );
                    drawContext.getMatrices().pop();
                }
            }
        });
    }
}
