package gg.monkeyclient.qa;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(CreateWorldScreen.class)
public class CreateProbe {
 @Inject(method="init",at=@At("TAIL"),require=1)
 private void monkeyqa$create(CallbackInfo ci){
  CreateWorldScreen screen=(CreateWorldScreen)(Object)this;
  screen.getUiState().setName("Monkey QA fresh world");
  Minecraft.getInstance().execute(()->{for(var child:screen.children())if(child instanceof Button button&&button.getMessage().getString().equals("Create New World")){button.onPress(null);break;}});
 }
}
