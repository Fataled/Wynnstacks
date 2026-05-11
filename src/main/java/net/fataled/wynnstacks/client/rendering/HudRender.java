package net.fataled.wynnstacks.client.rendering;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fataled.wynnstacks.client.HudConfig.HudConfig;
import net.fataled.wynnstacks.client.Utilities.MobLabelUtils;
import net.fataled.wynnstacks.client.Utilities.RaycastUtils;
import net.fataled.wynnstacks.client.WynnstacksClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import static net.fataled.wynnstacks.client.Utilities.Utilities.stylePUAOnly;

public class HudRender {
    private static final HudRender INSTANCE = new HudRender();
    private static final Identifier HUD_RENDER_LAYER = Identifier.of("wynnstacks", "hud_render_layer");
    private static final int HOLD_TICKS = 15;
    private UUID lastTargetId = null;
    private List<String> cachedLines = java.util.Collections.emptyList();
    private long holdUntilTick = 0;
    public static void registerHudCallback()
    {
        HudElementRegistry.attachElementAfter(VanillaHudElements.CHAT, HUD_RENDER_LAYER, INSTANCE::render);
    }



    public void render(DrawContext drawContext, RenderTickCounter tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;
        // 1) Raid counter block

        // 2) Acquire target + build live lines
        Entity target = RaycastUtils.getLookedAtEntity(
                mc,
                HudConfig.INSTANCE.coneAngleDeg,
                HudConfig.INSTANCE.ignorePlayers
        );

        long currentTick = mc.world.getTime();
        List<String> liveLines = List.of();
        UUID currentId = null;

        // 3) Satsujin timer
        if (HudConfig.INSTANCE.showSatsujinHud) {
            renderSatsujinHud(drawContext, mc);
        }

        if (target == null) return;
        List<String> statLines = MobLabelUtils.getStatLines(target);
        if (!statLines.isEmpty()) {
            String label = MobLabelUtils.removeUnrenderableChars(
                    MobLabelUtils.getEntityLabelName(target), false
            ).trim();

            if(!MobLabelUtils.isPriority(label)) return;

            if (!label.isBlank()) {
                statLines = new ArrayList<>(statLines);
                statLines.addFirst(label);
            }
            liveLines = statLines;
            currentId = target.getUuid();
        }

        // 4) Linger/cache logic
        boolean hasValidTarget = currentId != null && !liveLines.isEmpty();
        if (hasValidTarget) {
            if (!currentId.equals(lastTargetId)) lastTargetId = currentId; // instant switch
            cachedLines = liveLines;
            holdUntilTick = currentTick + HOLD_TICKS;
        } else if (currentTick > holdUntilTick) {
            lastTargetId = null;
            cachedLines = java.util.Collections.emptyList();
        }

        // 5) Draw HUD (live or cached), movable via HudConfig x/y and scale

        if (HudConfig.INSTANCE.showHud && !cachedLines.isEmpty()) {
            renderDebuffHud(drawContext, mc);
        }

    }

    private void renderSatsujinHud(DrawContext ctx, MinecraftClient mc){
        if (!(WynnstacksClient.soundListener != null && WynnstacksClient.soundListener.getCountdownTicks() > 0)) return;
        var ms = ctx.getMatrices();
        var satsu = HudConfig.INSTANCE.obtain(HudConfig.SATSUJIN);
        float scale = satsu.scale;
        int x = HudConfig.INSTANCE.satsujinX;
        int y = HudConfig.INSTANCE.satsujinY;
        ms.pushMatrix();
        try {
            ms.scale(scale, scale);

            int drawX = Math.round(x / scale);
            int drawY = Math.round(y / scale);


            ctx.drawTextWithShadow(
                    mc.textRenderer,
                    Text.of("Satsujin Timer: " + (WynnstacksClient.soundListener.getCountdownTicks() / 20) + "s"),
                    drawX,
                    drawY,
                    0xFFFFFFFF
            );
        } finally {
            ms.popMatrix();
        }
    }

    private void renderDebuffHud(DrawContext ctx, MinecraftClient mc){
        var ms = ctx.getMatrices();
        var debuff = HudConfig.INSTANCE.obtain(HudConfig.DEBUFF);
        float scale = debuff.scale;

        int x = HudConfig.INSTANCE.x;
        int y = HudConfig.INSTANCE.y;

        ms.pushMatrix();
        try {
            ms.scale(scale, scale);

            int drawX = Math.round(x / scale);
            int drawY = Math.round(y / scale);

            int lineHeight = mc.textRenderer.fontHeight + 3;

            for (String line : cachedLines) {
                Text styled = stylePUAOnly(line);

                ctx.drawTextWithShadow(
                        mc.textRenderer,
                        styled,
                        drawX,
                        drawY,
                        0xFFFFFFFF
                );

                drawY += lineHeight;
            }
        } finally {
            ms.popMatrix();
        }

    }
}




