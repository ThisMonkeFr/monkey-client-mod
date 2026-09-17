package gg.monkeyclient.mixin;
import gg.monkeyclient.MonkeyClient;
import gg.monkeyclient.modules.Zoom;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(MouseHandler.class)
public class MouseMixin {
    @Shadow private double accumulatedDX;
    @Shadow private double accumulatedDY;
    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true, require = 1)
    private void monkeyclient$scroll(long window, double x, double y, CallbackInfo ci) {
        if (MonkeyClient.ready() && MonkeyClient.inGame(window) && MonkeyClient.modules().get(Zoom.class).scroll(y)) ci.cancel();
    }
    @Inject(method = "turnPlayer", at = @At("HEAD"), require = 1)
    private void monkeyclient$sensitivity(double dt, CallbackInfo ci) {
        if (!MonkeyClient.ready()) return;
        Zoom z = MonkeyClient.modules().get(Zoom.class);
        if (z.reduceSensitivity()) { accumulatedDX *= z.fovFactor(); accumulatedDY *= z.fovFactor(); }
    }
}