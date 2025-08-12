package net.fataled.wynnstacks.mixin;

import net.fataled.wynnstacks.client.raidRelated.RaidModel;
import net.fataled.wynnstacks.event.TitleHandler;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class TitlePacketMixin {

    @Inject(method = "onTitle", at = @At("HEAD"))
    private void onTitle(TitleS2CPacket packet, CallbackInfo ci) {
        Text titleText = packet.text();
        if (titleText != null) {
            String rawTitle = titleText.getString().trim();
            TitleHandler.fire(rawTitle);
            RaidModel.setLastTitle(rawTitle);
            //MinecraftClient.getInstance().player.sendMessage(Text.literal(rawTitle), false);
        }
    }
}
