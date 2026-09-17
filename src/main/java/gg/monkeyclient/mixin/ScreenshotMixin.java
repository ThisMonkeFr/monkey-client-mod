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
import com.mojang.blaze3d.platform.NativeImage;
@Mixin(Screenshot.class)
public class ScreenshotMixin {
 @ModifyArg(method="grab(Ljava/io/File;Ljava/lang/String;Lcom/mojang/blaze3d/pipeline/RenderTarget;ILjava/util/function/Consumer;)V",at=@At(value="INVOKE",target="Lnet/minecraft/client/Screenshot;takeScreenshot(Lcom/mojang/blaze3d/pipeline/RenderTarget;ILjava/util/function/Consumer;)V"),index=2,require=1)
 private static Consumer<NativeImage> monkey$preview(Consumer<NativeImage> original){return image->{try{gg.monkeyclient.capture.ScreenshotFeedback.captured(image);}catch(Exception error){gg.monkeyclient.MonkeyClient.LOG.warn("Screenshot preview unavailable",error);}original.accept(image);};}
 @Inject(method="grab(Ljava/io/File;Lcom/mojang/blaze3d/pipeline/RenderTarget;Ljava/util/function/Consumer;)V",at=@At("HEAD"),cancellable=true,require=1)
 private static void monkey$capture(File directory,RenderTarget target,Consumer<Component> feedback,CallbackInfo ci){
  if(!ScreenshotCapture.saving()){ScreenshotCapture.request(directory,message->feedback.accept(gg.monkeyclient.capture.ScreenshotActions.feedback(directory,message)));ci.cancel();}
 }
}
