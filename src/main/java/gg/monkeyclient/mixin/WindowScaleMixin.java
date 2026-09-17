package gg.monkeyclient.mixin;
import gg.monkeyclient.MonkeyClient;
import gg.monkeyclient.modules.GuiScale;
import com.mojang.blaze3d.platform.Window;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Window.class)
public class WindowScaleMixin {
 @Shadow public int getWidth(){throw new AssertionError();}
 @Shadow public int getHeight(){throw new AssertionError();}
 @Inject(method="getGuiScaledWidth",at=@At("RETURN"),cancellable=true,require=1)
 private void monkeyclient$width(CallbackInfoReturnable<Integer> cir){if(MonkeyClient.ready()){var m=MonkeyClient.modules().get(GuiScale.class);if(m.isEnabled())cir.setReturnValue((int)Math.ceil(getWidth()/m.effectiveScale()));}}
 @Inject(method="getGuiScaledHeight",at=@At("RETURN"),cancellable=true,require=1)
 private void monkeyclient$height(CallbackInfoReturnable<Integer> cir){if(MonkeyClient.ready()){var m=MonkeyClient.modules().get(GuiScale.class);if(m.isEnabled())cir.setReturnValue((int)Math.ceil(getHeight()/m.effectiveScale()));}}
}
