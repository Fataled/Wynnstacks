package net.fataled.wynnstacks.client.HudConfig;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public class HudconfigScreenPage2 extends Screen {
    private final Screen parent;
    float screenWidth = MinecraftClient.getInstance().getWindow().getScaledWidth();
    float screenHeight = MinecraftClient.getInstance().getWindow().getScaledHeight();

    public HudconfigScreenPage2(Screen parent) {
        super(Text.literal("HUD Config - Debuff HUD"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = this.height / 4;

        // Add widgets for Page 2 here
        // Example:
        addDrawableChild(ButtonWidget.builder(Text.literal("Back"), b -> {
            MinecraftClient.getInstance().setScreen(parent);
        }).position(centerX - 100, y + 140).size(200, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("⚙"), b -> {
            MinecraftClient.getInstance().setScreen(new HudconfigScreenPageSymbols(this.parent));
        }).position(0, 0).size(50, 50).build());

        addDrawableChild(new SliderWidget(centerX - 225, y, 200, 20,
                Text.literal("X: " + HudConfig.INSTANCE.x),
                HudConfig.INSTANCE.x / screenWidth) {
            @Override
            protected void updateMessage() {
                setMessage(Text.literal("X: " + HudConfig.INSTANCE.x));
            }

            @Override
            protected void applyValue() {
                HudConfig.INSTANCE.x = (int) (this.value * screenWidth);
                updateMessage();
            }
        });

        addDrawableChild(new SliderWidget(centerX - 225, y + 30, 200, 20,
                Text.literal("Y: " + HudConfig.INSTANCE.y),
                HudConfig.INSTANCE.y / screenHeight) {
            @Override
            protected void updateMessage() {
                setMessage(Text.literal("Y: " + HudConfig.INSTANCE.y));
            }

            @Override
            protected void applyValue() {
                HudConfig.INSTANCE.y = (int) (this.value * screenHeight);
                updateMessage();
            }
        });

        addDrawableChild(new SliderWidget(centerX + 25 , y, 200, 20,
                Text.literal("Scale: " + String.format("%.2f",HudConfig.INSTANCE.scale) ),
                (HudConfig.INSTANCE.scale - 1f) / 2.0f) {
            @Override
            protected void updateMessage() {
                setMessage(Text.literal("Scale: " + String.format("%.2f", HudConfig.INSTANCE.scale)));
            }

            @Override
            protected void applyValue() {
                HudConfig.INSTANCE.scale = (float) (1 + this.value * 2.0); // 0.5 to 2.5
                updateMessage();
            }
        });

        addDrawableChild(new SliderWidget(centerX + 25, y + 30, 200, 20,
                Text.literal("Angle: " + HudConfig.INSTANCE.coneAngleDeg),
                HudConfig.INSTANCE.coneAngleDeg / 180) {
            @Override
            protected void updateMessage() {
                setMessage(Text.literal("Angle: " + HudConfig.INSTANCE.coneAngleDeg));
            }

            @Override
            protected void applyValue() {
                HudConfig.INSTANCE.coneAngleDeg = (int) (this.value * 180); // 0 to 120
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

