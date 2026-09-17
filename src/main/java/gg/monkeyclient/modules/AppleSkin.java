package gg.monkeyclient.modules;
import gg.monkeyclient.MonkeyClient;
import gg.monkeyclient.module.Module;
import gg.monkeyclient.module.Category;
import gg.monkeyclient.module.setting.*;
import gg.monkeyclient.integration.apple.ModConfig;
import gg.monkeyclient.integration.apple.client.*;
import gg.monkeyclient.integration.apple.network.*;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
/** Native AppleSkin overlays, adapted from squeek502's public-domain implementation. */
public class AppleSkin extends Module {
 public final BoolSetting saturation=add(new BoolSetting("saturation","Saturation outline",true));
 public final BoolSetting exhaustion=add(new BoolSetting("exhaustion","Exhaustion underlay",true));
 public final BoolSetting food=add(new BoolSetting("food","Held food preview",true));
 public final BoolSetting health=add(new BoolSetting("health","Estimated healing preview",true));
 public final BoolSetting tooltip=add(new BoolSetting("tooltip","Food tooltip",true));
 public final BoolSetting offhand=add(new BoolSetting("offhand","Preview offhand food",true));
 private static boolean external;
 public AppleSkin(){super("appleskin","AppleSkin","Native hunger, saturation, exhaustion and healing previews",Category.UTILITY,false);}
 public static void initialize(){
  external=FabricLoader.getInstance().isModLoaded("appleskin");
  HUDOverlayHandler.init();TooltipOverlayHandler.init();
  if(!external){
   PayloadTypeRegistry.clientboundPlay().register(ExhaustionSyncPayload.ID,ExhaustionSyncPayload.CODEC);
   PayloadTypeRegistry.clientboundPlay().register(SaturationSyncPayload.ID,SaturationSyncPayload.CODEC);
   PayloadTypeRegistry.clientboundPlay().register(NaturalRegenerationSyncPayload.ID,NaturalRegenerationSyncPayload.CODEC);
   ClientSyncHandler.init();
  }
 }
 public static boolean active(){
  if(!MonkeyClient.ready()||external)return false;
  var m=MonkeyClient.modules().get(AppleSkin.class);var c=ModConfig.INSTANCE;
  c.showSaturationHudOverlay=m.saturation.get();c.showFoodExhaustionHudUnderlay=m.exhaustion.get();
  c.showFoodValuesHudOverlay=m.food.get();c.showFoodHealthHudOverlay=m.health.get();
  c.showFoodValuesInTooltip=c.showFoodValuesInTooltipAlways=m.tooltip.get();c.showFoodValuesHudOverlayWhenOffhand=m.offhand.get();
  return m.isEnabled();
 }
 @Override public void onTick(){
  if(!active())return;HUDOverlayHandler.INSTANCE.onClientTick();
  var mc=Minecraft.getInstance();var server=mc.getSingleplayerServer();
  if(server!=null&&mc.player!=null){var clientPlayer=mc.player;var id=clientPlayer.getUUID();server.execute(()->{
   var player=server.getPlayerList().getPlayer(id);if(player==null)return;
   float sat=player.getFoodData().getSaturationLevel();float ex=gg.monkeyclient.integration.apple.helpers.ExhaustionHelper.getExhaustion(player);
   boolean regen=player.level().getGameRules().get(net.minecraft.world.level.gamerules.GameRules.NATURAL_HEALTH_REGENERATION);
   mc.execute(()->{if(mc.player==clientPlayer){clientPlayer.getFoodData().setSaturation(sat);gg.monkeyclient.integration.apple.helpers.ExhaustionHelper.setExhaustion(clientPlayer,ex);ClientSyncHandler.naturalRegeneration=regen;}});
  });}
 }
}
