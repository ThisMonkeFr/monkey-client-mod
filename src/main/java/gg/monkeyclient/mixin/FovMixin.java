package gg.monkeyclient.mixin;
import gg.monkeyclient.MonkeyClient;
import gg.monkeyclient.modules.Zoom;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Camera.class)
public class FovMixin {
    @Inject(method = "calculateFov", at = @At("RETURN"), cancellable = true, require = 1)
    private void monkeyclient$worldZoom(float partialTick, CallbackInfoReturnable<Float> cir) {
        if (!MonkeyClient.ready()) return;
        Zoom zoom = MonkeyClient.modules().get(Zoom.class);
        zoom.advanceFrame();
        cir.setReturnValue(zoom.zoomFov(cir.getReturnValueF()));
    }
}
