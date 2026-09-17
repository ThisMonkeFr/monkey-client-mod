package gg.monkeyclient.mixin;
import gg.monkeyclient.modules.ItemEditor;
import gg.monkeyclient.cosmetics.ItemAppearanceState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ItemStackRenderState.class)
public class ItemStateMixin implements ItemAppearanceState {
 @Unique private ItemEditor.Appearance monkeyclient$appearance;
 @Unique private boolean monkeyclient$pushed;
 @Override public void monkeyclient$appearance(ItemEditor.Appearance a){monkeyclient$appearance=a;}
 @Inject(method="clear",at=@At("HEAD"),require=1)
 private void monkeyclient$clear(CallbackInfo ci){monkeyclient$appearance=null;}
 @Inject(method="submit",at=@At("HEAD"),cancellable=true,require=1)
 private void monkeyclient$before(PoseStack pose,SubmitNodeCollector collector,int light,int overlay,int outline,CallbackInfo ci){
  monkeyclient$pushed=false;if(monkeyclient$appearance==null)return;
  pose.pushPose();monkeyclient$appearance.transform(pose);monkeyclient$pushed=true;
  if(monkeyclient$appearance.texture()!=null){collector.submitModelPart(ItemEditor.FLAT,pose,RenderTypes.entityCutout(monkeyclient$appearance.texture()),light,overlay,null,-1,null,outline);pose.popPose();monkeyclient$pushed=false;ci.cancel();}
 }
 @Inject(method="submit",at=@At("RETURN"),require=1)
 private void monkeyclient$after(PoseStack pose,SubmitNodeCollector collector,int light,int overlay,int outline,CallbackInfo ci){if(monkeyclient$pushed){pose.popPose();monkeyclient$pushed=false;}}
}
