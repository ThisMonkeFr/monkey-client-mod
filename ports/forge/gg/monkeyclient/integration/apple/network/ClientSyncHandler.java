package gg.monkeyclient.integration.apple.network;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import gg.monkeyclient.integration.apple.helpers.ExhaustionHelper;
import gg.monkeyclient.platform.PlatformLoader;
public final class ClientSyncHandler {
 public static boolean naturalRegeneration=true;
 public static boolean accept(CustomPacketPayload payload){
  if(PlatformLoader.getInstance().isModLoaded("appleskin"))return false;
  if(!(payload instanceof ExhaustionSyncPayload||payload instanceof SaturationSyncPayload||payload instanceof NaturalRegenerationSyncPayload))return false;
  var mc=Minecraft.getInstance();mc.execute(()->{if(mc.player==null)return;if(payload instanceof ExhaustionSyncPayload p)ExhaustionHelper.setExhaustion(mc.player,p.getExhaustion());if(payload instanceof SaturationSyncPayload p)mc.player.getFoodData().setSaturation(p.getSaturation());if(payload instanceof NaturalRegenerationSyncPayload p)naturalRegeneration=p.naturalRegeneration();});return true;
 }
}
