package gg.monkeyclient.mixin;
import gg.monkeyclient.MonkeyClient;
import gg.monkeyclient.modules.Zoom;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(GameRenderer.class)
public class FovMixin {
 @Inject(method="getFov",at=@At("RETURN"),cancellable=true,require=1)
 private void monkey$zoom(Camera camera,float tick,boolean world,CallbackInfoReturnable<Float> cir){if(world&&MonkeyClient.ready()){var zoom=MonkeyClient.modules().get(Zoom.class);zoom.advanceFrame();cir.setReturnValue(zoom.zoomFov(cir.getReturnValueF()));}}
}
