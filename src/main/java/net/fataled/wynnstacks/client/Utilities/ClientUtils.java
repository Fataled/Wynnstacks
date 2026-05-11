package net.fataled.wynnstacks.client.Utilities;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;

import java.util.Set;
import java.util.stream.Collectors;

public final class ClientUtils {

    private ClientUtils() {}

    /** Returns the set of plain (no colours) player names in the tab‑list. */
    public static Set<String> getOnlinePlayerNames() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getNetworkHandler() == null) return Set.of();

        return mc.getNetworkHandler().getPlayerList().stream()
                .map(PlayerListEntry::getProfile)
                .map(GameProfile::name)
                .collect(Collectors.toUnmodifiableSet());
    }
}


