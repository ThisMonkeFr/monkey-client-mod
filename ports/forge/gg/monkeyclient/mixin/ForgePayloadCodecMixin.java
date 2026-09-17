package gg.monkeyclient.mixin;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import gg.monkeyclient.integration.apple.network.*;
import gg.monkeyclient.platform.PlatformLoader;
import java.util.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(CustomPacketPayload.class)
public interface ForgePayloadCodecMixin {
 @ModifyVariable(method="codec(Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload$FallbackProvider;Ljava/util/List;)Lnet/minecraft/network/codec/StreamCodec;",at=@At("HEAD"),argsOnly=true,require=1)
 private static List<CustomPacketPayload.TypeAndCodec<?,?>> monkey$foodCodecs(List<CustomPacketPayload.TypeAndCodec<?,?>> original){
  if(PlatformLoader.getInstance().isModLoaded("appleskin"))return original;
  var list=new ArrayList<>(original);for(var codec:List.of(new CustomPacketPayload.TypeAndCodec<>(ExhaustionSyncPayload.ID,ExhaustionSyncPayload.CODEC),new CustomPacketPayload.TypeAndCodec<>(SaturationSyncPayload.ID,SaturationSyncPayload.CODEC),new CustomPacketPayload.TypeAndCodec<>(NaturalRegenerationSyncPayload.ID,NaturalRegenerationSyncPayload.CODEC)))if(list.stream().noneMatch(c->c.type().id().equals(codec.type().id())))list.add(codec);return list;
 }
}
