package gg.monkeyclient.qa;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Test-only injection. Packaged only in isolated CI probe jars, never a release. */
@Mixin(Minecraft.class)
public class WorldProbe {
 @Unique private int monkeyqa$ticks,monkeyqa$worldTicks;
 @Unique private boolean monkeyqa$started;
 @Inject(method="tick",at=@At("TAIL"),require=1)
 private void monkeyqa$tick(CallbackInfo ci){
  Minecraft mc=(Minecraft)(Object)this;
  if(!gg.monkeyclient.MonkeyClient.ready())return;
  if(!monkeyqa$started&&++monkeyqa$ticks>240){
   monkeyqa$started=true;
   try{var resource=mc.getResourceManager().getResource(Identifier.fromNamespaceAndPath("monkeyclient","textures/logo.png")).orElseThrow();try(var input=resource.open()){if(input.read()!=137)throw new IllegalStateException("Invalid logo texture");}System.out.println("MONKEY_QA_RESOURCES_OK");}
   catch(Exception e){throw new IllegalStateException("Monkey Client textures unavailable",e);}
   CreateWorldScreen.openFresh(mc,()->{});
  }
  if(mc.player!=null&&mc.level!=null){
   monkeyqa$worldTicks++;
   if(monkeyqa$worldTicks==80){
    for(var module:gg.monkeyclient.MonkeyClient.modules().all()){module.enabled.set(true);module.onToggle(true);}
    System.out.println("MONKEY_QA_WORLD_JOINED");
   }
   if(monkeyqa$worldTicks==240){mc.setScreenAndShow(new net.minecraft.client.gui.screens.PauseScreen(true));System.out.println("MONKEY_QA_PAUSE_MENU");}
   if(monkeyqa$worldTicks==280){
    boolean found=false;for(var child:mc.gui.screen().children())if(child instanceof net.minecraft.client.gui.components.Button button&&button.getMessage().getString().equals("Monkey Client")){button.onPress(null);found=true;break;}
    if(!found)throw new IllegalStateException("Pause menu shortcut missing");System.out.println("MONKEY_QA_MENU_OPEN");
   }
   if(monkeyqa$worldTicks==330){
    boolean found=false;for(var child:mc.gui.screen().children())if(child instanceof gg.monkeyclient.ui.MenuButton button&&button.getMessage().getString().equals("Client Settings")){button.onClick(null,false);found=true;break;}
    if(!found)throw new IllegalStateException("Client settings shortcut missing");System.out.println("MONKEY_QA_MODULE_GRID");
   }
   if(monkeyqa$worldTicks==430)System.out.println("MONKEY_QA_WORLD_PASS");
  }
 }
}
