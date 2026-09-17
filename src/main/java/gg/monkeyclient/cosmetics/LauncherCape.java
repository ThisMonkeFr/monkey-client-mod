package gg.monkeyclient.cosmetics;
import gg.monkeyclient.MonkeyClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;
import net.fabricmc.loader.api.FabricLoader;
import com.mojang.blaze3d.platform.NativeImage;
import com.google.gson.*;
import java.nio.file.*;
import java.util.UUID;
/** Local launcher cape, using vanilla PlayerSkin and CapeLayer for all animation. */
public final class LauncherCape {
 private static boolean loaded;private static UUID owner;private static ClientAsset.Texture cape;
 public static PlayerSkin apply(UUID uuid,PlayerSkin original){
  if(!loaded)load();
  return cape!=null&&uuid.equals(owner)?new PlayerSkin(original.body(),cape,original.elytra(),original.model(),original.secure()):original;
 }
 private static void load(){
  loaded=true;
  Path dir=FabricLoader.getInstance().getConfigDir().resolve("monkeyclient");
  try{
   Path manifest=dir.resolve("launcher.json");if(!Files.isRegularFile(manifest))return;
   JsonObject j;try(var r=Files.newBufferedReader(manifest)){j=JsonParser.parseReader(r).getAsJsonObject();}
   if(!j.has("cape")||j.get("cape").isJsonNull())return;
   String id=j.get("uuid").getAsString().replace("-","");
   owner=UUID.fromString(id.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})","$1-$2-$3-$4-$5"));
   Path file=dir.resolve(j.get("cape").getAsString()).normalize();if(!file.startsWith(dir.normalize())||Files.size(file)>4*1024*1024)return;
   NativeImage image;try(var in=Files.newInputStream(file)){image=NativeImage.read(in);}
   if(image.getWidth()!=image.getHeight()*2||image.getWidth()>2048){image.close();return;}
   Identifier texture=Identifier.fromNamespaceAndPath("monkeyclient","dynamic/launcher_cape");
   Minecraft.getInstance().getTextureManager().register(texture,new DynamicTexture(()->"Monkey Client cape",image));
   cape=new ClientAsset.ResourceTexture(texture,texture);
  }catch(Exception e){MonkeyClient.LOG.warn("Could not load launcher cape: {}",e.toString());}
 }
}
