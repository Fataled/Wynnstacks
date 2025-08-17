package net.fataled.wynnstacks.client.HudConfig;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public class HudconfigScreenPage3 extends Screen {
    private final Screen parent;
    float screenWidth = MinecraftClient.getInstance().getWindow().getScaledWidth();
    float screenHeight = MinecraftClient.getInstance().getWindow().getScaledHeight();

    public HudconfigScreenPage3(Screen parent) {
        super(Text.literal("HUD Config - Satsujin HUD"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = this.height / 4;

        addDrawableChild(new SliderWidget(centerX - 225, y + 30, 200, 20,
                Text.literal("Satsujin Y: " + HudConfig.INSTANCE.Saty),
                HudConfig.INSTANCE.Saty / screenHeight) {
            @Override
            protected void updateMessage() {
                setMessage(Text.literal("Satsujin Y: " + HudConfig.INSTANCE.Saty));
            }

            @Override
            protected void applyValue() {
                HudConfig.INSTANCE.Saty = (int) (this.value * screenHeight);
                updateMessage();
            }
        });

        addDrawableChild(new SliderWidget(centerX - 225, y, 200, 20,
                Text.literal("Satsujin X: " + HudConfig.INSTANCE.Satx),
                HudConfig.INSTANCE.Saty / screenHeight) {
            @Override
            protected void updateMessage() {
                setMessage(Text.literal("Satsujin X: " + HudConfig.INSTANCE.Satx));
            }

            @Override
            protected void applyValue() {
                HudConfig.INSTANCE.Satx = (int) (this.value * screenWidth);
                updateMessage();
            }
        });

        addDrawableChild(ButtonWidget.builder(Text.literal("Aspect LVL 2: " + HudConfig.INSTANCE.AspectLvl2), button -> {
            HudConfig.INSTANCE.AspectLvl2 = !HudConfig.INSTANCE.AspectLvl2;
            button.setMessage(Text.literal("Aspect LVL 2: " + HudConfig.INSTANCE.AspectLvl2));
        }).position(centerX - 100, y + 90).size(95, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Show Hud: " + HudConfig.INSTANCE.showSatsujinHud), button -> {
            HudConfig.INSTANCE.showSatsujinHud = !HudConfig.INSTANCE.showSatsujinHud;
            button.setMessage(Text.literal("Show Hud: " + HudConfig.INSTANCE.showSatsujinHud));
        }).position(centerX, y + 90).size(95, 20).build());

        addDrawableChild(new SliderWidget(centerX + 25, y + 30, 200, 20,
                Text.literal("Volume: " + String.format("%.0f",HudConfig.INSTANCE.Volume)+ "%"),
                HudConfig.INSTANCE.Volume / 100) {
            @Override
            protected void updateMessage() {
                setMessage(Text.literal("Volume: " + String.format("%.0f",HudConfig.INSTANCE.Volume)+ "%"));
            }

            @Override
            protected void applyValue() {
                HudConfig.INSTANCE.Volume = (float) (this.value * 100f);
                updateMessage();
            }
        });

        addDrawableChild(ButtonWidget.builder(Text.literal("Back"), b ->
                MinecraftClient.getInstance().setScreen(parent)).position(centerX - 100, y + 140).size(200, 20).build());


        addDrawableChild(new SliderWidget(centerX + 25 , y, 200, 20,
                Text.literal("Scale: " + String.format("%.2f",HudConfig.INSTANCE.sScale) ),
                (HudConfig.INSTANCE.sScale - 1f) / 2.0f) {
            @Override
            protected void updateMessage() {
                setMessage(Text.literal("Scale: " + String.format("%.2f", HudConfig.INSTANCE.sScale)));
            }

            @Override
            protected void applyValue() {
                HudConfig.INSTANCE.sScale = (float) (1 + this.value * 2.0); // 0.5 to 2.5
                updateMessage();
            }
        });

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

