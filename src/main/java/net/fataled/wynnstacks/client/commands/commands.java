package net.fataled.wynnstacks.client.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;

import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;


public class commands {
    public static void registerCommands() {


        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal("Wynnstacks")
                .then(literal("Help")
                                .executes(ctx -> {
                                    MinecraftClient.getInstance().player.sendMessage(HELP_TEXT, false);
                                    return 1;
                                })))

        );



    }
        // Your original JSON, unchanged
        private static final String HELP_JSON = """
[
{"text":"H","color":"white"},
{"text":"i ","color":"#fefefe"},
{"text":"y","color":"#fdfefe"},
{"text":"ou ","color":"#fcfefe"},
{"text":"s","color":"#fbfefe"},
{"text":"e","color":"#fafefe"},
{"text":"e t","color":"#f9fefe"},
{"text":"o ","color":"#f8fefe"},
{"text":"be ","color":"#f7fefe"},
{"text":"n","color":"#f6fefe"},
{"text":"e","color":"#f5fefe"},
{"text":"w t","color":"#f4fefe"},
{"text":"o ","color":"#f3fefe"},
{"text":"Wy","color":"#f2fefe"},
{"text":"n","color":"#f1fefe"},
{"text":"n","color":"#f0fefe"},
{"text":"st","color":"#effefe"},
{"text":"a","color":"#eefefe"},
{"text":"ck","color":"#edfefe"},
{"text":"s ","color":"#ecfefe"},
{"text":"o","color":"#ebfefe"},
{"text":"r y","color":"#eafefe"},
{"text":"o","color":"#e9fefe"},
{"text":"u j","color":"#e8fefe"},
{"text":"u","color":"#e7fefe"},
{"text":"s","color":"#e6fefe"},
{"text":"t n","color":"#e5fefe"},
{"text":"e","color":"#e4fefe"},
{"text":"ed ","color":"#e3fefe"},
{"text":"h","color":"#e2fefe"},
{"text":"e","color":"#e1fefe"},
{"text":"lp ","color":"#e0fefe"},
{"text":"s","color":"#dffefe"},
{"text":"o l","color":"#defefe"},
{"text":"e","color":"#ddfefe"},
{"text":"t ","color":"#dcfefe"},
{"text":"me ","color":"#dbfefe"},
{"text":"e","color":"#dafefe"},
{"text":"xp","color":"#d9fefe"},
{"text":"l","color":"#d8fefe"},
{"text":"a","color":"#d7fefe"},
{"text":"in ","color":"#d6fefe"},
{"text":"h","color":"#d5fefe"},
{"text":"ow ","color":"#d4fefe"},
{"text":"t","color":"#d3fefe"},
{"text":"h","color":"#d2fefe"},
{"text":"is ","color":"#d1fefe"},
{"text":"w","color":"#d0fefe"},
{"text":"or","color":"#cffefe"},
{"text":"k","color":"#cefefe"},
{"text":"s \\n","color":"#cdfefe"},
{"text":"If ","color":"#ccfefe"},
{"text":"y","color":"#cbfefe"},
{"text":"o","color":"#cafefe"},
{"text":"u p","color":"#c9fefe"},
{"text":"r","color":"#c8fefe"},
{"text":"es","color":"#c7fefe"},
{"text":"s ","color":"#c6fefe"},
{"text":"H ","color":"#c5fefe"},
{"text":"yo","color":"#c4fefe"},
{"text":"u ","color":"#c3fefe"},
{"text":"ca","color":"#c2fefe"},
{"text":"n ","color":"#c1fefe"},
{"text":"o","color":"#c0fefe"},
{"text":"pe","color":"#bffefe"},
{"text":"n ","color":"#befefe"},
{"text":"th","color":"#bdfefe"},
{"text":"e ","color":"#bcfefe"},
{"text":"m","color":"#bbfefe"},
{"text":"ai","color":"#bafefe"},
{"text":"n ","color":"#b9fefe"},
{"text":"me","color":"#b8fefe"},
{"text":"n","color":"#b7fefe"},
{"text":"u ","color":"#b6fefe"},
{"text":"se","color":"#b5fefe"},
{"text":"l","color":"#b4fefe"},
{"text":"ec","color":"#b3fefe"},
{"text":"t ","color":"#b2fefe"},
{"text":"w","color":"#b1fefe"},
{"text":"ha","color":"#b0fefe"},
{"text":"t ","color":"#affefe"},
{"text":"yo","color":"#aefefe"},
{"text":"u ","color":"#adfefe"},
{"text":"n","color":"#acfefe"},
{"text":"ee","color":"#abfefe"},
{"text":"d ","color":"#aafefe"},
{"text":"to ","color":"#a9fefe"},
{"text":"m","color":"#a8fefe"},
{"text":"o","color":"#a7fefe"},
{"text":"di","color":"#a6fefe"},
{"text":"f","color":"#a5fefe"},
{"text":"y.\\n","color":"#a4fefe"},
{"text":"Debuffs - Select the X Y pos of the hud and in the top right select what debuffs are shown\\n","color":"#71fefe"},
{"text":"Satsujin CD - Only use this if you are a shadestepper as it can cause performance hits\\n\\n","color":"aqua"}
]
""";

        public static final Text HELP_TEXT = parseHelpText();

        private static Text parseHelpText() {
            JsonElement element = JsonParser.parseString(HELP_JSON);

            return TextCodecs.CODEC
                    .parse(JsonOps.INSTANCE, element)
                    .result()
                    .orElse(Text.empty());
        }
    }
