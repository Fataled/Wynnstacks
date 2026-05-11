package net.fataled.wynnstacks.client.HudConfig;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;
// TODO MAKE THIS SCREEN NOT LOOK LIKE SH...
public class HudconfigScreenPageColorSettings extends Screen {
    private final Screen parent;

    // Layout
    private static final int FIELD_W = 150, FIELD_H = 20;
    private static final int MAX_LEN = 10; // allow 0xAARRGGBB too
    private static final int TITLE_Y = 20;
    private static final int COL_Y_TOP = 100;
    private static final int ROW_STEP = 40;
    private static final int GROUP_LABEL_Y = 70;

    private static String hex6(int rgb) { return String.format("#%06X", rgb & 0xFFFFFF); }
    private static String ftoa(float v) {
        return (v % 1f == 0f) ? Integer.toString((int)v) : Float.toString(v);
    }

    // Widgets
    private TextFieldWidget DebuffSolidColor, DebuffOutlineRGB, DebuffOutlinePixels;
    private TextFieldWidget SatsujinOutlineRGB, SatsujinOutlinePixels, SatsujinSolidColor;


    public HudconfigScreenPageColorSettings(Screen parent) {
        super(Text.literal("HUD Config -  Raid Counter Color Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        // ensure disk -> memory sync (if not already done at startup)
        final int centerX = this.width / 2;
        final int colDebuffX = centerX - 100;
        final int colSatsujinX = centerX - 300;

        // DEBUFF
        DebuffSolidColor    = tf(colDebuffX, rowY(0), "Solid", "enter color");
        DebuffOutlineRGB    = tf(colDebuffX, rowY(3), "Outline Color", "enter color");
        DebuffOutlinePixels = tf(colDebuffX, rowY(4), "Outline Pixels", "enter a number");

        // SATSUJIN
        SatsujinSolidColor    = tf(colSatsujinX, rowY(0), "Solid", "enter color");
        SatsujinOutlineRGB    = tf(colSatsujinX, rowY(3), "Outline Color", "enter color");
        SatsujinOutlinePixels = tf(colSatsujinX, rowY(4), "Outline Pixels", "enter a number");

        // Back / Save
        addDrawableChild(ButtonWidget.builder(Text.literal("Back"), b -> {
            onSaveColor();
            MinecraftClient.getInstance().setScreen(parent);
        }).position(centerX - 100, this.height / 4 + 220).size(200, 20).build());

        populateFieldsFromConfig();
    }

    @Override public boolean shouldPause() { return false; }

    @Override
    public void close() {
        onSaveColor();
        super.close();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // draw a simple dark overlay instead of the blur background
        context.fill(0, 0, this.width, this.height, 0xA0000000);

        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                this.title,
                this.width / 2,
                20,
                0xFFFFFF
        );

        context.drawCenteredTextWithShadow(this.textRenderer, "Debuff HUD Colors",    (this.width / 2) -  20, GROUP_LABEL_Y, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, "Satsujin HUD Colors",  (this.width / 2) - 220, GROUP_LABEL_Y, 0xFFFFFF);
    }

    /* =========================
       Save / Validation
       ========================= */

    private void onSaveColor() {
        final var d = HudConfig.INSTANCE.obtain(HudConfig.DEBUFF);
        final var s = HudConfig.INSTANCE.obtain(HudConfig.SATSUJIN);

        // DEBUFF
        applyColor(DebuffSolidColor,    v -> d.solidRgb           = v, "Debuff Solid");
        applyColor(DebuffOutlineRGB,    v -> d.outlineRgb         = v, "Debuff Outline");
        applyFloat(DebuffOutlinePixels, v -> d.outlineThicknessPx = v, "Debuff Outline Pixels");

        // SATSUJIN
        applyColor(SatsujinSolidColor,    v -> s.solidRgb           = v, "Satsujin Solid");
        applyColor(SatsujinOutlineRGB,    v -> s.outlineRgb         = v, "Satsujin Outline");
        applyFloat(SatsujinOutlinePixels, v -> s.outlineThicknessPx = v, "Satsujin Outline Pixels");

        HudconfigManager.save();
    }

    private void applyColor(TextFieldWidget field, Consumer<Integer> setter, String label) {
        String raw = safe(field);
        if (raw.isEmpty()) return;
        Integer rgb = parseArgb(raw);
        if (rgb == null) {
            msg(label + ": Please use RRGGBB / #RRGGBB / 0xRRGGBB (or #RGB).");
        } else {
            setter.accept(rgb);
        }
    }

    private void applyFloat(TextFieldWidget field, Consumer<Float> setter, String label) {
        String raw = safe(field);
        if (raw.isEmpty()) return;
        try {
            setter.accept(Float.parseFloat(raw));
        } catch (NumberFormatException e) {
            msg(label + ": Enter a number (e.g., 1 or 1.5).");
        }
    }

    private void msg(String s) {
        var p = MinecraftClient.getInstance().player;
        if (p != null) p.sendMessage(Text.literal(s), false);
    }

    /* =========================
       Swatch drawing
       ========================= */


    // Accepts: RRGGBB, #RRGGBB, 0xRRGGBB, AARRGGBB, #RGB
    private static Integer parseArgb(String raw) {
        if (raw == null) return null;

        String s = raw.trim();
        if (s.isEmpty()) return null;

        if (s.startsWith("#")) s = s.substring(1);
        else if (s.startsWith("0x") || s.startsWith("0X")) s = s.substring(2);

        // #RGB shorthand → expand to RRGGBB
        if (s.length() == 3 && s.matches("(?i)^[0-9a-f]{3}$")) {
            s = "" + s.charAt(0) + s.charAt(0)
                    + s.charAt(1) + s.charAt(1)
                    + s.charAt(2) + s.charAt(2);
        }

        // Must now be 6 or 8 hex digits
        if (!s.matches("(?i)^[0-9a-f]{6}([0-9a-f]{2})?$")) return null;

        try {
            long v = Long.parseUnsignedLong(s, 16);

            if (s.length() == 6) {
                // RGB → add full alpha
                return (int)(0xFF000000L | v);
            } else {
                // Already AARRGGBB
                return (int)v;
            }

        } catch (NumberFormatException e) {
            return null;
        }
    }


    private TextFieldWidget tf(int x, int y, String placeholder, String narration) {
        TextFieldWidget tf = new TextFieldWidget(this.textRenderer, x, y, FIELD_W, FIELD_H, Text.literal(narration));
        tf.setPlaceholder(Text.literal(placeholder));
        tf.setMaxLength(MAX_LEN);
        return addDrawableChild(tf);
    }

    private static int rowY(int rowIdx) { return COL_Y_TOP + rowIdx * ROW_STEP; }

    private static String safe(TextFieldWidget tf) {
        String t = tf.getText();
        return t == null ? "" : t.trim();
    }

    private void populateFieldsFromConfig() {
        HudConfig.Profile d = HudConfig.INSTANCE.obtain(HudConfig.DEBUFF);
        HudConfig.Profile s = HudConfig.INSTANCE.obtain(HudConfig.SATSUJIN);


        // DEBUFF
        if (DebuffSolidColor    != null) DebuffSolidColor.setText(hex6(d.solidRgb));
        if (DebuffOutlineRGB    != null) DebuffOutlineRGB.setText(hex6(d.outlineRgb));
        if (DebuffOutlinePixels != null) DebuffOutlinePixels.setText(ftoa(d.outlineThicknessPx));

        // SATSUJIN
        if (SatsujinSolidColor    != null) SatsujinSolidColor.setText(hex6(s.solidRgb));
        if (SatsujinOutlineRGB    != null) SatsujinOutlineRGB.setText(hex6(s.outlineRgb));
        if (SatsujinOutlinePixels != null) SatsujinOutlinePixels.setText(ftoa(s.outlineThicknessPx));
    }

}
