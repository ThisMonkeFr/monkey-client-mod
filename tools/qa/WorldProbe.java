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
 @Unique private long monkeyqa$groupWait;
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

   if(monkeyqa$worldTicks==290||monkeyqa$worldTicks==330){
    boolean found=false;for(var child:mc.gui.screen().children())if(child instanceof gg.monkeyclient.ui.MenuButton b&&b.getMessage().getString().equals("Client Settings")){b.onClick(null,false);found=true;break;}
    if(!found)throw new IllegalStateException("Landing menu not restored");
    for(var child:mc.gui.screen().children())if(child instanceof gg.monkeyclient.ui.MenuButton b&&b.getMessage().getString().equals("Home"))throw new IllegalStateException("Home tab button returned");
    System.out.println("MONKEY_QA_MODULE_GRID");
   }
   if(monkeyqa$worldTicks==350)mc.setScreenAndShow(new gg.monkeyclient.ui.FriendsScreen(mc.gui.screen()));
   if(monkeyqa$worldTicks==400){monkeyqa$loaded(mc);boolean found=false;for(var child:mc.gui.screen().children())if(child instanceof gg.monkeyclient.ui.MenuButton b&&b.getMessage().getString().contains("Building crew")){b.onClick(null,false);mc.gui.screen().resize(mc.getWindow().getGuiScaledWidth(),mc.getWindow().getGuiScaledHeight());found=true;break;}if(!found)throw new IllegalStateException("Group row missing: "+mc.gui.screen().children().stream().filter(c->c instanceof gg.monkeyclient.ui.MenuButton).map(c->((gg.monkeyclient.ui.MenuButton)c).getMessage().getString()).toList());}
   if(monkeyqa$worldTicks==440){try{for(String fieldName:new String[]{"messages","members"}){var field=gg.monkeyclient.ui.FriendsScreen.class.getDeclaredField(fieldName);field.setAccessible(true);if(((com.google.gson.JsonArray)field.get(mc.gui.screen())).isEmpty()){if(monkeyqa$groupWait==0)monkeyqa$groupWait=System.nanoTime();if(System.nanoTime()-monkeyqa$groupWait<15_000_000_000L){monkeyqa$worldTicks--;return;}var status=gg.monkeyclient.ui.NativeScreen.class.getDeclaredField("status");status.setAccessible(true);throw new IllegalStateException("Native group "+fieldName+" missing: "+status.get(mc.gui.screen()));}}}catch(ReflectiveOperationException e){throw new IllegalStateException(e);}}
   if(monkeyqa$worldTicks==450){monkeyqa$loaded(mc);System.out.println("MONKEY_QA_NATIVE_FRIENDS");mc.setScreenAndShow(new gg.monkeyclient.ui.LauncherScreen(null,"screenshots"));}
   if(monkeyqa$worldTicks==500){monkeyqa$loaded(mc);for(var child:mc.gui.screen().children())if(child instanceof gg.monkeyclient.ui.MenuButton b&&b.getMessage().getString().isEmpty()){b.onClick(null,false);break;}for(var child:mc.gui.screen().children())if(child instanceof gg.monkeyclient.ui.MenuButton b&&b.getMessage().getString().equals("Open")){b.onClick(null,false);break;}}
   if(monkeyqa$worldTicks==550){if(!(mc.gui.screen() instanceof gg.monkeyclient.ui.NativeImageScreen))throw new IllegalStateException("Native image viewer missing");try{var field=gg.monkeyclient.ui.NativeScreen.class.getDeclaredField("pictures");field.setAccessible(true);if(((java.util.Map<?,?>)field.get(mc.gui.screen())).isEmpty())throw new IllegalStateException("Image preview failed to decode");}catch(ReflectiveOperationException e){throw new IllegalStateException(e);}System.out.println("MONKEY_QA_NATIVE_GALLERY");mc.setScreenAndShow(new gg.monkeyclient.ui.LauncherScreen(null,"skins"));}
   if(monkeyqa$worldTicks==650){monkeyqa$loaded(mc);boolean found=false;for(var child:mc.gui.screen().children())if(child instanceof gg.monkeyclient.ui.MenuButton b&&b.getMessage().getString().contains("Orange monkey")){b.onClick(null,false);found=true;break;}if(!found)throw new IllegalStateException("Shared skin library empty");}
   if(monkeyqa$worldTicks==700){boolean found=false;for(var child:mc.gui.screen().children())if(child instanceof gg.monkeyclient.ui.MenuButton b&&b.getMessage().getString().equals("Edit")&&b.active){b.onClick(null,false);found=true;break;}if(!found)throw new IllegalStateException("Native skin editor missing");}
   if(monkeyqa$worldTicks==720){
    if(!(mc.gui.screen() instanceof gg.monkeyclient.ui.SkinEditorScreen editor))throw new IllegalStateException("Skin editor did not open");
    try{var pixels=editor.getClass().getDeclaredField("pixels");pixels.setAccessible(true);var fieldX=editor.getClass().getDeclaredField("px");fieldX.setAccessible(true);var fieldY=editor.getClass().getDeclaredField("py");fieldY.setAccessible(true);int[] data=(int[])pixels.get(editor);int old=data[0];
     editor.mouseClicked(new net.minecraft.client.input.MouseButtonEvent(fieldX.getInt(editor)+.5,fieldY.getInt(editor)+.5,new net.minecraft.client.input.MouseButtonInfo(0,0)),false);
     if(data[0]==old)throw new IllegalStateException("Skin pixel ignored click");
    }catch(ReflectiveOperationException e){throw new IllegalStateException(e);}
    System.out.println("MONKEY_QA_NATIVE_SKINS");
    var saved=net.minecraft.network.chat.Component.literal("sample.png").withStyle(st->st.withClickEvent(new net.minecraft.network.chat.ClickEvent.OpenFile(mc.gameDirectory.toPath().resolve("screenshots/sample.png"))));
    var feedback=gg.monkeyclient.capture.ScreenshotActions.feedback(mc.gameDirectory,net.minecraft.network.chat.Component.translatable("screenshot.success",saved));
    if(!feedback.getString().equals("Screenshot taken [Open] [Delete]"))throw new IllegalStateException("Screenshot chat feedback missing");
    var delete=(net.minecraft.network.chat.ClickEvent.RunCommand)feedback.getSiblings().getLast().getStyle().getClickEvent();
    mc.player.connection.sendUnattendedCommand(delete.command(),mc.gui.screen());
    if(!(mc.gui.screen() instanceof net.minecraft.client.gui.screens.ConfirmScreen))throw new IllegalStateException("Screenshot delete click did not stay local");
    System.out.println("MONKEY_QA_SCREENSHOT_CHAT_PASS");
    mc.setScreenAndShow(new gg.monkeyclient.ui.LauncherScreen(null,"screenshots"));
   }
   if(monkeyqa$worldTicks==790){monkeyqa$loaded(mc);System.out.println("MONKEY_QA_BRIDGE_PASS");System.out.println("MONKEY_QA_WORLD_PASS");}

  }
 }
 @Unique private static void monkeyqa$loaded(Minecraft mc){
  try{if(!(mc.gui.screen() instanceof gg.monkeyclient.ui.NativeScreen))throw new IllegalStateException("Not a native screen");var field=gg.monkeyclient.ui.NativeScreen.class.getDeclaredField("loaded");field.setAccessible(true);if(!field.getBoolean(mc.gui.screen()))throw new IllegalStateException("Native data did not load");}
  catch(ReflectiveOperationException e){throw new IllegalStateException(e);}
 }

}
