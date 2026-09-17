package gg.monkeyclient.modules;
import gg.monkeyclient.module.Category;
import gg.monkeyclient.module.Module;
import gg.monkeyclient.module.setting.*;
import net.minecraft.client.Minecraft;
import java.util.List;
public class ToggleSprint extends Module {
 public final EnumSetting mode=add(new EnumSetting("mode","Sprint mode","Always",List.of("Toggle","Always")));
 public final BoolSetting onlyForward=add(new BoolSetting("onlyForward","Forward movement only",true));
 public final BoolSetting keepInAir=add(new BoolSetting("keepInAir","Keep sprint in air",true));
 public final NumberSetting flight=add(new NumberSetting("flight","Creative flight speed",1,1,100,.1));
 public final EnumSetting flightMode=add(new EnumSetting("flightMode","Flight boost","Hold key",List.of("Hold key","Automatic")));
 public final KeySetting flightKey=add(new KeySetting("flightKey","Hold for flight boost",org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_ALT));
 private net.minecraft.client.player.LocalPlayer recoveredPlayer;
 private boolean pressed,toggled;private net.minecraft.client.player.LocalPlayer flightPlayer;private float originalFlight,lastApplied;
 private net.minecraft.server.MinecraftServer flightServer;
 private float lastServerSpeed=Float.NaN;private volatile long flightRevision;
 public ToggleSprint(){super("sprint","Sprint","Toggle, automatic sprint and creative flight",Category.UTILITY,false);}
 @Override public void onTick(){
  var mc=Minecraft.getInstance();var p=mc.player;if(p==null){restoreFlight();return;}
  if(p.isCreative()){
   if(flightPlayer!=p){restoreFlight();flightPlayer=p;flightServer=mc.getSingleplayerServer();originalFlight=.05f;lastApplied=originalFlight;}
   boolean boost=flightMode.get().equals("Automatic")||(mc.gui.screen()==null&&flightKey.down(mc));
   lastApplied=flightSpeed(boost?flight.get():1);p.getAbilities().setFlyingSpeed(lastApplied);
   syncServerFlight(lastApplied);
  }else restoreFlight();
  boolean down=mc.options.keySprint.isDown();if(mc.gui.screen()!=null){pressed=down;return;}
  if(down&&!pressed)toggled=!toggled;pressed=down;
  boolean wanted=mode.get().equals("Always")||toggled;
  boolean moving=p.input.getMoveVector().lengthSquared()>.01f;
  if(!moving||(onlyForward.get()&&p.input.getMoveVector().y<=0)||(!keepInAir.get()&&!p.onGround()))return;
  if(p.isUsingItem()||(!p.isCreative()&&p.getFoodData().getFoodLevel()<=6)||p.isFallFlying()||p.isCrouching())return;
  if(wanted)p.setSprinting(true);else if(mode.get().equals("Toggle"))p.setSprinting(false);
 }
 public static float flightSpeed(double multiplier){return .05f*(float)(Double.isFinite(multiplier)?Math.max(1,Math.min(100,multiplier)):1);}
 public void recoverSavedFlight(Minecraft mc){
  var p=mc.player;if(p==recoveredPlayer)return;recoveredPlayer=p;
  if(p!=null&&p.isCreative()&&mc.getSingleplayerServer()!=null){p.getAbilities().setFlyingSpeed(.05f);var server=mc.getSingleplayerServer();var id=p.getUUID();server.execute(()->{var sp=server.getPlayerList().getPlayer(id);if(sp!=null&&sp.isCreative())sp.getAbilities().setFlyingSpeed(.05f);});}
 }
 private void syncServerFlight(float speed){
  if(flightServer==null||lastServerSpeed==speed)return;
  var server=flightServer;var uuid=flightPlayer.getUUID();long revision=++flightRevision;
  lastServerSpeed=speed;
  server.execute(()->{if(revision!=flightRevision)return;var player=server.getPlayerList().getPlayer(uuid);if(player!=null&&player.isCreative())player.getAbilities().setFlyingSpeed(speed);});
 }
 private void restoreFlight(){if(flightPlayer!=null){
  var player=flightPlayer;float original=originalFlight;var server=flightServer;++flightRevision;
  player.getAbilities().setFlyingSpeed(original);
  if(server!=null){var uuid=player.getUUID();float applied=lastServerSpeed;server.execute(()->{var p=server.getPlayerList().getPlayer(uuid);if(p!=null&&Math.abs(p.getAbilities().getFlyingSpeed()-applied)<.0001)p.getAbilities().setFlyingSpeed(original);});}
  flightPlayer=null;flightServer=null;lastServerSpeed=Float.NaN;
 }}
 @Override public void onToggle(boolean on){toggled=pressed=false;if(!on){restoreFlight();var p=Minecraft.getInstance().player;if(p!=null)p.setSprinting(false);}}
}
