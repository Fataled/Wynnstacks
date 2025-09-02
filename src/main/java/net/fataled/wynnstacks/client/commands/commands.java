package net.fataled.wynnstacks.client.commands;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fataled.wynnstacks.client.RaidCounter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;


public class commands {

    public static void registerCommands() {

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal("Wynnstacks")
                .then(literal("chungus")
                                .executes(ctx -> {
                                        RaidCounter.raidCompletions.replaceAll((k , v) -> 0);
                                        RaidCounter.raidFails.replaceAll((k , v) -> 0);
                                    MinecraftClient.getInstance().player.sendMessage(Text.literal("Reset Raids oh and blame wintel for the name of the command"), false);
                                    return 1;
                                })))

        );



    }
}