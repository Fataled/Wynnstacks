package net.fataled.wynnstacks.client.HudConfig;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class HudconfigScreen extends Screen {



    public HudconfigScreen(Screen parent) {
        super(Text.literal("HUD Config"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = this.height / 4;

        addDrawableChild(ButtonWidget.builder(Text.literal("Debuff HUD"), button ->
                MinecraftClient.getInstance().setScreen(new HudconfigScreenPage2(this))).position(centerX - 100, y).size(95, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Satsujin HUD"), button ->
                MinecraftClient.getInstance().setScreen(new HudconfigScreenPage3(this))).position(centerX, y).size(95, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Reset"), button -> {
            HudconfigManager.resetAndSave();
            MinecraftClient.getInstance().setScreen(null);
        }).position(centerX - 100, y + 140).size(95, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), button -> {
            HudconfigManager.save();
            MinecraftClient.getInstance().setScreen(null);
        }).position(centerX + 5, y + 140).size(95, 20).build());

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
                10,
                0xFFFFFFFF
        );

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("Any issues message §nfataled§r on discord or in game"),
                this.width / 2,
                this.height / 2 - 40,
                0xFFFF00FF
        );
    }
}
