package net.fataled.wynnstacks.client.HudConfig;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class HudconfigScreenPage2 extends Screen {
    private final Screen parent;

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
        addDrawableChild(ButtonWidget.builder(Text.literal("Back"), b ->
                MinecraftClient.getInstance().setScreen(parent)).position(centerX - 100, y + 140).size(200, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("⚙"), b -> MinecraftClient.getInstance().setScreen(new HudconfigScreenPageSymbols(this.parent))).position(0, 0).size(50, 50).build());

        int ScreenWidth= this.width;
        int bottomMarginX = 50;
        int maxX = ScreenWidth - bottomMarginX;

        // make sure current y is within range
        HudConfig.INSTANCE.x = MathHelper.clamp(HudConfig.INSTANCE.x, 0, maxX);

        // 0.0–1.0 slider value
        double initialValueX = HudConfig.INSTANCE.x / (double) maxX;

        addDrawableChild(new SliderWidget(centerX - 225, y, 200, 20,
                Text.literal("X: " + HudConfig.INSTANCE.x),
                initialValueX) {

            @Override
            protected void updateMessage() {
                setMessage(Text.literal("X: " + HudConfig.INSTANCE.x));
            }

            @Override
            protected void applyValue() {
                HudConfig.INSTANCE.x = (int) (this.value * maxX);
                updateMessage();
            }
        });

        int screenHeight = this.height;
        int bottomMarginY = 70;
        int maxY = screenHeight - bottomMarginY;

        // make sure current y is within range
        HudConfig.INSTANCE.y = MathHelper.clamp(HudConfig.INSTANCE.y, 0, maxY);

        // 0.0–1.0 slider value
        double initialValueY = HudConfig.INSTANCE.y / (double) maxY;

        addDrawableChild(new SliderWidget(centerX - 225, y + 30, 200, 20,
                Text.literal("Y: " + HudConfig.INSTANCE.y),
                initialValueY) {

            @Override
            protected void updateMessage() {
                setMessage(Text.literal("Y: " + HudConfig.INSTANCE.y));
            }

            @Override
            protected void applyValue() {
                HudConfig.INSTANCE.y = (int) (this.value * maxY);
                updateMessage();
            }
        });

        addDrawableChild(new SliderWidget(centerX + 25 , y, 200, 20,
                Text.literal("Scale: " + String.format("%.2f",HudConfig.INSTANCE.obtain(HudConfig.DEBUFF).scale) ),
                (HudConfig.INSTANCE.obtain(HudConfig.DEBUFF).scale - 1f) / 2.0f) {
            @Override
            protected void updateMessage() {
                setMessage(Text.literal("Scale: " + String.format("%.2f", HudConfig.INSTANCE.obtain(HudConfig.DEBUFF).scale)));
            }

            @Override
            protected void applyValue() {
                HudConfig.INSTANCE.obtain(HudConfig.DEBUFF).scale = (float) (1+this.value *2.0); // 0.5 to 2.5
                updateMessage();
            }
        });

        addDrawableChild(new SliderWidget(centerX + 25, y + 30, 200, 20,
                Text.literal("Angle: " + HudConfig.INSTANCE.coneAngleDeg),
                (double) HudConfig.INSTANCE.coneAngleDeg / 180) {
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

        addDrawableChild(ButtonWidget.builder(Text.literal("Show Hud: " + HudConfig.INSTANCE.showHud), button -> {
            HudConfig.INSTANCE.showHud = !HudConfig.INSTANCE.showHud;
            button.setMessage(Text.literal("Show Hud: " + HudConfig.INSTANCE.showHud));
        }).position(centerX - 50, y + 70).size(95, 20).build());


    }

    @Override
    public boolean shouldPause() {
        return false;
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

    }
    @Override
    public void close(){
        HudconfigManager.save();
        super.close();
    }
}

