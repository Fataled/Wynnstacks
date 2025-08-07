package net.fataled.wynnstacks.client;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;

import java.util.Set;
import java.util.stream.Collectors;

public final class ClientUtils {

    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger("ShadestepperHUD");

    /** Returns the set of plain (no colours) player names in the tab‑list. */
    public static Set<String> getOnlinePlayerNames() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getNetworkHandler() == null) return Set.of(mc.player.getName().getString());

        return mc.getNetworkHandler().getPlayerList().stream()
                .map(PlayerListEntry::getProfile)
                .map(GameProfile::getName)
                .collect(Collectors.toUnmodifiableSet());

    }

    public static void Online(){
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        Set<String> Gamers= getOnlinePlayerNames();

        if (Gamers.contains("AverageGisu")) {
            player.sendMessage(Text.literal("AAAAHHHH ITS GISU RUNNNN"), false);
        }
    }

}


