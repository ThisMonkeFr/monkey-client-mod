package gg.monkeyclient.platform;
import java.nio.file.Path;
import java.util.List;
/** Forge adapter; reflection covers the instance and static ModList APIs. */
public final class PlatformLoader {
 private static final PlatformLoader INSTANCE=new PlatformLoader();
 public static PlatformLoader getInstance(){return INSTANCE;}
 public Path getConfigDir(){return net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get();}
 public boolean isModLoaded(String id){
  // Mixin plugins and Options run before the runtime ModList exists on 1.21.
  // LoadingModList is available during discovery and throughout client startup.
  try{
   Class<?> c=Class.forName("net.minecraftforge.fml.loading.LoadingModList");
   var method=c.getMethod("getModFileById",String.class);
   boolean isStatic=java.lang.reflect.Modifier.isStatic(method.getModifiers());
   Object receiver=isStatic?null:c.getMethod("get").invoke(null);
   if(!isStatic&&receiver==null)return false;
   return method.invoke(receiver,id)!=null;
  }catch(ReflectiveOperationException e){return false;}
 }
 public <T> List<T> getEntrypoints(String name,Class<T> type){return List.of();}
}
