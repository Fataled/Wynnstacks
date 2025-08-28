package net.fataled.wynnstacks.client.rendering;

import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.fataled.wynnstacks.client.HudConfig.HudConfig;
import net.fataled.wynnstacks.client.RaidCounter;
import net.fataled.wynnstacks.client.Utilities.HudTextDraw;
import net.fataled.wynnstacks.client.Utilities.MobLabelUtils;
import net.fataled.wynnstacks.client.Utilities.RaycastUtils;
import net.fataled.wynnstacks.client.WynnstacksClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import static net.fataled.wynnstacks.client.Utilities.Utilities.stylePUAOnly;

public class HudRender {
    private static final HudRender INSTANCE = new HudRender();
    private static final Identifier HudRenderLayer = Identifier.of("wynnstacks", "hud_render_layer");
    private static final int HOLD_TICKS = 60;
    private UUID lastTargetId = null;
    private List<String> cachedLines = java.util.Collections.emptyList();
    private long holdUntilTick = 0;
    public static void registerHudCallback()
    {
        HudLayerRegistrationCallback.EVENT.register(layeredDrawer -> layeredDrawer.attachLayerBefore(IdentifiedLayer.CHAT, HudRenderLayer, INSTANCE::Render));
    }

    public void Render(DrawContext drawContext, RenderTickCounter tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        // 1) Raid counter block
        renderRaidCounter(drawContext);

        // 2) Acquire target + build live lines
        Entity target = RaycastUtils.getLookedAtEntity(
                mc,
                HudConfig.INSTANCE.range,
                HudConfig.INSTANCE.coneAngleDeg,
                HudConfig.INSTANCE.ignorePlayers
        );

        long currentTick = mc.world.getTime();
        List<String> liveLines = List.of();
        UUID currentId = null;

        if (target != null) {
            List<String> statLines = MobLabelUtils.getStatLines(target);
            if (!statLines.isEmpty()) {
                String label = MobLabelUtils.removeUnrenderableChars(
                        MobLabelUtils.getEntityLabelName(target), false
                ).trim();
                if (!label.isBlank()) {
                    statLines = new ArrayList<>(statLines);
                    statLines.addFirst(label);
                }
                liveLines = statLines;
                currentId = target.getUuid();
            }
        }

        // 3) Linger/cache logic
        boolean hasValidTarget = currentId != null && !liveLines.isEmpty();
        if (hasValidTarget) {
            if (!currentId.equals(lastTargetId)) lastTargetId = currentId; // instant switch
            cachedLines = new ArrayList<>(liveLines);
            holdUntilTick = currentTick + HOLD_TICKS;
        } else if (currentTick > holdUntilTick) {
            lastTargetId = null;
            cachedLines = java.util.Collections.emptyList();
        }

        // 4) Draw HUD (live or cached), movable via HudConfig x/y and scale
        if (HudConfig.INSTANCE.showHud && !cachedLines.isEmpty()) {
            MatrixStack ms = drawContext.getMatrices();

            ms.push();
            ms.scale(HudConfig.INSTANCE.obtain(HudConfig.DEBUFF).scale, HudConfig.INSTANCE.obtain(HudConfig.DEBUFF).scale, 1.0F);

            int x = HudConfig.INSTANCE.x;
            int y = HudConfig.INSTANCE.y;
            int lineheight = mc.textRenderer.fontHeight + 3;

            for (String line : cachedLines) {
                String cleaned = MobLabelUtils.removeUnrenderableChars(line, true).trim();
                if (cleaned.isEmpty()) continue;
                Text styled = stylePUAOnly(cleaned);
                String finished = styled.getString();
                HudTextDraw.draw(drawContext, finished, x,y, HudConfig.INSTANCE, HudConfig.DEBUFF);
                y += lineheight;
            }
            ms.pop();
        }

        // 5) Satsujin timer
        if (HudConfig.INSTANCE.showSatsujinHud) {
            if (WynnstacksClient.soundListener != null && WynnstacksClient.soundListener.getCountdownTicks() > 0) {
                MatrixStack ms = drawContext.getMatrices();
                ms.push();
                ms.scale(HudConfig.INSTANCE.obtain(HudConfig.SATSUJIN).scale, HudConfig.INSTANCE.obtain(HudConfig.SATSUJIN).scale, 1.0f);

                HudTextDraw.draw(drawContext, ("Satsujin Timer: " + (WynnstacksClient.soundListener.getCountdownTicks() / 20) + "s"),
                        HudConfig.INSTANCE.SatsujinX, HudConfig.INSTANCE.SatsujinY, HudConfig.INSTANCE, HudConfig.SATSUJIN
                );
                ms.pop();
            }
        }
    }

        private void renderRaidCounter(DrawContext drawContext) {
            if (!HudConfig.INSTANCE.showRaidCounter) return;

            int[] wins = RaidCounter.wins();
            int[] fails = RaidCounter.fails();

            String[] lines = new String[] {
                    "TNA:   " + wins[0] + " / " + fails[0],
                    "NoL:   " + wins[1] + " / " + fails[1],
                    "TCC:   " + wins[2] + " / " + fails[2],
                    "NoTG:  " + wins[3] + " / " + fails[3]
            };

            MatrixStack ms = drawContext.getMatrices();
            ms.push();
            ms.scale(HudConfig.INSTANCE.obtain(HudConfig.RAIDCOUNTER).scale, HudConfig.INSTANCE.obtain(HudConfig.RAIDCOUNTER).scale, 1.0f);

            for (int i = 0; i < lines.length; i++) {
                int y = HudConfig.INSTANCE.RaidCounterY + i * HudConfig.INSTANCE.lineGap;
                HudTextDraw.draw(drawContext, lines[i],HudConfig.INSTANCE.RaidCounterX, y, HudConfig.INSTANCE, HudConfig.RAIDCOUNTER);
            }
            ms.pop();
        }






    }




