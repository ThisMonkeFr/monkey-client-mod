package gg.monkeyclient.mixin;
import gg.monkeyclient.MonkeyClient;
import gg.monkeyclient.modules.Nametag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(AvatarRenderer.class)
public class NametagMixin {
 @Inject(method="shouldShowName(Lnet/minecraft/world/entity/Avatar;D)Z",at=@At("RETURN"),cancellable=true,require=1)
 private void monkeyclient$own(Avatar player,double distance,CallbackInfoReturnable<Boolean> cir){
  if(!MonkeyClient.ready())return;var m=MonkeyClient.modules().get(Nametag.class);if(!m.isEnabled())return;
  if(distance>m.range.get()*m.range.get())cir.setReturnValue(false);
  else if(player==Minecraft.getInstance().player)cir.setReturnValue(m.own.get()&&!player.isInvisible());
 }
 @Inject(method="extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",at=@At("TAIL"),require=1)
 private void monkeyclient$style(Avatar player,AvatarRenderState state,float partial,CallbackInfo ci){
  if(!MonkeyClient.ready())return;var m=MonkeyClient.modules().get(Nametag.class);
  if(m.isEnabled()&&state.nameTag!=null){
   var prefix=Component.literal(m.prefix.get());
   if(m.logo.get()&&player==Minecraft.getInstance().player)prefix.append(gg.monkeyclient.render.Icons.text("monkey")).append(" ");
   state.nameTag=prefix.append(state.nameTag).withColor((m.themed.get()?MonkeyClient.theme().text:m.color.resolve())&0xFFFFFF);
  }
  var tiers=MonkeyClient.modules().get(gg.monkeyclient.modules.TiersDisplay.class);
  if(tiers.isEnabled()&&tiers.names.get()&&state.nameTag!=null)state.nameTag=tiers.append(state.nameTag,player.getUUID());
 }
 @Inject(method="submitNameDisplay(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",at=@At("HEAD"),require=1)
 private void monkeyclient$beginName(CallbackInfo ci){Nametag.DRAWING.set(true);}
 @Inject(method="submitNameDisplay(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",at=@At("RETURN"),require=1)
 private void monkeyclient$endName(CallbackInfo ci){Nametag.DRAWING.set(false);}
}
