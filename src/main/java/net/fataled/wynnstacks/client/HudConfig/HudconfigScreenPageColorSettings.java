package net.fataled.wynnstacks.client.HudConfig;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class HudconfigScreenPageColorSettings extends Screen {
    private final Screen parent;

    // Layout
    private static final int FIELD_W = 150, FIELD_H = 20;
    private static final int MAX_LEN = 10; // allow 0xAARRGGBB too
    private static final int TITLE_Y = 20;
    private static final int COL_Y_TOP = 100;
    private static final int ROW_STEP = 40;
    private static final int GROUP_LABEL_Y = 70;

    // Swatch layout (inside the text box on the right)
    private static final int SWATCH_SIZE = 14;
    private static final int SWATCH_PAD = 3; // padding from box edge

    private static String hex6(int rgb) { return String.format("#%06X", rgb & 0xFFFFFF); }
    private static String ftoa(float v) {
        return (v % 1f == 0f) ? Integer.toString((int)v) : Float.toString(v);
    }

    // Widgets
    private TextFieldWidget RaidStartGrad, RaidEndGrad, RaidSolidColor, RaidOutlineRGB, RaidOutlinePixels;
    private TextFieldWidget DebuffSolidColor, DebuffStartGrad, DebuffEndGrad, DebuffOutlineRGB, DebuffOutlinePixels;
    private TextFieldWidget SatsujinStartGrad, SatsujinEndGrad, SatsujinOutlineRGB, SatsujinOutlinePixels, SatsujinSolidColor;

    private ButtonWidget raidToggleBtn, debuffToggleBtn, satsujinToggleBtn;

    public HudconfigScreenPageColorSettings(Screen parent) {
        super(Text.literal("HUD Config -  Raid Counter Color Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        // ensure disk -> memory sync (if not already done at startup)
        HudconfigManager.load(); // <-- add this if your mod doesn't autoload earlier

        final var r = HudConfig.INSTANCE.obtain(HudConfig.RAIDCOUNTER);
        final var d = HudConfig.INSTANCE.obtain(HudConfig.DEBUFF);
        final var s = HudConfig.INSTANCE.obtain(HudConfig.SATSUJIN);

        final int centerX = this.width / 2;
        final int colRaidX = centerX + 100;
        final int colDebuffX = centerX - 100;
        final int colSatsujinX = centerX - 300;

        // RAID
        RaidSolidColor    = tf(colRaidX, rowY(0), "Solid", "enter color");
        RaidStartGrad     = tf(colRaidX, rowY(1), "Gradient Start Color", "enter color");
        RaidEndGrad       = tf(colRaidX, rowY(2), "Gradient End Color", "enter color");
        RaidOutlineRGB    = tf(colRaidX, rowY(3), "Outline Color", "enter color");
        RaidOutlinePixels = tf(colRaidX, rowY(4), "Outline Pixels", "enter a number");

        raidToggleBtn = ButtonWidget.builder(Text.literal("Use Gradient (Raid): " + r.useGradient), b -> {
            r.useGradient = !r.useGradient;
            b.setMessage(Text.literal("Use Gradient (Raid): " + r.useGradient));
        }).position(centerX + 130, this.height / 4 + 160).size(95, 20).build();
        addDrawableChild(raidToggleBtn);

        // DEBUFF
        DebuffSolidColor    = tf(colDebuffX, rowY(0), "Solid", "enter color");
        DebuffStartGrad     = tf(colDebuffX, rowY(1), "Gradient Start Color", "enter color");
        DebuffEndGrad       = tf(colDebuffX, rowY(2), "Gradient End Color", "enter color");
        DebuffOutlineRGB    = tf(colDebuffX, rowY(3), "Outline Color", "enter color");
        DebuffOutlinePixels = tf(colDebuffX, rowY(4), "Outline Pixels", "enter a number");

        debuffToggleBtn = ButtonWidget.builder(Text.literal("Use Gradient (Debuff): " + d.useGradient), b -> {
            d.useGradient = !d.useGradient;
            b.setMessage(Text.literal("Use Gradient (Debuff): " + d.useGradient));
        }).position(centerX - 80, this.height / 4 + 160).size(95, 20).build();
        addDrawableChild(debuffToggleBtn);

        // SATSUJIN
        SatsujinSolidColor    = tf(colSatsujinX, rowY(0), "Solid", "enter color");
        SatsujinStartGrad     = tf(colSatsujinX, rowY(1), "Gradient Start Color", "enter color");
        SatsujinEndGrad       = tf(colSatsujinX, rowY(2), "Gradient End Color", "enter color");
        SatsujinOutlineRGB    = tf(colSatsujinX, rowY(3), "Outline Color", "enter color");
        SatsujinOutlinePixels = tf(colSatsujinX, rowY(4), "Outline Pixels", "enter a number");

        satsujinToggleBtn = ButtonWidget.builder(Text.literal("Use Gradient (Satsujin): " + s.useGradient), b -> {
            s.useGradient = !s.useGradient;
            b.setMessage(Text.literal("Use Gradient (Satsujin): " + s.useGradient));
        }).position(centerX - 270, this.height / 4 + 160).size(95, 20).build();
        addDrawableChild(satsujinToggleBtn);

        // Back / Save
        addDrawableChild(ButtonWidget.builder(Text.literal("Back"), b -> {
            onSaveColor();
            MinecraftClient.getInstance().setScreen(parent);
        }).position(centerX - 100, this.height / 4 + 220).size(200, 20).build());

        populateFieldsFromConfig(r, d, s);
    }

    @Override public boolean shouldPause() { return false; }

    @Override
    public void close() {
        onSaveColor();
        super.close();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // ENTER (main/kp) -> Save & back
        if (keyCode == 257 || keyCode == 335) {
            onSaveColor();
            MinecraftClient.getInstance().setScreen(parent);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        this.renderBackground(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, TITLE_Y, 0xFFFFFF);
        ctx.drawCenteredTextWithShadow(this.textRenderer, "Raid Counter Colors",  (this.width / 2) + 170, GROUP_LABEL_Y, 0xFFFFFF);
        ctx.drawCenteredTextWithShadow(this.textRenderer, "Debuff HUD Colors",    (this.width / 2) -  20, GROUP_LABEL_Y, 0xFFFFFF);
        ctx.drawCenteredTextWithShadow(this.textRenderer, "Satsujin HUD Colors",  (this.width / 2) - 220, GROUP_LABEL_Y, 0xFFFFFF);

        // Let widgets draw themselves first
        super.render(ctx, mouseX, mouseY, delta);

        // Then overlay color swatches INSIDE the text boxes (right side)
        drawColorSwatch(ctx, RaidSolidColor);
        drawColorSwatch(ctx, RaidStartGrad);
        drawColorSwatch(ctx, RaidEndGrad);
        drawColorSwatch(ctx, RaidOutlineRGB);

        drawColorSwatch(ctx, DebuffSolidColor);
        drawColorSwatch(ctx, DebuffStartGrad);
        drawColorSwatch(ctx, DebuffEndGrad);
        drawColorSwatch(ctx, DebuffOutlineRGB);

        drawColorSwatch(ctx, SatsujinSolidColor);
        drawColorSwatch(ctx, SatsujinStartGrad);
        drawColorSwatch(ctx, SatsujinEndGrad);
        drawColorSwatch(ctx, SatsujinOutlineRGB);
    }

    /* =========================
       Save / Validation
       ========================= */

    private void onSaveColor() {
        final var r = HudConfig.INSTANCE.obtain(HudConfig.RAIDCOUNTER);
        final var d = HudConfig.INSTANCE.obtain(HudConfig.DEBUFF);
        final var s = HudConfig.INSTANCE.obtain(HudConfig.SATSUJIN);

        // RAID
        applyColor(RaidSolidColor,    v -> r.solidRgb           = v, "Raid Solid");
        applyColor(RaidStartGrad,     v -> r.gradientStartRgb   = v, "Raid Gradient Start");
        applyColor(RaidEndGrad,       v -> r.gradientEndRgb     = v, "Raid Gradient End");
        applyColor(RaidOutlineRGB,    v -> r.outlineRgb         = v, "Raid Outline");
        applyFloat(RaidOutlinePixels, v -> r.outlineThicknessPx = v, "Raid Outline Pixels");

        // DEBUFF
        applyColor(DebuffSolidColor,    v -> d.solidRgb           = v, "Debuff Solid");
        applyColor(DebuffStartGrad,     v -> d.gradientStartRgb   = v, "Debuff Gradient Start");
        applyColor(DebuffEndGrad,       v -> d.gradientEndRgb     = v, "Debuff Gradient End");
        applyColor(DebuffOutlineRGB,    v -> d.outlineRgb         = v, "Debuff Outline");
        applyFloat(DebuffOutlinePixels, v -> d.outlineThicknessPx = v, "Debuff Outline Pixels");

        // SATSUJIN
        applyColor(SatsujinSolidColor,    v -> s.solidRgb           = v, "Satsujin Solid");
        applyColor(SatsujinStartGrad,     v -> s.gradientStartRgb   = v, "Satsujin Gradient Start");
        applyColor(SatsujinEndGrad,       v -> s.gradientEndRgb     = v, "Satsujin Gradient End");
        applyColor(SatsujinOutlineRGB,    v -> s.outlineRgb         = v, "Satsujin Outline");
        applyFloat(SatsujinOutlinePixels, v -> s.outlineThicknessPx = v, "Satsujin Outline Pixels");

        HudconfigManager.save();
    }

    private void applyColor(TextFieldWidget field, Consumer<Integer> setter, String label) {
        String raw = safe(field);
        if (raw.isEmpty()) return;
        Integer rgb = parseRgb24(raw);
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

    private void drawColorSwatch(DrawContext ctx, TextFieldWidget tf) {
        if (tf == null || !tf.isVisible()) return;

        // Parse every frame for live feedback (cheap)
        Integer rgb = parseRgb24(safe(tf));

        if (rgb == null || safe(tf).isEmpty()) {
            final var r = HudConfig.INSTANCE.obtain(HudConfig.RAIDCOUNTER);
            final var d = HudConfig.INSTANCE.obtain(HudConfig.DEBUFF);
            final var s = HudConfig.INSTANCE.obtain(HudConfig.SATSUJIN);

            if (tf == RaidSolidColor   ) rgb = r.solidRgb;
            else if (tf == RaidStartGrad) rgb = r.gradientStartRgb;
            else if (tf == RaidEndGrad  ) rgb = r.gradientEndRgb;
            else if (tf == RaidOutlineRGB) rgb = r.outlineRgb;

            else if (tf == DebuffSolidColor   ) rgb = d.solidRgb;
            else if (tf == DebuffStartGrad    ) rgb = d.gradientStartRgb;
            else if (tf == DebuffEndGrad      ) rgb = d.gradientEndRgb;
            else if (tf == DebuffOutlineRGB   ) rgb = d.outlineRgb;

            else if (tf == SatsujinSolidColor ) rgb = s.solidRgb;
            else if (tf == SatsujinStartGrad  ) rgb = s.gradientStartRgb;
            else if (tf == SatsujinEndGrad    ) rgb = s.gradientEndRgb;
            else if (tf == SatsujinOutlineRGB ) rgb = s.outlineRgb;
        }
        // Where to draw: inside the field’s right edge
        int bx = tf.getX();
        int by = tf.getY();
        int bw = tf.getWidth();
        int bh = tf.getHeight();

        int swX2 = bx + bw - SWATCH_PAD;                 // right inner edge
        int swY1 = by + (bh - SWATCH_SIZE) / 2;          // vertically centered
        int swX1 = swX2 - SWATCH_SIZE;
        int swY2 = swY1 + SWATCH_SIZE;

        // Background bezel (so it stands out over text)
        // Outer border (dark)
        ctx.fill(swX1 - 1, swY1 - 1, swX2 + 1, swY2 + 1, 0xFF000000);
        // Inner border (light)
        ctx.fill(swX1, swY1, swX2, swY2, 0xFFFFFFFF);

        // Fill: parsed color or gray if invalid
        int fill = (rgb != null) ? (0xFF000000 | rgb) : 0xFF7F7F7F;
        ctx.fill(swX1 + 1, swY1 + 1, swX2 - 1, swY2 - 1, fill);
    }

    /* =========================
       Parsing & helpers
       ========================= */

    // Accepts: RRGGBB, #RRGGBB, 0xRRGGBB, AARRGGBB, #RGB
    private static Integer parseRgb24(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        if (s.isEmpty()) return null;

        if (s.startsWith("#")) s = s.substring(1);
        else if (s.startsWith("0x") || s.startsWith("0X")) s = s.substring(2);

        // #RGB shorthand -> expand to RRGGBB
        if (s.length() == 3 && s.matches("(?i)^[0-9a-f]{3}$")) {
            s = new StringBuilder(6)
                    .append(s.charAt(0)).append(s.charAt(0))
                    .append(s.charAt(1)).append(s.charAt(1))
                    .append(s.charAt(2)).append(s.charAt(2))
                    .toString();
        }

        // Accept 6 (RRGGBB) or 8 (AARRGGBB)
        if (!s.matches("(?i)^[0-9a-f]{6}([0-9a-f]{2})?$")) return null;

        try {
            long v = Long.parseUnsignedLong(s, 16);
            return (int)(v & 0xFFFFFF); // ignore alpha if present
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

    private void populateFieldsFromConfig(HudConfig.Profile r, HudConfig.Profile d, HudConfig.Profile s) {
        r = HudConfig.INSTANCE.obtain(HudConfig.RAIDCOUNTER);
        d = HudConfig.INSTANCE.obtain(HudConfig.DEBUFF);
        s = HudConfig.INSTANCE.obtain(HudConfig.SATSUJIN);

        // RAID
        if (RaidSolidColor    != null) RaidSolidColor.setText(hex6(r.solidRgb));
        if (RaidStartGrad     != null) RaidStartGrad.setText(hex6(r.gradientStartRgb));
        if (RaidEndGrad       != null) RaidEndGrad.setText(hex6(r.gradientEndRgb));
        if (RaidOutlineRGB    != null) RaidOutlineRGB.setText(hex6(r.outlineRgb));
        if (RaidOutlinePixels != null) RaidOutlinePixels.setText(ftoa(r.outlineThicknessPx));

        // DEBUFF
        if (DebuffSolidColor    != null) DebuffSolidColor.setText(hex6(d.solidRgb));
        if (DebuffStartGrad     != null) DebuffStartGrad.setText(hex6(d.gradientStartRgb));
        if (DebuffEndGrad       != null) DebuffEndGrad.setText(hex6(d.gradientEndRgb));
        if (DebuffOutlineRGB    != null) DebuffOutlineRGB.setText(hex6(d.outlineRgb));
        if (DebuffOutlinePixels != null) DebuffOutlinePixels.setText(ftoa(d.outlineThicknessPx));

        // SATSUJIN
        if (SatsujinSolidColor    != null) SatsujinSolidColor.setText(hex6(s.solidRgb));
        if (SatsujinStartGrad     != null) SatsujinStartGrad.setText(hex6(s.gradientStartRgb));
        if (SatsujinEndGrad       != null) SatsujinEndGrad.setText(hex6(s.gradientEndRgb));
        if (SatsujinOutlineRGB    != null) SatsujinOutlineRGB.setText(hex6(s.outlineRgb));
        if (SatsujinOutlinePixels != null) SatsujinOutlinePixels.setText(ftoa(s.outlineThicknessPx));
    }

}
