package gg.monkeyclient.mixin;
import gg.monkeyclient.MonkeyClient;
import gg.monkeyclient.modules.FogEditor;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(FogRenderer.class)
public class FogMixin {
 @Inject(method="setupFog",at=@At("RETURN"),require=1)
 private void monkeyclient$fog(Camera camera,int chunks,DeltaTracker delta,float darken,ClientLevel level,CallbackInfoReturnable<FogData> cir){
  if(MonkeyClient.ready()){var m=MonkeyClient.modules().get(FogEditor.class);if(m.isEnabled())m.apply(cir.getReturnValue(),camera.getFluidInCamera());}
 }
}
