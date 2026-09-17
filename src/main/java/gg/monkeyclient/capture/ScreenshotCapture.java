package gg.monkeyclient.capture;

import gg.monkeyclient.MonkeyClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.Component;
import java.io.File;
import java.util.function.Consumer;

/** Temporarily renders at UHD resolution; the window itself is never resized. */
public final class ScreenshotCapture {
 public static final int WIDTH=3840, HEIGHT=2160;
 private static boolean active, saving;
 private static int frames;
 private static File directory;
 private static Consumer<Component> feedback;
 public static boolean active(){return active;}
 public static boolean saving(){return saving;}
 public static void request(File dir,Consumer<Component> callback){
  if(active)return;
  directory=dir;feedback=callback;frames=0;active=true;
  try{resize();}catch(Exception e){fail(e);}
 }
 public static void afterRender(){
  if(!active||++frames<2)return;
  try{
   saving=true;
   Screenshot.grab(directory,Minecraft.getInstance().gameRenderer.mainRenderTarget(),feedback);
  }catch(Exception e){report(e);}finally{restore();}
 }
 private static void resize(){
  var mc=Minecraft.getInstance();var window=mc.getWindow();
  mc.gameRenderer.mainRenderTarget().resize(window.getScreenWidth(),window.getScreenHeight());
 }
 private static void report(Exception e){
  MonkeyClient.LOG.warn("4K screenshot failed",e);
  if(feedback!=null)feedback.accept(Component.literal("Could not capture a 4K screenshot: "+e.getMessage()));
 }
 private static void fail(Exception e){try{report(e);}finally{restore();}}
 private static void restore(){
  active=false;saving=false;directory=null;feedback=null;
  resize();
 }
 private ScreenshotCapture(){}
}
