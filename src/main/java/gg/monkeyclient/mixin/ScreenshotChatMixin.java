package gg.monkeyclient.mixin;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ClientPacketListener.class)
public class ScreenshotChatMixin {
 @Inject(method="sendCommand",at=@At("HEAD"),cancellable=true,require=1)
 private void monkey$screenshot(String command,CallbackInfo ci){if(gg.monkeyclient.capture.ScreenshotActions.handle(command))ci.cancel();}
 @Inject(method="sendUnattendedCommand",at=@At("HEAD"),cancellable=true,require=1)
 private void monkey$screenshotClick(String command,net.minecraft.client.gui.screens.Screen parent,CallbackInfo ci){if(gg.monkeyclient.capture.ScreenshotActions.handle(command))ci.cancel();}
}
