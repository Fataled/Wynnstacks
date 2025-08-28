package net.fataled.wynnstacks.client.Utilities;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;


public class Utilities {

    private static final Identifier PUA_FONT = Identifier.of("wynnstacks","stat_icons");

    private static boolean isPUA(int cp) { return cp >= 0xE000 && cp <= 0xF8FF; }

    public static Text stylePUAOnly(String s) {
        MutableText out = Text.empty();
        int i = 0, n = s.length();
        while (i < n) {
            int j = i;
            boolean pua = isPUA(s.codePointAt(i));
            while (j < n) {
                int cp = s.codePointAt(j);
                if (isPUA(cp) != pua) break;
                j += Character.charCount(cp);
            }
            MutableText seg = Text.literal(s.substring(i, j));
            if (pua) seg = seg.styled(st -> st.withFont(PUA_FONT)); // <- force your sheet
            out.append(seg);
            i = j;
        }
        return out;
    }

    private Utilities() {}

    // ---------- Public API ----------
    public enum Align { LEFT, CENTER, RIGHT }

    public static final class Options {
        // Fill
        public boolean useGradient = false;
        public int solidRgb = 0xFFFFFF;
        public int gradientStartRgb = 0x00FFFF;
        public int gradientEndRgb   = 0xFF00FF;

        // Outline
        public float outlineThicknessPx = 0f;   // 0 = off
        public int outlineRgb = 0x101018;
        public boolean shadowOnMain = false; // vanilla shadow on main text

        // Layout
        public Align align = Align.LEFT;
        public float scale = 1.0f; // 1.0 = normal; scales both text and outline

        // Builder-ish helpers
        public static Options solid(int rgb) {
            Options o = new Options();
            o.useGradient = false;
            o.solidRgb = rgb;
            return o;
        }
        public static Options gradient(int startRgb, int endRgb) {
            Options o = new Options();
            o.useGradient = true;
            o.gradientStartRgb = startRgb;
            o.gradientEndRgb = endRgb;
            return o;
        }
        public Options outline(int rgb, float thicknessPx) {
            this.outlineRgb = rgb;
            this.outlineThicknessPx = Math.max(0, thicknessPx);
            return this;
        }
        public Options shadow(boolean on) { this.shadowOnMain = on; return this; }
        public Options align(Align a) { this.align = a; return this; }
        public Options scale(float s) { this.scale = Math.max(0.001f, s); return this; }
    }

    /** Main entry: draws text at (x,y) with the given options. */
    public static void draw(DrawContext ctx, String text, int x, int y, Options opt) {
        if (text == null || text.isEmpty()) return;
        final TextRenderer tr = MinecraftClient.getInstance().textRenderer;

        // Build the fill Text (single object; kerning preserved)
        final Text fillText = opt.useGradient
                ? buildGradient(text, opt.gradientStartRgb, opt.gradientEndRgb)
                : Text.literal(text).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(opt.solidRgb)));

        // Horizontal alignment offset (compute BEFORE scaling)
        int width = tr.getWidth(text);
        int xAligned = switch (opt.align) {
            case LEFT -> x;
            case CENTER -> x - Math.round(width * opt.scale * 0.5f);
            case RIGHT -> x - Math.round(width * opt.scale);
        };

        // Apply scale
        var ms = ctx.getMatrices();
        ms.push();
        ms.translate(xAligned, y, 0);
        if (opt.scale != 1.0f) ms.scale(opt.scale, opt.scale, 1f);

        // Outline (flat color, drawn using the plain string)
        if (opt.outlineThicknessPx > 0) {
            // Keep outline roughly "pixel thick" under scaling
            float step = 1f / opt.scale; // 1 screen px in local coords
            float r = opt.outlineThicknessPx;
            for (float ox = -r; ox <= r; ox++) {
                for (float oy = -r; oy <= r; oy++) {
                    if (ox == 0 && oy == 0) continue;
                    // simple diamond mask to avoid square corners; comment this out for square
                    if (Math.abs(ox) + Math.abs(oy) > r + 1) continue;
                    int drawX = Math.round(ox * step);
                    int drawY = Math.round(oy * step);
                    ctx.drawText(tr, text, drawX, drawY, opt.outlineRgb, false);
                }
            }
        }

        // Main fill on top (gradient or solid)
        ctx.drawText(tr, fillText, 0, 0, 0xFFFFFF, opt.shadowOnMain);

        ms.pop();
    }

    // ---------- Internals ----------
    private static Text buildGradient(String text, int startRgb, int endRgb) {
        MutableText out = Text.empty();
        final int len = text.codePointCount(0, text.length());
        int i = 0, off = 0;
        while (off < text.length()) {
            int cp = text.codePointAt(off);
            float t = (len <= 1) ? 0f : (float) i / (float) (len - 1);
            int rgb = lerpRgb(startRgb, endRgb, t);
            out = out.append(Text.literal(new String(Character.toChars(cp)))
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb))));
            off += Character.charCount(cp);
            i++;
        }
        return out;
    }

    private static int lerpRgb(int a, int b, float t) {
        int ar=(a>>16)&0xFF, ag=(a>>8)&0xFF, ab=a&0xFF;
        int br=(b>>16)&0xFF, bg=(b>>8)&0xFF, bb=b&0xFF;
        int r = (int)(ar + (br - ar) * t);
        int g = (int)(ag + (bg - ag) * t);
        int bl= (int)(ab + (bb - ab) * t);
        return (r<<16) | (g<<8) | bl;
    }

}



