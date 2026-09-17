package gg.monkeyclient.mixin;
import gg.monkeyclient.MonkeyClient;
import gg.monkeyclient.modules.Nametag;
import net.minecraft.client.renderer.SubmitNodeCollection;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(SubmitNodeCollection.class)
public class NameScaleMixin {
 @Redirect(method="submitNameTag",at=@At(value="INVOKE",target="Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V"),require=1)
 private void monkeyclient$scale(PoseStack pose,float x,float y,float z){
  float s=1;if(MonkeyClient.ready()&&Nametag.DRAWING.get()){var m=MonkeyClient.modules().get(Nametag.class);if(m.isEnabled())s=m.scale.getFloat();}pose.scale(x*s,y*s,z*s);
 }
}
