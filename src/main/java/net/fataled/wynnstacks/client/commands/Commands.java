package net.fataled.wynnstacks.client.commands;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class Commands {

    private static final int GRADIENT_START = 0xFEFEFE;
    private static final int GRADIENT_END   = 0xCDFEFE;
    private static final int DEBUFFS_COLOR  = 0x71FEFE;

    public static void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(literal("Wynnstacks")
                        .then(literal("Help").executes(ctx -> {
                            var player = MinecraftClient.getInstance().player;
                            if (player != null) player.sendMessage(buildHelpText(), false);
                            return 1;
                        }))
                )
        );
    }

    private static Text buildHelpText() {
        MutableText out = Text.empty();
        gradient(out,
                "Hi you see to be new to Wynnstacks or you just needed help so let me explain how this works\n"
              + "If you press H you can open the main menu select what you need to modify.\n",
                GRADIENT_START, GRADIENT_END);

        out.append(Text.literal(
                "Debuffs - Select the X Y pos of the hud and in the top right select what debuffs are shown\n"
        ).styled(s -> s.withColor(DEBUFFS_COLOR)));

        out.append(Text.literal(
                "Satsujin CD - Only use this if you are a shadestepper as it can cause performance hits"
        ).formatted(Formatting.AQUA));

        return out;
    }

    private static void gradient(MutableText out, String text, int from, int to) {
        int len = text.length();
        for (int i = 0; i < len; i++) {
            float t = len > 1 ? (float) i / (len - 1) : 0f;
            int color = lerpColor(from, to, t);
            out.append(Text.literal(String.valueOf(text.charAt(i)))
                    .styled(s -> s.withColor(color)));
        }
    }

    private static int lerpColor(int from, int to, float t) {
        int fr = (from >> 16) & 0xFF, fg = (from >> 8) & 0xFF, fb = from & 0xFF;
        int tr = (to   >> 16) & 0xFF, tg = (to   >> 8) & 0xFF, tb = to   & 0xFF;
        int r = Math.round(fr + (tr - fr) * t);
        int g = Math.round(fg + (tg - fg) * t);
        int b = Math.round(fb + (tb - fb) * t);
        return (r << 16) | (g << 8) | b;
    }
}
