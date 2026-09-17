package gg.monkeyclient.mixin;
import gg.monkeyclient.MonkeyClient;
import gg.monkeyclient.modules.CoordsDisplay;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.gui.components.debug.DebugScreenEntryList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(DebugScreenOverlay.class)
public class DebugOverlayMixin {
    @Redirect(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/debug/DebugScreenEntryList;isOverlayVisible()Z"), require = 1)
    private boolean monkeyclient$hints(DebugScreenEntryList list) {
        if (!MonkeyClient.ready()) return list.isOverlayVisible();
        var e = MonkeyClient.modules().get(CoordsDisplay.class);
        return list.isOverlayVisible() && (!e.isEnabled() || e.help.get()&&!e.coordsOnly.get());
    }
    @Inject(method = {"showProfilerChart", "showNetworkCharts", "showFpsCharts", "showLightmapTexture"}, at = @At("HEAD"), cancellable = true, require = 1)
    private void monkeyclient$charts(CallbackInfoReturnable<Boolean> cir) {
        if (!MonkeyClient.ready()) return;
        var e = MonkeyClient.modules().get(CoordsDisplay.class);
        if (e.isEnabled() && (!e.charts.get()||e.coordsOnly.get())) cir.setReturnValue(false);
    }
}