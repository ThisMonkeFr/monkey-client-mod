package gg.monkeyclient.platform;
import java.nio.file.Path;
import java.util.List;
/** Forge adapter; reflection covers the instance and static ModList APIs. */
public final class PlatformLoader {
 private static final PlatformLoader INSTANCE=new PlatformLoader();
 public static PlatformLoader getInstance(){return INSTANCE;}
 public Path getConfigDir(){return net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get();}
 public boolean isModLoaded(String id){try{Class<?> c=Class.forName("net.minecraftforge.fml.ModList");var method=c.getMethod("isLoaded",String.class);Object receiver=java.lang.reflect.Modifier.isStatic(method.getModifiers())?null:c.getMethod("get").invoke(null);return Boolean.TRUE.equals(method.invoke(receiver,id));}catch(ReflectiveOperationException e){return false;}}
 public <T> List<T> getEntrypoints(String name,Class<T> type){return List.of();}
}
