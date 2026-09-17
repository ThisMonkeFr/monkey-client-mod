package gg.monkeyclient.mixin;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import gg.monkeyclient.integration.apple.network.ClientSyncHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ClientCommonPacketListenerImpl.class)
public class ForgePayloadMixin {
 @Inject(method="handleCustomPayload(Lnet/minecraft/network/protocol/common/ClientboundCustomPayloadPacket;)V",at=@At("HEAD"),cancellable=true,require=1)
 private void monkey$food(ClientboundCustomPayloadPacket packet,CallbackInfo ci){if(ClientSyncHandler.accept(packet.payload()))ci.cancel();}
}
