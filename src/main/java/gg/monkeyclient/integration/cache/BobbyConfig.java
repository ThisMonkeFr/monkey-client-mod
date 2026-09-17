package gg.monkeyclient.integration.cache;
import net.fabricmc.loader.api.FabricLoader;
public final class BobbyConfig {
 public static final BobbyConfig DEFAULT=new BobbyConfig();
 public boolean isEnabled(){return Bobby.getInstance().isEnabled();}
 public boolean isNoBlockEntities(){return true;}
 public boolean isDynamicMultiWorld(){return false;}
 public boolean isTaintFakeChunks(){return false;}
 public int getUnloadDelaySecs(){return isEnabled()?5:0;}
 public int getDeleteUnusedRegionsAfterDays(){return -1;}
 public int getMaxRenderDistance(){return FabricLoader.getInstance().isModLoaded("sodium")?1000:32;}
 public int getViewDistanceOverwrite(){return isEnabled()?12:0;}
}
