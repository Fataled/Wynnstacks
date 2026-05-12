package net.fataled.wynnstacks.client.config.screen;

import net.fataled.wynnstacks.client.config.HudConfig;
import net.fataled.wynnstacks.client.config.HudConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import static net.fataled.wynnstacks.client.rendering.PuaStyler.stylePUAOnly;

public class HudConfigScreen extends Screen {

    private enum Category {
        GENERAL("General"),
        DEBUFF("Debuff HUD"),
        SATSUJIN("Satsujin HUD"),
        SYMBOLS("Symbols"),
        COLORS("Colors");

        final String label;

        Category(String label) {
            this.label = label;
        }
    }

    private static final int SIDEBAR_W = 110;
    private static final int HEADER_H = 28;
    private static final int FOOTER_H = 30;
    private static final int PAD = 8;
    private static final int ROW_H = 20;
    private static final int GAP = 4;

    private static final int BG_OVERLAY = 0xC8000000;
    private static final int PANEL_FILL = 0xFF1A1A1F;
    private static final int PANEL_BORDER = 0xFF3A3A45;
    private static final int CAT_TITLE_RGB = 0xFFAAAAAA;
    private static final int TITLE_RGB = 0xFFFFFFFF;

    // Reverse lookup so the Colors tab can show the PUA glyph next to each named
    // profile.
    private static final Map<String, Integer> PROFILE_CODEPOINTS = Map.ofEntries(
            Map.entry(HudConfig.MARKED, 0x271C),
            Map.entry(HudConfig.DISCOMBOBULATE, 0x2699),
            Map.entry(HudConfig.POISON, 0x2620),
            Map.entry(HudConfig.TRICKS, 0xE03A),
            Map.entry(HudConfig.DRAINED, 0xE03F),
            Map.entry(HudConfig.ENKINDLED, 0xE03D),
            Map.entry(HudConfig.CONFUSION, 0xE03C),
            Map.entry(HudConfig.CONTAMINATION, 0xE043),
            Map.entry(HudConfig.WEAKENED, 0x2694));

    private final Screen parent;
    private Category current = Category.GENERAL;
    private final List<ButtonWidget> tabButtons = new ArrayList<>();

    public HudConfigScreen(Screen parent) {
        super(Text.literal("WynnStacks Config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        tabButtons.clear();

        int y = HEADER_H + PAD;
        for (Category cat : Category.values()) {
            final Category captured = cat;
            ButtonWidget tab = ButtonWidget.builder(Text.literal(cat.label), b -> selectCategory(captured))
                    .position(PAD, y)
                    .size(SIDEBAR_W - PAD * 2, ROW_H)
                    .build();
            tabButtons.add(tab);
            addDrawableChild(tab);
            y += ROW_H + GAP;
        }
        refreshTabLabels();

        int footerY = this.height - FOOTER_H + (FOOTER_H - ROW_H) / 2;
        addDrawableChild(ButtonWidget.builder(Text.literal("Reset"), b -> {
            HudConfigManager.resetAndSave();
            this.clearAndInit();
        }).position(PAD, footerY).size(60, ROW_H).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> {
            HudConfigManager.save();
            this.client.setScreen(parent);
        }).position(this.width - PAD - 60, footerY).size(60, ROW_H).build());

        switch (current) {
            case GENERAL -> populateGeneral();
            case DEBUFF -> populateDebuff();
            case SATSUJIN -> populateSatsujin();
            case SYMBOLS -> populateSymbols();
            case COLORS -> populateColors();
        }
    }

    private void selectCategory(Category cat) {
        current = cat;
        this.clearAndInit();
    }

    private void refreshTabLabels() {
        Category[] cats = Category.values();
        for (int i = 0; i < tabButtons.size(); i++) {
            String prefix = (cats[i] == current) ? "▸ " : "  ";
            tabButtons.get(i).setMessage(Text.literal(prefix + cats[i].label));
        }
    }

    private int contentX() {
        return SIDEBAR_W + PAD;
    }

    private int contentY() {
        return HEADER_H + PAD + ROW_H;
    } // leave room for the category title

    private int contentWidth() {
        return this.width - SIDEBAR_W - PAD * 2;
    }

    private int contentHeight() {
        return this.height - HEADER_H - FOOTER_H - PAD * 2 - ROW_H;
    }

    /*
     * ============================================================
     * Generic widget builders
     * ============================================================
     */

    private SliderWidget intSlider(int x, int y, int w, String label,
            int initial, int min, int max,
            IntConsumer apply) {
        double v = (max == min) ? 0 : (initial - min) / (double) (max - min);
        return new SliderWidget(x, y, w, ROW_H, Text.literal(label + ": " + initial), v) {
            @Override
            protected void updateMessage() {
                int curr = (int) Math.round(min + this.value * (max - min));
                setMessage(Text.literal(label + ": " + curr));
            }

            @Override
            protected void applyValue() {
                apply.accept((int) Math.round(min + this.value * (max - min)));
            }
        };
    }

    private SliderWidget floatSlider(int x, int y, int w, String label,
            float initial, float min, float max, String fmt,
            FloatConsumer apply) {
        double v = (max == min) ? 0 : (initial - min) / (double) (max - min);
        return new SliderWidget(x, y, w, ROW_H, Text.literal(label + ": " + String.format(fmt, initial)), v) {
            @Override
            protected void updateMessage() {
                float curr = (float) (min + this.value * (max - min));
                setMessage(Text.literal(label + ": " + String.format(fmt, curr)));
            }

            @Override
            protected void applyValue() {
                float curr = (float) (min + this.value * (max - min));
                apply.accept(curr);
            }
        };
    }

    private ButtonWidget toggle(int x, int y, int w, String label,
            BooleanSupplier getter, Consumer<Boolean> apply) {
        return ButtonWidget.builder(toggleText(label, getter.getAsBoolean()), b -> {
            boolean newVal = !getter.getAsBoolean();
            apply.accept(newVal);
            b.setMessage(toggleText(label, newVal));
        }).position(x, y).size(w, ROW_H).build();
    }

    private static Text toggleText(String label, boolean on) {
        return Text.literal(label + ": " + (on ? "ON" : "OFF"));
    }

    @FunctionalInterface
    private interface FloatConsumer {
        void accept(float v);
    }

    /*
     * ============================================================
     * Category content
     * ============================================================
     */

    private void populateGeneral() {
        int x = contentX(), y = contentY();
        int w = Math.min(contentWidth(), 260);

        addDrawableChild(floatSlider(x, y, w, "Target Distance",
                HudConfig.INSTANCE.maxTargetDistance, 0f, 144f, "%.0f",
                v -> HudConfig.INSTANCE.maxTargetDistance = v));
        y += ROW_H + GAP;

        addDrawableChild(floatSlider(x, y, w, "Audio Range",
                (float) HudConfig.INSTANCE.range, 0f, 48f, "%.0f",
                v -> HudConfig.INSTANCE.range = v));
        y += ROW_H + GAP;

        addDrawableChild(intSlider(x, y, w, "Cone Angle",
                HudConfig.INSTANCE.coneAngleDeg, 0, 180,
                v -> HudConfig.INSTANCE.coneAngleDeg = v));
        y += ROW_H + GAP;

        addDrawableChild(toggle(x, y, w, "Ignore Players",
                () -> HudConfig.INSTANCE.ignorePlayers,
                v -> HudConfig.INSTANCE.ignorePlayers = v));
    }

    private void populateDebuff() {
        int x = contentX(), y = contentY();
        int w = Math.min(contentWidth(), 260);

        addDrawableChild(toggle(x, y, w, "Show HUD",
                () -> HudConfig.INSTANCE.showHud,
                v -> HudConfig.INSTANCE.showHud = v));
        y += ROW_H + GAP;

        int maxX = Math.max(1, this.width - 50);
        HudConfig.INSTANCE.x = MathHelper.clamp(HudConfig.INSTANCE.x, 0, maxX);
        addDrawableChild(intSlider(x, y, w, "Position X",
                HudConfig.INSTANCE.x, 0, maxX,
                v -> HudConfig.INSTANCE.x = v));
        y += ROW_H + GAP;

        int maxY = Math.max(1, this.height - 70);
        HudConfig.INSTANCE.y = MathHelper.clamp(HudConfig.INSTANCE.y, 0, maxY);
        addDrawableChild(intSlider(x, y, w, "Position Y",
                HudConfig.INSTANCE.y, 0, maxY,
                v -> HudConfig.INSTANCE.y = v));
        y += ROW_H + GAP;

        addDrawableChild(floatSlider(x, y, w, "Scale",
                HudConfig.INSTANCE.obtain(HudConfig.DEBUFF).scale, 0.5f, 3.0f, "%.2f",
                v -> HudConfig.INSTANCE.obtain(HudConfig.DEBUFF).scale = v));
    }

    private void populateSatsujin() {
        int x = contentX(), y = contentY();
        int w = Math.min(contentWidth(), 260);

        addDrawableChild(toggle(x, y, w, "Show HUD",
                () -> HudConfig.INSTANCE.showSatsujinHud,
                v -> HudConfig.INSTANCE.showSatsujinHud = v));
        y += ROW_H + GAP;

        int maxX = Math.max(1, this.width - 50);
        HudConfig.INSTANCE.satsujinX = MathHelper.clamp(HudConfig.INSTANCE.satsujinX, 0, maxX);
        addDrawableChild(intSlider(x, y, w, "Position X",
                HudConfig.INSTANCE.satsujinX, 0, maxX,
                v -> HudConfig.INSTANCE.satsujinX = v));
        y += ROW_H + GAP;

        int maxY = Math.max(1, this.height - 70);
        HudConfig.INSTANCE.satsujinY = MathHelper.clamp(HudConfig.INSTANCE.satsujinY, 0, maxY);
        addDrawableChild(intSlider(x, y, w, "Position Y",
                HudConfig.INSTANCE.satsujinY, 0, maxY,
                v -> HudConfig.INSTANCE.satsujinY = v));
        y += ROW_H + GAP;

        addDrawableChild(floatSlider(x, y, w, "Scale",
                HudConfig.INSTANCE.obtain(HudConfig.SATSUJIN).scale, 0.5f, 3.0f, "%.2f",
                v -> HudConfig.INSTANCE.obtain(HudConfig.SATSUJIN).scale = v));
        y += ROW_H + GAP;

        addDrawableChild(floatSlider(x, y, w, "Volume",
                HudConfig.INSTANCE.volume, 0f, 100f, "%.0f",
                v -> HudConfig.INSTANCE.volume = v));
        y += ROW_H + GAP;

        addDrawableChild(toggle(x, y, w, "Aspect Lvl 2",
                () -> HudConfig.INSTANCE.aspectLvl2,
                v -> HudConfig.INSTANCE.aspectLvl2 = v));
        y += ROW_H + GAP;

        addDrawableChild(toggle(x, y, w, "Use End Sounds",
                () -> HudConfig.INSTANCE.useEndSounds,
                v -> HudConfig.INSTANCE.useEndSounds = v));
    }

    private void populateSymbols() {
        int x = contentX(), startY = contentY();
        int colW = (contentWidth() - GAP) / 2;
        int col = 0;
        int y = startY;

        for (Map.Entry<String, Boolean> entry : HudConfig.INSTANCE.chosenSymbols.entrySet()) {
            String key = entry.getKey();
            String hex = key.startsWith("0x") ? key.substring(2) : key;
            int cp = Integer.parseInt(hex, 16);
            String icon = new String(Character.toChars(cp));

            ButtonWidget[] holder = new ButtonWidget[1];
            Runnable refresh = () -> {
                boolean on = HudConfig.INSTANCE.chosenSymbols.getOrDefault(key, false);
                String label = icon + "  (" + hex.toUpperCase(Locale.ROOT) + ")  " + (on ? "ON" : "OFF");
                holder[0].setMessage(stylePUAOnly(label));
            };
            ButtonWidget btn = ButtonWidget.builder(Text.literal("..."), b -> {
                boolean newVal = !HudConfig.INSTANCE.chosenSymbols.getOrDefault(key, false);
                HudConfig.INSTANCE.chosenSymbols.put(key, newVal);
                HudConfig.bumpSymbolVersion();
                refresh.run();
            }).position(x + col * (colW + GAP), y).size(colW, ROW_H).build();
            holder[0] = btn;
            refresh.run();
            addDrawableChild(btn);

            col++;
            if (col >= 2) {
                col = 0;
                y += ROW_H + GAP;
            }
        }
    }

    private void populateColors() {
        int x = contentX(), y = contentY();
        int w = contentWidth();
        int h = contentHeight();

        ProfileListWidget list = new ProfileListWidget(this.client, w, h, y, 26);
        list.setX(x);
        for (Map.Entry<String, HudConfig.Profile> e : HudConfig.INSTANCE.profiles.entrySet()) {
            list.addProfileEntry(new ProfileEntry(e.getKey(), e.getValue(), this.textRenderer));
        }
        addDrawableChild(list);
    }

    /*
     * ============================================================
     * Render: header, sidebar panel, category title
     * ============================================================
     */

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, BG_OVERLAY);

        // Sidebar panel + borders
        ctx.fill(0, HEADER_H, SIDEBAR_W, this.height - FOOTER_H, PANEL_FILL);
        ctx.drawHorizontalLine(0, this.width - 1, HEADER_H - 1, PANEL_BORDER);
        ctx.drawHorizontalLine(0, this.width - 1, this.height - FOOTER_H, PANEL_BORDER);
        ctx.drawVerticalLine(SIDEBAR_W, HEADER_H, this.height - FOOTER_H, PANEL_BORDER);

        super.render(ctx, mouseX, mouseY, delta);

        // Header title
        ctx.drawTextWithShadow(this.textRenderer, this.title,
                PAD, (HEADER_H - this.textRenderer.fontHeight) / 2, TITLE_RGB);

        // Current category caption in the content area
        ctx.drawTextWithShadow(this.textRenderer, Text.literal(current.label),
                contentX(), HEADER_H + PAD, CAT_TITLE_RGB);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        HudConfigManager.save();
        this.client.setScreen(parent);
    }

    /*
     * ============================================================
     * Colors tab: scrollable list of per-profile rows
     * ============================================================
     */

    private static class ProfileListWidget extends ElementListWidget<ProfileEntry> {
        ProfileListWidget(MinecraftClient client, int width, int height, int top, int itemHeight) {
            super(client, width, height, top, itemHeight);
        }

        // Expose the protected addEntry so the screen can populate the list.
        public void addProfileEntry(ProfileEntry e) {
            this.addEntry(e);
        }

        @Override
        protected int getScrollbarX() {
            return getX() + getWidth() - 6;
        }
    }

    private static class ProfileEntry extends ElementListWidget.Entry<ProfileEntry> {
        private final String name;
        private final HudConfig.Profile profile;
        private final TextFieldWidget solid;
        private final TextFieldWidget outline;
        private final TextFieldWidget thickness;
        private final List<Element> children;

        ProfileEntry(String name, HudConfig.Profile profile, net.minecraft.client.font.TextRenderer tr) {
            this.name = name;
            this.profile = profile;
            this.solid = makeField(tr, hex6(profile.solidRgb), 8);
            this.outline = makeField(tr, hex6(profile.outlineRgb), 8);
            this.thickness = makeField(tr, ftoa(profile.outlineThicknessPx), 6);

            this.solid.setChangedListener(t -> {
                Integer v = parseHex(t);
                if (v != null)
                    profile.solidRgb = v;
            });
            this.outline.setChangedListener(t -> {
                Integer v = parseHex(t);
                if (v != null)
                    profile.outlineRgb = v;
            });
            this.thickness.setChangedListener(t -> {
                try {
                    profile.outlineThicknessPx = Math.max(0f, Float.parseFloat(t));
                } catch (NumberFormatException ignored) {
                }
            });

            this.children = List.of(solid, outline, thickness);
        }

        private static TextFieldWidget makeField(net.minecraft.client.font.TextRenderer tr, String initial,
                int maxLen) {
            TextFieldWidget tf = new TextFieldWidget(tr, 0, 0, 56, 18, Text.empty());
            tf.setMaxLength(maxLen);
            tf.setText(initial);
            return tf;
        }

        private static String hex6(int rgb) {
            return String.format("%06X", rgb & 0xFFFFFF);
        }

        private static String ftoa(float v) {
            return (v % 1f == 0f) ? Integer.toString((int) v) : String.format("%.1f", v);
        }

        private static Integer parseHex(String raw) {
            String s = raw.trim();
            if (s.startsWith("#"))
                s = s.substring(1);
            else if (s.startsWith("0x") || s.startsWith("0X"))
                s = s.substring(2);
            if (!s.matches("(?i)^[0-9a-f]{6}$"))
                return null;
            try {
                return Integer.parseInt(s, 16) & 0xFFFFFF;
            } catch (NumberFormatException e) {
                return null;
            }
        }

        @Override
        public void render(DrawContext ctx, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            var tr = MinecraftClient.getInstance().textRenderer;

            int x = getContentX();
            int y = getContentY();
            int entryHeight = getContentHeight();

            // Glyph (if this profile maps to a stat symbol)
            Integer cp = PROFILE_CODEPOINTS.get(name);
            int textX = x + 4;
            int textY = y + (entryHeight - tr.fontHeight) / 2;
            if (cp != null) {
                Text glyph = stylePUAOnly(new String(Character.toChars(cp)));
                ctx.drawTextWithShadow(tr, glyph, textX, textY, 0xFFFFFFFF);
                textX += 14;
            }
            ctx.drawTextWithShadow(tr, name, textX, textY, 0xFFEEEEEE);

            // Live swatch reflecting profile.solidRgb
            int swatchX = x + 100;
            int swatchY = y + (entryHeight - 18) / 2;
            ctx.fill(swatchX, swatchY, swatchX + 18, swatchY + 18, 0xFF000000 | (profile.solidRgb & 0xFFFFFF));
            // Border around swatch
            int swatchEdge = 0xFF555555;
            ctx.fill(swatchX - 1, swatchY - 1, swatchX + 19, swatchY, swatchEdge); // top
            ctx.fill(swatchX - 1, swatchY + 18, swatchX + 19, swatchY + 19, swatchEdge); // bottom
            ctx.fill(swatchX - 1, swatchY, swatchX, swatchY + 18, swatchEdge); // left
            ctx.fill(swatchX + 18, swatchY, swatchX + 19, swatchY + 18, swatchEdge); // right

            // Position + render text fields
            int fieldsX = swatchX + 26;
            int fieldsY = y + (entryHeight - 18) / 2;
            solid.setX(fieldsX);
            solid.setY(fieldsY);
            solid.setWidth(56);
            outline.setX(fieldsX + 62);
            outline.setY(fieldsY);
            outline.setWidth(56);
            thickness.setX(fieldsX + 124);
            thickness.setY(fieldsY);
            thickness.setWidth(36);
            solid.render(ctx, mouseX, mouseY, tickDelta);
            outline.render(ctx, mouseX, mouseY, tickDelta);
            thickness.render(ctx, mouseX, mouseY, tickDelta);
        }

        @Override
        public List<? extends Element> children() {
            return children;
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return List.of();
        }
    }
}
