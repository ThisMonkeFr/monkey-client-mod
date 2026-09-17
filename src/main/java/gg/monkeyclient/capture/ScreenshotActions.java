package gg.monkeyclient.capture;
import gg.monkeyclient.MonkeyClient;
import gg.monkeyclient.integration.LauncherBridge;
import gg.monkeyclient.ui.NativeScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.contents.TranslatableContents;
import java.io.File;
import java.nio.file.*;
import java.util.*;
/** Only locally captured paths receive delete capabilities; no paths in commands. */
public final class ScreenshotActions {
 private static final Map<String,Path> files=Collections.synchronizedMap(new LinkedHashMap<>());
 private static final String COMMAND="monkeyclient-screenshot-delete ";
 public static Component feedback(File directory,Component original){
  if(!(original.getContents() instanceof TranslatableContents t)||!t.getKey().equals("screenshot.success"))return original;
  ClickEvent.OpenFile click=findFile(original);if(click==null)return original;
  Path root=directory.toPath().toAbsolutePath().normalize().resolve("screenshots"),path=Path.of(click.path()).toAbsolutePath().normalize();
  if(!root.equals(path.getParent())||!path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".png"))return original;
  String id=UUID.randomUUID().toString();synchronized(files){files.put(id,path);while(files.size()>256)files.remove(files.keySet().iterator().next());}
  int accent=MonkeyClient.theme().accent&0xFFFFFF,ink=MonkeyClient.theme().text&0xFFFFFF;
  return Component.literal("Screenshot taken ").withColor(ink)
   .append(Component.literal("[Open]").withStyle(s->s.withColor(accent).withUnderlined(true).withClickEvent(new ClickEvent.OpenFile(path))))
   .append(" ").append(Component.literal("[Delete]").withStyle(s->s.withColor(accent).withUnderlined(true).withClickEvent(new ClickEvent.RunCommand(COMMAND+id))));
 }
 private static ClickEvent.OpenFile findFile(Component c){if(c.getStyle().getClickEvent() instanceof ClickEvent.OpenFile f)return f;for(Component child:c.getSiblings()){var f=findFile(child);if(f!=null)return f;}if(c.getContents() instanceof TranslatableContents t)for(Object a:t.getArgs())if(a instanceof Component child){var f=findFile(child);if(f!=null)return f;}return null;}
 public static boolean handle(String command){if(!command.startsWith(COMMAND))return false;String id=command.substring(COMMAND.length());Path file=files.get(id);var mc=Minecraft.getInstance();if(file==null){tell("That screenshot action has expired.");return true;}var parent=mc.gui.screen();mc.setScreenAndShow(new ConfirmScreen(ok->{mc.setScreenAndShow(parent);if(!ok)return;
   if(LauncherBridge.available())LauncherBridge.json("/screenshots/local-delete",NativeScreen.obj("name",file.getFileName().toString())).whenComplete((r,e)->mc.execute(()->{if(e==null){files.remove(id);tell("Screenshot deleted");}else tell("Could not delete screenshot: "+e.getCause().getMessage());}));
   else LauncherBridge.WORK.execute(()->{try{if(Files.isSymbolicLink(file)||!Files.isRegularFile(file,LinkOption.NOFOLLOW_LINKS))throw new IllegalStateException("Screenshot is no longer available");Files.delete(file);files.remove(id);mc.execute(()->tell("Screenshot deleted"));}catch(Exception e){mc.execute(()->tell("Could not delete screenshot: "+e.getMessage()));}});
  },Component.literal("Delete screenshot?"),Component.literal("This removes "+file.getFileName()+" from your screenshot library.")));return true;}
 private static void tell(String text){Minecraft.getInstance().gui.hud.getChat().addClientSystemMessage(Component.literal(text).withColor(MonkeyClient.theme().accent&0xFFFFFF));}
 private ScreenshotActions(){}
}
