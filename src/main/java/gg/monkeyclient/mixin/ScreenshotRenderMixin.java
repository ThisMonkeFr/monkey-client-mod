package gg.monkeyclient.mixin;
import gg.monkeyclient.capture.ScreenshotCapture;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Minecraft.class)
public class ScreenshotRenderMixin {
 @Inject(method="renderFrame",at=@At(value="INVOKE",target="Lnet/minecraft/client/renderer/GameRenderer;render(Lnet/minecraft/client/DeltaTracker;Z)V",shift=At.Shift.AFTER),require=1)
 private void monkey$capture(CallbackInfo ci){ScreenshotCapture.afterRender();}
}
