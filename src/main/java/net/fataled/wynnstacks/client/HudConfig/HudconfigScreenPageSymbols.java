package net.fataled.wynnstacks.client.HudConfig;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Map;

public class HudconfigScreenPageSymbols extends Screen {
    private final Screen parent;

    public HudconfigScreenPageSymbols(Screen parent) {
        super(Text.literal("HUD Config - Hud Symbols"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = this.height / 4;
        int gapY = 24;      // vertical spacing
        int gapX = 210;     // horizontal spacing between columns
        int cols  = 2;      // number of columns

        // Back button
        addDrawableChild(
                ButtonWidget.builder(Text.literal("Back"), b ->
                        MinecraftClient.getInstance().setScreen(new HudconfigScreenPage2(this.parent))
                ).position(centerX - 100, startY + 140).size(200, 20).build()
        );

        int index = 0;
        for (Map.Entry<String, Boolean> entry : HudConfig.INSTANCE.chosenSymbols.entrySet()) {
            // parse hex key -> icon string
            String key = entry.getKey();
            String hex = key.startsWith("0x") ? key.substring(2) : key;
            int codePoint = Integer.parseInt(hex, 16);
            String icon = new String(Character.toChars(codePoint));

            // figure out grid position
            int col = index % cols; // which column we’re in
            int row = index / cols; // which row we’re in

            int xPos = (centerX - (gapX / 2)) + (col * gapX);
            int yPos = startY + (row * gapY);

            // set up button & label
            ButtonWidget[] holder = new ButtonWidget[1];
            Runnable refreshLabel = () -> {
                boolean enabled = HudConfig.INSTANCE.chosenSymbols.getOrDefault(key, false);
                String label = icon + " (" + hex.toUpperCase() + ") [" + (enabled ? "ON" : "OFF") + "]";
                Text cleaned = stylePUAOnly(label);
                holder[0].setMessage(cleaned);
            };

            ButtonWidget btn = ButtonWidget.builder(Text.literal("..."), b -> {
                boolean newVal = !HudConfig.INSTANCE.chosenSymbols.getOrDefault(key, false);
                HudConfig.INSTANCE.chosenSymbols.put(key, newVal);
                refreshLabel.run();
            }).position(xPos - 100, yPos).size(200, 20).build();

            holder[0] = btn;
            refreshLabel.run();
            addDrawableChild(btn);

            index++; // move to the next grid slot
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }

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
}

