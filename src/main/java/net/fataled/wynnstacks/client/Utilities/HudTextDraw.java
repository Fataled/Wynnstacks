package net.fataled.wynnstacks.client.Utilities;

import net.fataled.wynnstacks.client.HudConfig.HudConfig;
import net.minecraft.client.gui.DrawContext;

public class HudTextDraw {
    private HudTextDraw() {}

    /** Convenience: draw using a named profile (e.g., "debuff"). */
    public static void draw(DrawContext ctx, String text, int x, int y,
                            HudConfig config, String profileKey) {
        var p = config.obtain(profileKey);

        var opt = p.useGradient
                ? Utilities.Options.gradient(p.gradientStartRgb, p.gradientEndRgb)
                : Utilities.Options.solid(p.solidRgb);

        opt.outline(p.outlineRgb, p.outlineThicknessPx)
                .shadow(p.shadowOnMain)
                .align(Utilities.Align.valueOf(p.align))   // "LEFT"/"CENTER"/"RIGHT"
                .scale(p.scale);

        Utilities.draw(ctx, text, x, y, opt);
    }
}
