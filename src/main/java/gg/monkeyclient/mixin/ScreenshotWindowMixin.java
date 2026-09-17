package gg.monkeyclient.mixin;
import gg.monkeyclient.capture.ScreenshotCapture;
import com.mojang.blaze3d.platform.Window;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(value=Window.class,priority=900)
public class ScreenshotWindowMixin {
 @Shadow public int getGuiScale(){throw new AssertionError();}
 @ModifyReturnValue(method={"getWidth","getScreenWidth"},at=@At("RETURN"),require=1)
 private int monkey$width(int original){return ScreenshotCapture.active()?ScreenshotCapture.WIDTH:original;}
 @ModifyReturnValue(method={"getHeight","getScreenHeight"},at=@At("RETURN"),require=1)
 private int monkey$height(int original){return ScreenshotCapture.active()?ScreenshotCapture.HEIGHT:original;}
 @ModifyReturnValue(method="getGuiScaledWidth",at=@At("RETURN"),require=1)
 private int monkey$guiWidth(int original){return ScreenshotCapture.active()?(int)Math.ceil((double)ScreenshotCapture.WIDTH/Math.max(1,getGuiScale())):original;}
 @ModifyReturnValue(method="getGuiScaledHeight",at=@At("RETURN"),require=1)
 private int monkey$guiHeight(int original){return ScreenshotCapture.active()?(int)Math.ceil((double)ScreenshotCapture.HEIGHT/Math.max(1,getGuiScale())):original;}
}
