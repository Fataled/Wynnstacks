package net.fataled.wynnstacks.client.Utilities;

import net.fataled.wynnstacks.client.HudConfig.HudConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class HudTextDraw {
    private HudTextDraw() {}

    /** Convenience: draw using a named profile (e.g., "debuff"). */
    public static void draw(DrawContext ctx, Text text, int x, int y,
                            HudConfig config, String profileKey) {
        var p = config.obtain(profileKey);

        var opt = Utilities.Options.solid(p.solidRgb);

        opt.outline(p.outlineRgb, p.outlineThicknessPx)
                .shadow(p.shadowOnMain)
                .align(Utilities.Align.valueOf(p.align))   // "LEFT"/"CENTER"/"RIGHT"
                .scale(p.scale);

        Utilities.draw(ctx, text, x, y, opt);
    }
}
