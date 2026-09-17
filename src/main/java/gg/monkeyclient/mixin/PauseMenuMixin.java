package gg.monkeyclient.mixin;
import gg.monkeyclient.MonkeyClient;
import gg.monkeyclient.ui.MonkeyMenuScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(PauseScreen.class)
public abstract class PauseMenuMixin extends Screen {
 protected PauseMenuMixin(Component title){super(title);}
 @Inject(method="init",at=@At("TAIL"),require=1)
 private void monkey$button(CallbackInfo ci){
  if(!MonkeyClient.ready())return;
  // A separate row below the title leaves the vanilla menu grid untouched.
  addRenderableWidget(Button.builder(Component.literal("Monkey Client"),b->Minecraft.getInstance().setScreenAndShow(new MonkeyMenuScreen())).bounds(width/2-100,Math.max(34,height/4-28),200,20).build());
 }
}
