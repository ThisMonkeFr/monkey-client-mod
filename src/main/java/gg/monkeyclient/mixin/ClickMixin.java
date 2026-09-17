package gg.monkeyclient.mixin;
import gg.monkeyclient.MonkeyClient;
import gg.monkeyclient.modules.CpsDisplay;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(MouseHandler.class)
public class ClickMixin {
 @Inject(method="onButton",at=@At("HEAD"),require=1)
 private void monkeyclient$click(long window,MouseButtonInfo info,int action,CallbackInfo ci){
  if(action==1&&MonkeyClient.ready()&&Minecraft.getInstance().gui.screen()==null){var m=MonkeyClient.modules().get(CpsDisplay.class);if(m.isEnabled())m.click(info.button());}
 }
}
