package net.fataled.shadestepperqol.shadestepper_qol_huds.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Environment(EnvType.CLIENT)
@Mixin(ClientConnection.class)
public class ClientConnectionMixin {
    private static final Logger LOGGER = LogManager.getLogger("ShadeStepperHUD");

    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;)V", at = @At("HEAD"))
    private void onSendPacket(Packet<?> packet, @Nullable PacketCallbacks callbacks, CallbackInfo ci) {
        Set<String> ignorePacket = Set.of("class_9836", "class_2935", "class_2827");
        if (!ignorePacket.contains(packet.getClass().getSimpleName())) {
            //LOGGER.info("→ Sending packet: {}", packet.getClass().getSimpleName());
        }
    }
}
//[03:14:29] [Render thread] [ShadeStepperHUD/INFO]:
//→ Sending packet: class_2813