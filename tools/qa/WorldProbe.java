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
   var flight=gg.monkeyclient.MonkeyClient.modules().get(gg.monkeyclient.modules.ToggleSprint.class);
   if(monkeyqa$worldTicks==90){var server=mc.getSingleplayerServer();server.execute(()->server.getPlayerList().getPlayer(mc.player.getUUID()).setGameMode(net.minecraft.world.level.GameType.CREATIVE));flight.flight.set(10d);flight.flightMode.set("Automatic");}
   if(monkeyqa$worldTicks==105){mc.player.getAbilities().flying=true;mc.options.keyUp.setDown(true);}
   if(monkeyqa$worldTicks>110&&monkeyqa$worldTicks<165){if(Math.abs(mc.player.getAbilities().getFlyingSpeed()-.5f)>.0001)throw new IllegalStateException("Flight multiplier compounded");}
   if(monkeyqa$worldTicks==170){mc.options.keyUp.setDown(false);flight.enabled.set(false);flight.onToggle(false);}
   if(monkeyqa$worldTicks==190){if(Math.abs(mc.player.getAbilities().getFlyingSpeed()-.05f)>.0001)throw new IllegalStateException("Flight failed to restore");System.out.println("MONKEY_QA_FLIGHT_PASS");}
   if(monkeyqa$worldTicks==240){mc.setScreenAndShow(new net.minecraft.client.gui.screens.PauseScreen(true));System.out.println("MONKEY_QA_PAUSE_MENU");}
   if(monkeyqa$worldTicks==280){
    boolean found=false;for(var child:mc.gui.screen().children())if(child instanceof net.minecraft.client.gui.components.Button button&&button.getMessage().getString().equals("Monkey Client")){button.onPress(null);found=true;break;}
    if(!found)throw new IllegalStateException("Pause menu shortcut missing");System.out.println("MONKEY_QA_MENU_OPEN");
   }
   if(monkeyqa$worldTicks==310){
    var color=new gg.monkeyclient.module.setting.ColorSetting("test","Test",0xFF00FF00);var wheel=new gg.monkeyclient.ui.ColorPickerScreen(null,color,()->{});mc.setScreenAndShow(wheel);
    int sw=mc.getWindow().getGuiScaledWidth(),sh=mc.getWindow().getGuiScaledHeight();
    wheel.mouseClicked(new net.minecraft.client.input.MouseButtonEvent(sw/2+3,sh/2-7,new net.minecraft.client.input.MouseButtonInfo(0,0)),false);
    if(color.get()==0xFF00FF00)throw new IllegalStateException("Color wheel ignored click");
    var cross=gg.monkeyclient.MonkeyClient.modules().get(gg.monkeyclient.modules.CustomCrosshair.class);cross.pixels.set("0".repeat(1089));var drawing=new gg.monkeyclient.ui.CrosshairEditorScreen(null,cross);mc.setScreenAndShow(drawing);int cell=Math.max(2,Math.min(10,(sh-75)/33)),left=(sw-cell*33)/2;
    drawing.mouseClicked(new net.minecraft.client.input.MouseButtonEvent(left+16*cell+1,32+16*cell+1,new net.minecraft.client.input.MouseButtonInfo(0,0)),false);
    if(cross.pixels.get().charAt(544)!='1')throw new IllegalStateException("Crosshair ignored click");
    mc.setScreenAndShow(new gg.monkeyclient.ui.MonkeyMenuScreen());System.out.println("MONKEY_QA_INPUT_PASS");
   }
   if(monkeyqa$worldTicks==330)System.out.println("MONKEY_QA_MODULE_GRID");
   if(monkeyqa$worldTicks==350&&gg.monkeyclient.integration.LauncherBridge.available())mc.setScreenAndShow(new gg.monkeyclient.ui.LauncherScreen(mc.gui.screen(),"friends"));
   if(monkeyqa$worldTicks>=430&&monkeyqa$worldTicks<630){
    if(!gg.monkeyclient.integration.LauncherBridge.available()){if(monkeyqa$worldTicks==430)System.out.println("MONKEY_QA_WORLD_PASS");}
    else try{var field=gg.monkeyclient.ui.LauncherScreen.class.getDeclaredField("loaded");field.setAccessible(true);if(field.getBoolean(mc.gui.screen())){System.out.println("MONKEY_QA_BRIDGE_PASS");System.out.println("MONKEY_QA_WORLD_PASS");monkeyqa$worldTicks=630;}}catch(Exception e){throw new IllegalStateException("In-game launcher tab failed",e);}
   }
  }
 }
}
