package net.fataled.wynnstacks.client.debug;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class TestScreen extends Screen {

    public TestScreen() {
        super(Text.literal("TEST SCREEN"));
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int y = this.height / 2;

        this.addDrawableChild(
                ButtonWidget.builder(Text.literal("Close"), button ->
                        this.client.setScreen(null)
                ).position(centerX - 50, y).size(100, 20).build()
        );
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // vanilla background + widgets
        // BIG, obvious debug fills + text
        int w = this.width;
        // blue bar at top

        context.fill(0, 0, this.width, 40, 0x800000FF);

        super.render(context, mouseX, mouseY, delta);

        context.drawTextWithShadow(
                this.textRenderer,
                Text.literal("IF YOU SEE THIS, TEXT RENDERING WORKS"),
                10,
                10,
                0xFFFFFFFF
        );

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("CENTERED TEST TEXT"),
                this.width / 2,
                this.height / 2 - 40,
                0xFFFF00FF
        );
    }

    public static void open() {
        MinecraftClient.getInstance().setScreen(new TestScreen());
    }
}