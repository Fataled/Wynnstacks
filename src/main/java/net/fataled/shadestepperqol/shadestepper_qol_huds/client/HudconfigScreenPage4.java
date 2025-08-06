package net.fataled.shadestepperqol.shadestepper_qol_huds.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public class HudconfigScreenPage4 extends Screen {
    private final Screen parent;
    int screenWidth = MinecraftClient.getInstance().getWindow().getScaledWidth();
    int screenHeight = MinecraftClient.getInstance().getWindow().getScaledHeight();

    public HudconfigScreenPage4(Screen parent) {
        super(Text.literal("HUD Config - Raid Counter HUD"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = this.height / 4;

        addDrawableChild(new SliderWidget(centerX - 225, y + 30, 200, 20,
                Text.literal("Raid Counter Y: " + HudConfig.INSTANCE.rcY),
                HudConfig.INSTANCE.rcY / screenHeight) {
            @Override
            protected void updateMessage() {
                setMessage(Text.literal("Raid Counter Y: " + HudConfig.INSTANCE.rcY));
            }

            @Override
            protected void applyValue() {
                HudConfig.INSTANCE.rcY = (int) (this.value * screenHeight);
                updateMessage();
            }
        });

        addDrawableChild(new SliderWidget(centerX -225, y, 200, 20,
                Text.literal("Raid Counter X: " + HudConfig.INSTANCE.rcX),
                HudConfig.INSTANCE.rcX / screenHeight) {
            @Override
            protected void updateMessage() {
                setMessage(Text.literal("Raid Counter X: " + HudConfig.INSTANCE.rcX));
            }

            @Override
            protected void applyValue() {
                HudConfig.INSTANCE.rcX = (int) (this.value * screenWidth);
                updateMessage();
            }
        });

        addDrawableChild(new SliderWidget(centerX + 25, y, 200, 20,
                Text.literal("Raid Counter Scale: " + String.format("%.2f", HudConfig.INSTANCE.rcS)),
                (HudConfig.INSTANCE.rcS -1f)/2f){
            @Override
            protected void updateMessage() {
                setMessage(Text.literal("Raid Counter Scale: " + String.format("%.2f", HudConfig.INSTANCE.rcS)));
            }

            @Override
            protected void applyValue() {
                HudConfig.INSTANCE.rcS = (float) (1.0 + this.value * 2.0);
                updateMessage();
            }
        });


        addDrawableChild(new SliderWidget(centerX + 25 , y+30, 200, 20,
                Text.literal("Line Gap: " + HudConfig.INSTANCE.lineGap),
                (HudConfig.INSTANCE.lineGap - 1f) / 2.0f) {
            @Override
            protected void updateMessage() {
                setMessage(Text.literal("Line Gap: " +  HudConfig.INSTANCE.lineGap));
            }

            @Override
            protected void applyValue() {
                HudConfig.INSTANCE.lineGap = (int) (1 + this.value * 20); // 0.5 to 2.5
                updateMessage();
            }
        });

        addDrawableChild(ButtonWidget.builder(Text.literal("Show Raid Counter: " + HudConfig.INSTANCE.showRaidCounter), button -> {
            HudConfig.INSTANCE.showRaidCounter = !HudConfig.INSTANCE.showRaidCounter;
            button.setMessage(Text.literal("Show Raid Counter: " + HudConfig.INSTANCE.showRaidCounter));
        }).position(centerX - 50, y + 90).size(95, 20).build());


        addDrawableChild(ButtonWidget.builder(Text.literal("Back"), b ->
                MinecraftClient.getInstance().setScreen(parent)).position(centerX - 100, y + 140).size(200, 20).build());
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

