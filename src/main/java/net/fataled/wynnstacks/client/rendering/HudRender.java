package net.fataled.wynnstacks.client.rendering;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fataled.wynnstacks.client.HudConfig.HudConfig;
import net.fataled.wynnstacks.client.Utilities.HudTextDraw;
import net.fataled.wynnstacks.client.Utilities.MobLabelUtils;
import net.fataled.wynnstacks.client.Utilities.RaycastUtils;
import net.fataled.wynnstacks.client.WynnstacksClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2fStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import static net.fataled.wynnstacks.client.Utilities.Utilities.stylePUAOnly;

public class HudRender {
    private static final HudRender INSTANCE = new HudRender();
    private static final Identifier HudRenderLayer = Identifier.of("wynnstacks", "hud_render_layer");
    private static final int HOLD_TICKS = 15;
    private UUID lastTargetId = null;
    private List<String> cachedLines = java.util.Collections.emptyList();
    private long holdUntilTick = 0;
    public static void registerHudCallback()
    {
        HudElementRegistry.attachElementAfter(VanillaHudElements.CHAT, HudRenderLayer, INSTANCE::Render);
    }

    public void Render(DrawContext drawContext, RenderTickCounter tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        // 1) Raid counter block
        //renderRaidCounter(drawContext);

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

                //if(!containsAnySubstring(label, MobLabelUtils.PRIORITY_LABELS)) return;

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

            Matrix3x2fStack ms = drawContext.getMatrices();

            ms.pushMatrix();
            ms.scale(HudConfig.INSTANCE.obtain(HudConfig.DEBUFF).scale, HudConfig.INSTANCE.obtain(HudConfig.DEBUFF).scale);

            int x = HudConfig.INSTANCE.x;
            int y = HudConfig.INSTANCE.y;
            int lineheight = mc.textRenderer.fontHeight + 3;

            for (String line : cachedLines) {
                String cleaned = MobLabelUtils.removeUnrenderableChars(line, true).trim();
                if (cleaned.isEmpty()) continue;
                Text styled = stylePUAOnly(cleaned);
                HudTextDraw.draw(drawContext, styled, x,y, HudConfig.INSTANCE, HudConfig.DEBUFF);
                y += lineheight;
            }
            ms.popMatrix();
        }

        // 5) Satsujin timer
        if (HudConfig.INSTANCE.showSatsujinHud) {
            if (WynnstacksClient.soundListener != null && WynnstacksClient.soundListener.getCountdownTicks() > 0) {
                Matrix3x2fStack ms = drawContext.getMatrices();
                ms.pushMatrix();
                ms.scale(HudConfig.INSTANCE.obtain(HudConfig.SATSUJIN).scale, HudConfig.INSTANCE.obtain(HudConfig.SATSUJIN).scale);

                HudTextDraw.draw(drawContext, Text.of("Satsujin Timer: " + (WynnstacksClient.soundListener.getCountdownTicks() / 20) + "s"),
                        HudConfig.INSTANCE.SatsujinX, HudConfig.INSTANCE.SatsujinY, HudConfig.INSTANCE, HudConfig.SATSUJIN
                );
                ms.popMatrix();
            }
        }

        drawContext.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.literal("Visible"), 50, 50, 0xFFFFF);
    }

        private boolean containsAnySubstring(String mainWord, List<String> keywords){
            for (String keyword : keywords){
                if(mainWord.toLowerCase().contains(keyword)){
                    return true;
                }
            }
            return false;
        }




    }




