package gg.monkeyclient.mixin;
import gg.monkeyclient.capture.ScreenshotCapture;
import net.minecraft.client.Screenshot;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.network.chat.Component;
import java.io.File;
import java.util.function.Consumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Screenshot.class)
public class ScreenshotMixin {
 @Inject(method="grab(Ljava/io/File;Lcom/mojang/blaze3d/pipeline/RenderTarget;Ljava/util/function/Consumer;)V",at=@At("HEAD"),cancellable=true,require=1)
 private static void monkey$capture(File directory,RenderTarget target,Consumer<Component> feedback,CallbackInfo ci){
  if(!ScreenshotCapture.saving()){ScreenshotCapture.request(directory,feedback);ci.cancel();}
 }
}
