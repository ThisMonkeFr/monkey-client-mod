package gg.monkeyclient.modules;
import gg.monkeyclient.module.Module;
import gg.monkeyclient.module.Category;
import gg.monkeyclient.module.setting.*;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.world.level.material.FogType;
import net.minecraft.client.Minecraft;
import net.fabricmc.loader.api.FabricLoader;
public class FogEditor extends Module {
 public final BoolSetting extended=add(new BoolSetting("extended","Render cached chunks beyond the server",false));
 public final NumberSetting distance=add(new NumberSetting("distance","Cached render distance (chunks)",32,2,1000,1));
 public final BoolSetting followDistance=add(new BoolSetting("followDistance","Fog follows cached render distance",true));
 public final BoolSetting sky=add(new BoolSetting("sky","Sky",true)),clouds=add(new BoolSetting("clouds","Clouds",true)),sun=add(new BoolSetting("sun","Sun",true)),moon=add(new BoolSetting("moon","Moon",true)),stars=add(new BoolSetting("stars","Stars",true)),sunrise=add(new BoolSetting("sunrise","Sunrise and sunset",true));
 public final BoolSetting skyColorOn=add(new BoolSetting("skyColorOn","Custom sky color",false)),cloudColorOn=add(new BoolSetting("cloudColorOn","Custom cloud color",false));
 public final ColorSetting skyColor=add(new ColorSetting("skyColor","Sky color",0xFF78A7FF)),cloudColor=add(new ColorSetting("cloudColor","Cloud color",0xFFFFFFFF));
 public final NumberSetting cloudHeight=add(new NumberSetting("cloudHeight","Cloud height offset",0,-384,512,1)),sunScale=add(new NumberSetting("sunScale","Sun size",1,.1,5,.1)),moonScale=add(new NumberSetting("moonScale","Moon size",1,.1,5,.1));
 private Integer lastModuleDistance, lastVideoDistance;
 public final BoolSetting world=add(new BoolSetting("world","World fog",true)),water=add(new BoolSetting("water","Water fog",true)),lava=add(new BoolSetting("lava","Lava fog",true));
 public final BoolSetting customColor=add(new BoolSetting("customColor","Override world fog color",false)),waterColorOn=add(new BoolSetting("waterColorOn","Override water fog color",false)),lavaColorOn=add(new BoolSetting("lavaColorOn","Override lava fog color",false));
 public final ColorSetting worldColor=add(new ColorSetting("worldColor","World fog color",0xFFC0D8FF)),waterColor=add(new ColorSetting("waterColor","Water fog color",0xFF2459BB)),lavaColor=add(new ColorSetting("lavaColor","Lava fog color",0xFFFF6500));
 public final NumberSetting start=add(new NumberSetting("start","World start (blocks)",64,0,32000,1)),end=add(new NumberSetting("end","World end (blocks)",256,1,32000,1));
 public final NumberSetting waterStart=add(new NumberSetting("waterStart","Water start (blocks)",0,0,1024,.5)),waterEnd=add(new NumberSetting("waterEnd","Water end (blocks)",96,1,2048,.5));
 public final NumberSetting lavaStart=add(new NumberSetting("lavaStart","Lava start (blocks)",0,0,1024,.5)),lavaEnd=add(new NumberSetting("lavaEnd","Lava end (blocks)",4,1,2048,.5));
 public FogEditor(){super("fog","World Editor","Cached terrain, sky, celestial bodies, clouds and fog",Category.VISUAL,false);}
 public boolean extendedActive(){return isEnabled()&&extended.get();}
 @Override public void onTick(){
  if(!extendedActive()){lastModuleDistance=lastVideoDistance=null;return;}
  var option=Minecraft.getInstance().options.renderDistance();
  int video=option.get(), requested=distance.getInt();
  // A change in Video Settings takes precedence. Never reapply a stale module
  // value every tick, and never restore a value the player has since changed.
  if(lastVideoDistance!=null && video!=lastVideoDistance){
   distance.set((double)video);gg.monkeyclient.MonkeyClient.saveConfig();
  }else if(lastModuleDistance==null || requested!=lastModuleDistance){
   int value=Math.min(requested,FabricLoader.getInstance().isModLoaded("sodium")?1000:32);
   if(video!=value)option.set(value);
   distance.set((double)option.get());
  }
  lastVideoDistance=option.get();lastModuleDistance=distance.getInt();
 }
 @Override public void onToggle(boolean enabled){lastModuleDistance=lastVideoDistance=null;}
 public void apply(FogData f,FogType type){
  boolean wet=type==FogType.WATER,hot=type==FogType.LAVA;
  boolean enabled=(wet?water:hot?lava:world).get();
  float a=(wet?waterStart:hot?lavaStart:start).getFloat(),b=Math.max(a+.1f,(wet?waterEnd:hot?lavaEnd:end).getFloat());
  if(!wet&&!hot&&extendedActive()&&followDistance.get()){b=Minecraft.getInstance().options.renderDistance().get()*16f;a=b*.75f;}
  if(!enabled){a=1e6f;b=2e6f;}
  f.environmentalStart=a;f.renderDistanceStart=a;f.environmentalEnd=b;f.renderDistanceEnd=b;f.skyEnd=b;f.cloudEnd=b;
  if((wet?waterColorOn:hot?lavaColorOn:customColor).get()){int c=(wet?waterColor:hot?lavaColor:worldColor).resolve();f.color.set((c>>16&255)/255f,(c>>8&255)/255f,(c&255)/255f,1);}
 }
}
