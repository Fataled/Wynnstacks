package net.fataled.wynnstacks.client.HudConfig;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;

public class HudconfigScreen extends Screen {

    private final Screen parent;

    private TextFieldWidget inputbox;
    ClientPlayerEntity player = MinecraftClient.getInstance().player;

    public HudconfigScreen(Screen parent) {
        super(Text.literal("HUD Config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = this.height / 4;

        addDrawableChild(ButtonWidget.builder(Text.literal("Debuff HUD"), button ->
                MinecraftClient.getInstance().setScreen(new HudconfigScreenPage2(this))).position(centerX - 100, y).size(95, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Satsujin HUD"), button ->
                MinecraftClient.getInstance().setScreen(new HudconfigScreenPage3(this))).position(centerX, y).size(95, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Raid Counter HUD"), button ->
                MinecraftClient.getInstance().setScreen(new HudconfigScreenPage4(this))).position(centerX -50, y+ 30).size(95, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Reset"), button -> {
            HudconfigManager.resetAndSave();
            MinecraftClient.getInstance().setScreen(null);
        }).position(centerX - 100, y + 140).size(95, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), button -> {
            HudconfigManager.save();
            MinecraftClient.getInstance().setScreen(null);

            String raw = inputbox.getText();
            if(!raw.isEmpty()) {
                try {
                    int color = Integer.decode(raw.startsWith("#") ? "0x" + raw.substring(1) : raw);
                    HudConfig.INSTANCE.color = color & 0xFFFFFF; // mask out any accidental alpha
                } catch (NumberFormatException e) {
                    player.sendMessage(Text.literal("Invalid color! Use RRGGBB or 0xRRGGBB."), false);
                }
            }
        }).position(centerX + 5, y + 140).size(95, 20).build());

        inputbox = new TextFieldWidget(
                this.textRenderer,
                centerX - 100,    // x
                y + 100,         // y
                200,              // width
                20,              // height
                Text.literal("enter color")
        );
        inputbox.setMaxLength(8);
        inputbox.setEditableColor(0xFFFFFF);
        inputbox.setUneditableColor(0xAAAAAA);
        this.addDrawableChild(inputbox);

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
}
