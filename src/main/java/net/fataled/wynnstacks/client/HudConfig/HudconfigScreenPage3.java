package net.fataled.wynnstacks.client.HudConfig;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

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

        int ScreenWidth= this.width;
        int bottomMarginX = 50;
        int maxX = ScreenWidth - bottomMarginX;

        // make sure current y is within range
        HudConfig.INSTANCE.satsujinX = MathHelper.clamp(HudConfig.INSTANCE.satsujinX, 0, maxX);

        // 0.0–1.0 slider value
        double initialValueX = HudConfig.INSTANCE.satsujinX / (double) maxX;

        addDrawableChild(new SliderWidget(centerX - 225, y, 200, 20,
                Text.literal("X: " + HudConfig.INSTANCE.satsujinX),
                initialValueX) {

            @Override
            protected void updateMessage() {
                setMessage(Text.literal("X: " + HudConfig.INSTANCE.satsujinX));
            }

            @Override
            protected void applyValue() {
                HudConfig.INSTANCE.satsujinX = (int) (this.value * maxX);
                updateMessage();
            }
        });

        int screenHeight = this.height;
        int bottomMarginY = 70;
        int maxY = screenHeight - bottomMarginY;

        // make sure current y is within range
        HudConfig.INSTANCE.satsujinY = MathHelper.clamp(HudConfig.INSTANCE.satsujinY, 0, maxY);

        // 0.0–1.0 slider value
        double initialValueY = HudConfig.INSTANCE.satsujinY / (double) maxY;

        addDrawableChild(new SliderWidget(centerX - 225, y + 30, 200, 20,
                Text.literal("Y: " + HudConfig.INSTANCE.satsujinY),
                initialValueY) {

            @Override
            protected void updateMessage() {
                setMessage(Text.literal("Y: " + HudConfig.INSTANCE.satsujinY));
            }

            @Override
            protected void applyValue() {
                HudConfig.INSTANCE.satsujinY = (int) (this.value * maxY);
                updateMessage();
            }
        });

        addDrawableChild(ButtonWidget.builder(Text.literal("Aspect LVL 2: " + HudConfig.INSTANCE.aspectLvl2), button -> {
            HudConfig.INSTANCE.aspectLvl2 = !HudConfig.INSTANCE.aspectLvl2;
            button.setMessage(Text.literal("Aspect LVL 2: " + HudConfig.INSTANCE.aspectLvl2));
        }).position(centerX - 100, y + 90).size(95, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Show Hud: " + HudConfig.INSTANCE.showSatsujinHud), button -> {
            HudConfig.INSTANCE.showSatsujinHud = !HudConfig.INSTANCE.showSatsujinHud;
            button.setMessage(Text.literal("Show Hud: " + HudConfig.INSTANCE.showSatsujinHud));
        }).position(centerX, y + 90).size(95, 20).build());

        addDrawableChild(new SliderWidget(centerX + 25, y + 30, 200, 20,
                Text.literal("Volume: " + String.format("%.0f",HudConfig.INSTANCE.volume)+ "%"),
                HudConfig.INSTANCE.volume / 100) {
            @Override
            protected void updateMessage() {
                setMessage(Text.literal("Volume: " + String.format("%.0f",HudConfig.INSTANCE.volume)+ "%"));
            }

            @Override
            protected void applyValue() {
                HudConfig.INSTANCE.volume = (float) (this.value * 100f);
                updateMessage();
            }
        });

        addDrawableChild(ButtonWidget.builder(Text.literal("Back"), b ->
                MinecraftClient.getInstance().setScreen(parent)).position(centerX - 100, y + 140).size(200, 20).build());


        addDrawableChild(new SliderWidget(centerX + 25 , y, 200, 20,
                Text.literal("Scale: " + String.format("%.2f",HudConfig.INSTANCE.obtain(HudConfig.SATSUJIN).scale) ),
                (HudConfig.INSTANCE.obtain(HudConfig.SATSUJIN).scale - 1f) / 2.0f) {
            @Override
            protected void updateMessage() {
                setMessage(Text.literal("Scale: " + String.format("%.2f", HudConfig.INSTANCE.obtain(HudConfig.SATSUJIN).scale)));
            }

            @Override
            protected void applyValue() {
                HudConfig.INSTANCE.obtain(HudConfig.SATSUJIN).scale = (float) (1+this.value *2.0);// 0.5 to 2.5
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

