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
 private static ClientAsset.Texture body;private static net.minecraft.world.entity.player.PlayerModelType model;
 private static boolean loaded;private static UUID owner;private static ClientAsset.Texture cape,elytra;
 public static PlayerSkin apply(UUID uuid,PlayerSkin original){
  if(!loaded)load();
  return uuid.equals(owner)&&(cape!=null||body!=null)?new PlayerSkin(body!=null?body:original.body(),cape!=null?cape:original.cape(),elytra!=null?elytra:original.elytra(),model!=null?model:original.model(),original.secure()):original;
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
   installCape(image);
  }catch(Exception e){MonkeyClient.LOG.warn("Could not load launcher cape: {}",e.toString());}
 }
 private static void installCape(NativeImage image)throws Exception{
   Identifier texture=Identifier.fromNamespaceAndPath("monkeyclient","dynamic/launcher_cape");
   // The cape atlas has opaque pixels outside the wing silhouette. Applying it
   // directly fills the elytra's transparent cutouts and makes the wings blocky.
   // Keep vanilla's wing mask and geometry while sampling the cape's colours.
   try(var in=Minecraft.getInstance().getResourceManager().getResourceOrThrow(Identifier.withDefaultNamespace("textures/entity/equipment/wings/elytra.png")).open();var mask=NativeImage.read(in)){
    NativeImage wings=new NativeImage(image.getWidth(),image.getHeight(),true);
    for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++){
     int color=image.getPixel(x,y),alpha=mask.getPixel(x*mask.getWidth()/image.getWidth(),y*mask.getHeight()/image.getHeight())>>>24;
     wings.setPixel(x,y,((color>>>24)*alpha/255)<<24|(color&0xFFFFFF));
    }
    Identifier wingTexture=Identifier.fromNamespaceAndPath("monkeyclient","dynamic/launcher_elytra");
    Minecraft.getInstance().getTextureManager().register(wingTexture,new DynamicTexture(()->"Monkey Client elytra",wings));
    elytra=new ClientAsset.ResourceTexture(wingTexture,wingTexture);
   }
   Minecraft.getInstance().getTextureManager().register(texture,new DynamicTexture(()->"Monkey Client cape",image));
   cape=new ClientAsset.ResourceTexture(texture,texture);
 }
 public static void equip(String kind,String data,boolean slim,boolean equipped){
  gg.monkeyclient.integration.LauncherBridge.WORK.execute(()->{
   try{NativeImage decoded=equipped?NativeImage.read(java.util.Base64.getDecoder().decode(data.split(",",2)[1])):null;NativeImage image=decoded!=null&&kind.equals("skin")?SkinPixels.modern(decoded):decoded;
    Minecraft.getInstance().execute(()->{var mc=Minecraft.getInstance();if(mc.player==null){if(image!=null)image.close();return;}if(!loaded)load();owner=mc.player.getUUID();loaded=true;
     try{if(kind.equals("cape")){if(image==null){cape=null;elytra=null;mc.getTextureManager().release(Identifier.fromNamespaceAndPath("monkeyclient","dynamic/launcher_cape"));mc.getTextureManager().release(Identifier.fromNamespaceAndPath("monkeyclient","dynamic/launcher_elytra"));}else installCape(image);}
      else if(image!=null){Identifier id=Identifier.fromNamespaceAndPath("monkeyclient","dynamic/local_skin");mc.getTextureManager().register(id,new DynamicTexture(()->"Monkey Client skin",image));body=new ClientAsset.ResourceTexture(id,id);model=slim?net.minecraft.world.entity.player.PlayerModelType.SLIM:net.minecraft.world.entity.player.PlayerModelType.WIDE;}
     }catch(Exception e){if(image!=null)image.close();MonkeyClient.LOG.warn("Could not apply cosmetic",e);}
    });
   }catch(Exception e){MonkeyClient.LOG.warn("Could not decode cosmetic",e);}
  });
 }

}
