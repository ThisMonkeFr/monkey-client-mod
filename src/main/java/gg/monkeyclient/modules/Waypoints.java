package gg.monkeyclient.modules;
import gg.monkeyclient.module.Module;
import gg.monkeyclient.module.Category;
import gg.monkeyclient.module.setting.*;
import gg.monkeyclient.MonkeyClient;
import gg.monkeyclient.ui.WaypointEditorScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.phys.Vec3;
import com.google.gson.*;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import gg.monkeyclient.render.WaypointIcons;
import java.util.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.phys.shapes.Shapes;
public class Waypoints extends Module {
 public final JsonSetting points=add(new JsonSetting("points","Saved waypoints"));
 public final NumberSetting maxDistance=add(new NumberSetting("maxDistance","Maximum distance",10000,16,100000,16)),scale=add(new NumberSetting("scale","Label scale",1,.5,3,.1));
 public final BoolSetting distance=add(new BoolSetting("distance","Show distance",true)),background=add(new BoolSetting("background","Label background",true)),themed=add(new BoolSetting("themed","Match theme color",true));
 public final ColorSetting color=add(new ColorSetting("color","Waypoint color",0xFF7CC24A));
 public final KeySetting key=add(new KeySetting("key","Hold to paint a waypoint area",org.lwjgl.glfw.GLFW.GLFW_KEY_B));
 public final KeySetting managerKey=add(new KeySetting("managerKey","Open waypoint manager",org.lwjgl.glfw.GLFW.GLFW_KEY_M));
 public final EnumSetting paintMode=add(new EnumSetting("paintMode","Paint blocks","Underfoot",List.of("Underfoot","Crosshair")));
 public final BoolSetting areas=add(new BoolSetting("areas","Highlight saved blocks",true));
 private final Set<BlockPos> painting=new LinkedHashSet<>();
 private String paintingWorld="",paintingDimension="";
 public JsonObject draft;
 private boolean down,managerDown;
 private final Matrix4f matrix=new Matrix4f();private final Vector4f clip=new Vector4f();
 public Waypoints(){super("waypoints","Waypoints","Named markers saved separately for each world and dimension",Category.UTILITY,false);}
 public static String world(Minecraft mc){
  if(mc.getCurrentServer()!=null)return "server:"+mc.getCurrentServer().ip;
  return mc.getSingleplayerServer()!=null?"local:"+mc.getSingleplayerServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).toAbsolutePath().normalize():"";
 }
 public static String dimension(Minecraft mc){return mc.level==null?"":mc.level.dimension().identifier().toString();}
 public void addHere(String name){
  var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null)return;
  points.get().add(point(name,mc.player.getX(),mc.player.getY(),mc.player.getZ()));
 }
 private JsonObject point(String name,double x,double y,double z){var mc=Minecraft.getInstance();JsonObject p=new JsonObject();p.addProperty("name",name);p.addProperty("x",x);p.addProperty("y",y);p.addProperty("z",z);p.addProperty("world",world(mc));p.addProperty("dimension",dimension(mc));p.addProperty("visible",true);p.addProperty("icon","grass");p.addProperty("color",themed.get()?MonkeyClient.theme().accent:color.resolve());return p;}
 public void saveDraft(){if(draft!=null){points.get().add(draft);draft=null;MonkeyClient.saveConfig();}}
 public void discardDraft(){draft=null;painting.clear();}
 public int pointColor(JsonObject p){return p.has("color")?p.get("color").getAsInt():themed.get()?MonkeyClient.theme().accent:color.resolve();}
 public int blockColor(JsonObject p){return p.has("blockColor")?p.get("blockColor").getAsInt():pointColor(p);}
 private boolean current(JsonObject p,Minecraft mc){return p.get("world").getAsString().equals(world(mc))&&p.get("dimension").getAsString().equals(dimension(mc));}
 private void finishPainting(){if(painting.isEmpty())return;double x=0,y=0,z=0;JsonArray blocks=new JsonArray();for(var pos:painting){x+=pos.getX()+.5;y+=pos.getY()+1;z+=pos.getZ()+.5;blocks.add(pos.asLong());}draft=point("Waypoint "+(points.get().size()+1),x/painting.size(),y/painting.size(),z/painting.size());draft.add("blocks",blocks);painting.clear();Minecraft.getInstance().setScreenAndShow(new WaypointEditorScreen(null,this));}
 @Override public void onTick(){
  var mc=Minecraft.getInstance();boolean pressed=key.down(mc);
  boolean managerPressed=managerKey.down(mc);
  if(managerPressed&&!managerDown&&mc.player!=null&&mc.gui.screen()==null){managerDown=true;mc.setScreenAndShow(new WaypointEditorScreen(null,this));return;}
  managerDown=managerPressed;
  if(mc.player==null||mc.level==null||mc.gui.screen()!=null){down=false;painting.clear();return;}
  if(pressed&&!down){painting.clear();paintingWorld=world(mc);paintingDimension=dimension(mc);}
  if(!paintingWorld.equals(world(mc))||!paintingDimension.equals(dimension(mc))){painting.clear();down=false;return;}
  if(pressed&&painting.size()<4096){BlockPos pos=null;if(paintMode.get().equals("Crosshair")&&mc.hitResult instanceof net.minecraft.world.phys.BlockHitResult hit&&hit.getType()==net.minecraft.world.phys.HitResult.Type.BLOCK)pos=hit.getBlockPos();else if(paintMode.get().equals("Underfoot"))pos=BlockPos.containing(mc.player.getX(),mc.player.getY()-.02,mc.player.getZ());if(pos!=null&&!mc.level.getBlockState(pos).isAir())painting.add(pos.immutable());}
  if(!pressed&&down)finishPainting();down=pressed;
 }
 @Override public void onToggle(boolean enabled){if(!enabled){painting.clear();down=false;}}
 @Override public Screen editor(Screen parent){return new WaypointEditorScreen(parent,this);}
 @Override public String editorLabel(){return "MANAGE WAYPOINTS";}
 public void draw(GuiGraphicsExtractor g,Minecraft mc){
  if(mc.player==null||mc.level==null)return;
  var camera=mc.gameRenderer.mainCamera();var eye=camera.position();camera.getViewRotationProjectionMatrix(matrix);
  String world=world(mc),dim=dimension(mc);int sw=mc.getWindow().getGuiScaledWidth(),sh=mc.getWindow().getGuiScaledHeight();
  for(var element:points.get())try{
   JsonObject p=element.getAsJsonObject();if(!p.get("world").getAsString().equals(world)||!p.get("dimension").getAsString().equals(dim)||!p.get("visible").getAsBoolean()||(p.has("showName")&&!p.get("showName").getAsBoolean()))continue;
   double dx=p.get("x").getAsDouble()-eye.x,dy=p.get("y").getAsDouble()-eye.y,dz=p.get("z").getAsDouble()-eye.z,dist=Math.sqrt(dx*dx+dy*dy+dz*dz);
   if(dist>maxDistance.get())continue;
   clip.set((float)dx,(float)dy,(float)dz,1).mul(matrix);if(clip.w<=0)continue;
   float px=(clip.x/clip.w*.5f+.5f)*sw,py=(.5f-clip.y/clip.w*.5f)*sh;if(px<0||px>sw||py<0||py>sh)continue;
   Component label=Component.empty().append(WaypointIcons.text(p.has("icon")?p.get("icon").getAsString():"monkey")).append(" ").append(Component.literal(p.get("name").getAsString()+(distance.get()?" ["+Math.round(dist)+"m]":"")).withColor(pointColor(p)&0xFFFFFF));int w=mc.font.width(label);
   g.pose().pushMatrix();g.pose().translate(px,py);g.pose().scale(scale.getFloat(),scale.getFloat());
   if(background.get())g.fill(-w/2-3,-3,w/2+3,12,0xA0000000);
   g.text(mc.font,label,-w/2,0,0xFFFFFFFF,true);g.pose().popMatrix();
  }catch(RuntimeException ignored){}
  if(down){String hint=painting.size()+" blocks selected · release "+key.display()+" to name and save";g.centeredText(mc.font,hint,sw/2,sh-68,MonkeyClient.theme().text);}
 }
 public void submitAreas(LevelRenderState state,SubmitNodeCollector collector){
  var mc=Minecraft.getInstance();if(mc.level==null)return;var eye=state.cameraRenderState.pos;
  for(var pos:List.copyOf(painting))submitBlock(pos,eye,collector,MonkeyClient.theme().accent);
  if(draft!=null&&current(draft,mc))submitPoint(draft,eye,collector);
  if(areas.get())for(var element:points.get())try{var p=element.getAsJsonObject();if(current(p,mc)&&p.get("visible").getAsBoolean())submitPoint(p,eye,collector);}catch(RuntimeException ignored){}
 }
 private void submitPoint(JsonObject p,Vec3 eye,SubmitNodeCollector collector){if(p.has("showBlocks")&&!p.get("showBlocks").getAsBoolean())return;if(p.has("blocks"))for(var v:p.getAsJsonArray("blocks"))submitBlock(BlockPos.of(v.getAsLong()),eye,collector,blockColor(p));}
 private void submitBlock(BlockPos pos,Vec3 eye,SubmitNodeCollector collector,int tint){
  if(pos.distToCenterSqr(eye.x,eye.y,eye.z)>128*128)return;
  PoseStack pose=new PoseStack();pose.translate(pos.getX()-eye.x,pos.getY()-eye.y,pos.getZ()-eye.z);
  collector.submitShapeOutline(pose,Shapes.box(-.003,-.003,-.003,1.003,1.003,1.003),RenderTypes.linesTranslucent(),0xC0000000|(tint&0xFFFFFF),2f,false);
  collector.submitCustomGeometry(pose,RenderTypes.debugQuads(),(matrix,vertices)->{
   int fill=0x48000000|(tint&0xFFFFFF);vertices.addVertex(matrix,0,1.006f,0).setColor(fill);vertices.addVertex(matrix,0,1.006f,1).setColor(fill);vertices.addVertex(matrix,1,1.006f,1).setColor(fill);vertices.addVertex(matrix,1,1.006f,0).setColor(fill);
  });
 }
}
