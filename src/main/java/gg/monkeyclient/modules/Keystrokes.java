package gg.monkeyclient.modules;
import gg.monkeyclient.hud.HudElement;
import gg.monkeyclient.module.Module;
import gg.monkeyclient.module.Category;
import gg.monkeyclient.module.setting.*;
import gg.monkeyclient.ui.KeyEditorScreen;
import gg.monkeyclient.MonkeyClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;
import com.google.gson.*;
import java.util.*;
public class Keystrokes extends HudElement {
 public final JsonSetting keys=add(new JsonSetting("keys","Individual keys"));
 public Keystrokes(){super("keystrokes","Keystrokes","Individually editable keyboard and mouse keys",false);bg.setDefault(false);defaults();}
 private void defaults(){keys.get().add(key("W",87,18,0));keys.get().add(key("A",65,0,18));keys.get().add(key("S",83,18,18));keys.get().add(key("D",68,36,18));}
 private JsonObject key(String label,int code,int x,int y){var k=new JsonObject();k.addProperty("label",label);k.addProperty("key",code);k.addProperty("x",x);k.addProperty("y",y);return k;}
 public void addKey(){keys.get().add(key("KEY",81,0,40));}
 @Override public void resetAll(){super.resetAll();defaults();}
 @Override public Screen editor(Screen parent){return new KeyEditorScreen(parent,this);}
 @Override public String editorLabel(){return "EDIT INDIVIDUAL KEYS";}
 private static double number(JsonObject k,String key,double fallback){try{double v=k.get(key).getAsDouble();return Double.isFinite(v)?v:fallback;}catch(Exception e){return fallback;}}
 private static String string(JsonObject k,String key,String fallback){try{return k.get(key).getAsString();}catch(Exception e){return fallback;}}
 private static int color(JsonObject k,String key,int fallback){try{String raw=k.get(key).getAsString();if(raw.endsWith(":rainbow"))return java.awt.Color.HSBtoRGB((System.currentTimeMillis()%6000)/6000f,.8f,1f);return (int)Long.parseLong(raw,16);}catch(Exception e){return fallback;}}
 @Override public int width(Minecraft mc){int w=1;for(var e:keys.get())if(e.isJsonObject()){var k=e.getAsJsonObject();w=Math.max(w,(int)(number(k,"x",0)+number(k,"width",16)));}return w;}
 @Override public int height(Minecraft mc){int h=1;for(var e:keys.get())if(e.isJsonObject()){var k=e.getAsJsonObject();h=Math.max(h,(int)(number(k,"y",0)+number(k,"height",16)));}return h;}
 @Override public void render(GuiGraphicsExtractor g,Minecraft mc){
  long handle=mc.getWindow().handle();
  for(var e:keys.get())try{var k=e.getAsJsonObject();if(k.has("enabled")&&!k.get("enabled").getAsBoolean())continue;
   int code=(int)number(k,"key",81),x=(int)number(k,"x",0),y=(int)number(k,"y",0),w=(int)number(k,"width",16),h=(int)number(k,"height",16);String kind=string(k,"kind","Keyboard");
   boolean down=kind.equals("Keyboard")?code>=32&&code<=GLFW.GLFW_KEY_LAST&&GLFW.glfwGetKey(handle,code)==GLFW.GLFW_PRESS:GLFW.glfwGetMouseButton(handle,kind.equals("Left mouse")?0:kind.equals("Right mouse")?1:2)==GLFW.GLFW_PRESS;
   int bg=themeColors.get()?(down?MonkeyClient.theme().accent:backgroundColor()):color(k,down?"pressed":"normal",down?0xFF7CC24A:0x80000000);
   g.fill(x,y,x+w,y+h,bg);
   if(k.has("border")&&k.get("border").getAsBoolean()){int c=themeColors.get()?borderColor():color(k,"borderColor",0xFFFFFFFF);g.fill(x,y,x+w,y+1,c);g.fill(x,y+h-1,x+w,y+h,c);g.fill(x,y,x+1,y+h,c);g.fill(x+w-1,y,x+w,y+h,c);}
   String label=string(k,"label","KEY");float s=(float)(number(k,"textSize",1)*textSize.get());g.pose().pushMatrix();g.pose().translate(x+(w-mc.font.width(label)*s)/2,y+(h-9*s)/2);g.pose().scale(s,s);g.text(mc.font,label,0,0,themeColors.get()?(down?MonkeyClient.theme().background:foregroundColor()):color(k,"textColor",0xFFFFFFFF),shadow.get());g.pose().popMatrix();
  }catch(RuntimeException ignored){}
 }
 public Module settingsFor(int index){
  JsonObject data=keys.get().get(index).getAsJsonObject();
  return new Module("key","Key appearance","Position is relative to the whole Keystrokes HUD",Category.HUD,true){
   {add(new StringSetting("label","Key label","KEY",24));add(new EnumSetting("kind","Input type","Keyboard",List.of("Keyboard","Left mouse","Right mouse","Middle mouse")));add(new KeySetting("key","Keyboard binding",81));
    add(new NumberSetting("x","X position",0,0,500,1));add(new NumberSetting("y","Y position",0,0,500,1));add(new NumberSetting("width","Width",16,10,200,1));add(new NumberSetting("height","Height",16,10,200,1));add(new NumberSetting("textSize","Text size",1,.5,3,.1));
    add(new ColorSetting("normal","Idle color",0x80000000));add(new ColorSetting("pressed","Pressed color",0xFF7CC24A));add(new ColorSetting("textColor","Text color",0xFFFFFFFF));add(new BoolSetting("border","Border",false));add(new ColorSetting("borderColor","Border color",0xFFFFFFFF));
    for(var s:settings())if(data.has(s.id))s.load(data.get(s.id));
   }
   @Override public void onSettingChanged(Setting<?> s){for(var setting:settings())data.add(setting.id,setting.save());}
   @Override public void onToggle(boolean on){onSettingChanged(enabled);}
   @Override public void resetAll(){super.resetAll();onSettingChanged(enabled);}
  };
 }
}
