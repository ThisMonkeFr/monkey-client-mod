package gg.monkeyclient.modules;
import gg.monkeyclient.module.Module;
import gg.monkeyclient.module.Category;
import gg.monkeyclient.module.setting.*;
import gg.monkeyclient.ui.ItemEditorScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.*;
import java.util.*;
public class ItemEditor extends Module {
 public final JsonSetting items=add(new JsonSetting("items","Item appearance overrides"));
 private final Map<String,Appearance> profiles=new HashMap<>();private boolean dirty=true;
 public record Appearance(float x,float y,float z,float rx,float ry,float rz,float sx,float sy,float sz,Identifier texture,String identity){
  public void transform(PoseStack pose){pose.translate(x,y,z);pose.mulPose(Axis.XP.rotationDegrees(rx));pose.mulPose(Axis.YP.rotationDegrees(ry));pose.mulPose(Axis.ZP.rotationDegrees(rz));pose.scale(sx,sy,sz);}
 }
 public static final ModelPart FLAT;
 static {var mesh=new MeshDefinition();mesh.getRoot().addOrReplaceChild("item",CubeListBuilder.create().texOffs(0,0).addBox(-8,-8,0,16,16,0),PartPose.ZERO);FLAT=LayerDefinition.create(mesh,32,16).bakeRoot();}
 public ItemEditor(){super("items","Item Editor","Search any item; first-person and dropped-item appearance",Category.VISUAL,false);}
 @Override public Screen editor(Screen parent){return new ItemEditorScreen(parent,this);}
 @Override public String editorLabel(){return "SEARCH & EDIT ITEMS";}
 public void dirty(){dirty=true;}
 @Override public void resetAll(){super.resetAll();dirty=true;}
 @Override public void onToggle(boolean on){dirty=true;}
 @Override public void onTick(){if(dirty)rebuild();}
 public static Path textureDir(){return FabricLoader.getInstance().getConfigDir().resolve("monkeyclient").resolve("textures");}
 public JsonObject record(String id){for(var e:items.get())if(e.isJsonObject()&&e.getAsJsonObject().has("id")&&id.equals(e.getAsJsonObject().get("id").getAsString()))return e.getAsJsonObject();var o=new JsonObject();o.addProperty("id",id);items.get().add(o);return o;}
 public Appearance appearance(String id,ItemDisplayContext context){return profiles.get(id+":"+((context==ItemDisplayContext.FIRST_PERSON_LEFT_HAND||context==ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)?"firstPerson":context==ItemDisplayContext.GROUND?"world":"none"));}
 private static float f(JsonObject o,String key,float fallback){try{float f=o.get(key).getAsFloat();return Float.isFinite(f)?f:fallback;}catch(Exception e){return fallback;}}
 private void rebuild(){
  dirty=false;profiles.clear();
  for(var e:items.get())try{var o=e.getAsJsonObject();String id=o.get("id").getAsString();
   for(String context:List.of("firstPerson","world")){if(!o.has(context))continue;var p=o.getAsJsonObject(context);if(p.has("enabled")&&!p.get("enabled").getAsBoolean())continue;
    Identifier texture=null;
    if(p.has("texture")&&!p.get("texture").getAsString().isBlank())texture=loadTexture(p.get("texture").getAsString());
    profiles.put(id+":"+context,new Appearance(f(p,"x",0),f(p,"y",0),f(p,"z",0),f(p,"rx",0),f(p,"ry",0),f(p,"rz",0),f(p,"sx",1),f(p,"sy",1),f(p,"sz",1),texture,p.toString()));
   }
  }catch(Exception ignored){}
 }
 private Identifier loadTexture(String name){
  try{Path dir=textureDir().toAbsolutePath().normalize();Files.createDirectories(dir);Path path=dir.resolve(name).normalize();if(!path.startsWith(dir)||!Files.isRegularFile(path)||Files.size(path)>4*1024*1024)return null;
   try(var in=Files.newInputStream(path);var image=NativeImage.read(in)){
    if(image.getWidth()!=image.getHeight()||image.getWidth()>1024)return null;
    int n=image.getWidth();NativeImage doubled=new NativeImage(n*2,n,true);
    for(int y=0;y<n;y++)for(int x=0;x<n;x++){int color=image.getPixel(x,y);doubled.setPixel(x,y,color);doubled.setPixel(x+n,y,color);}
    var id=Identifier.fromNamespaceAndPath("monkeyclient","dynamic/item/"+Integer.toUnsignedString(name.hashCode(),16));
    Minecraft.getInstance().getTextureManager().register(id,new DynamicTexture(()->"Monkey item "+name,doubled));return id;
   }
  }catch(Exception e){gg.monkeyclient.MonkeyClient.LOG.warn("Item texture {}: {}",name,e.toString());return null;}
 }
 public Module settingsFor(String id,String context){
  JsonObject record=record(id);if(!record.has(context))record.add(context,new JsonObject());JsonObject data=record.getAsJsonObject(context);
  return new Module("itemAppearance",context.toUpperCase(Locale.ROOT)+" — "+id,"PNG files: config/monkeyclient/textures; blank uses original model",Category.VISUAL,true){
   {add(new StringSetting("texture","PNG filename","",200));
    for(String axis:List.of("x","y","z"))add(new NumberSetting(axis,"Position "+axis.toUpperCase(Locale.ROOT),0,-4,4,.05));
    for(String axis:List.of("rx","ry","rz"))add(new NumberSetting(axis,"Rotation "+axis.substring(1).toUpperCase(Locale.ROOT),0,-180,180,1));
    for(String axis:List.of("sx","sy","sz"))add(new NumberSetting(axis,"Scale "+axis.substring(1).toUpperCase(Locale.ROOT),1,.05,5,.05));
    for(var s:settings())if(data.has(s.id))s.load(data.get(s.id));
   }
   @Override public void onSettingChanged(Setting<?> s){for(var setting:settings())data.add(setting.id,setting.save());dirty();}
   @Override public void onToggle(boolean on){onSettingChanged(enabled);}
   @Override public void resetAll(){super.resetAll();onSettingChanged(enabled);}
  };
 }
}
