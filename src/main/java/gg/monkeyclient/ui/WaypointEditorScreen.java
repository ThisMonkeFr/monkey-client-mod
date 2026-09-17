package gg.monkeyclient.ui;
import gg.monkeyclient.MonkeyClient;
import gg.monkeyclient.modules.Waypoints;
import gg.monkeyclient.module.setting.ColorSetting;
import gg.monkeyclient.render.WaypointIcons;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import com.google.gson.*;
import java.util.*;
/** Compact list + detail manager. Painted blocks remain a draft until saved. */
public class WaypointEditorScreen extends Screen {
 private static final List<String> ICONS=WaypointIcons.NAMES;
 private final Screen parent;private final Waypoints module;private int selected=-1,scroll,left,top,pw,ph,rail,right,rw;
 private boolean appearance;private String query="";private boolean currentOnly=true;private List<Integer> filtered=List.of();
 public WaypointEditorScreen(Screen parent,Waypoints module){super(Component.literal("Waypoints"));this.parent=parent;this.module=module;selected=module.draft!=null?-2:module.points.get().isEmpty()?-1:0;}
 private JsonObject point(){return selected==-2?module.draft:selected>=0&&selected<module.points.get().size()?module.points.get().get(selected).getAsJsonObject():null;}
 private MenuButton button(int x,int y,int w,String text,Runnable action){return addRenderableWidget(new MenuButton(x,y,w,20,text,action));}
 @Override protected void init(){
  clearWidgets();pw=Math.min(620,width-20);ph=Math.min(350,height-20);left=(width-pw)/2;top=(height-ph)/2;rail=Math.max(120,pw/3);right=left+rail+16;rw=pw-rail-30;
  button(left+pw-68,top+10,56,"Done",this::onClose);
  var search=addRenderableWidget(new EditBox(font,left+10,top+42,rail-20,20,Component.literal("Search waypoints")));search.setHint(Component.literal("Search waypoints"));search.setValue(query);
  search.setResponder(value->{query=value;scroll=0;init();for(var widget:children())if(widget instanceof EditBox e&&e.getX()==left+10){setFocused(e);e.setFocused(true);e.setCursorPosition(value.length());break;}});
  button(left+10,top+68,rail-20,currentOnly?"This world":"All worlds",()->{currentOnly=!currentOnly;scroll=0;init();});
  ArrayList<Integer> indexes=new ArrayList<>();if(module.draft!=null)indexes.add(-2);
  for(int i=0;i<module.points.get().size();i++)try{var p=module.points.get().get(i).getAsJsonObject();if(!p.get("name").getAsString().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT)))continue;if(currentOnly&&(!p.get("world").getAsString().equals(Waypoints.world(minecraft))||!p.get("dimension").getAsString().equals(Waypoints.dimension(minecraft))))continue;indexes.add(i);}catch(RuntimeException ignored){}
  filtered=indexes;int rows=Math.max(1,(ph-138)/26);scroll=Math.max(0,Math.min(scroll,Math.max(0,filtered.size()-rows)));
  for(int i=scroll;i<Math.min(filtered.size(),scroll+rows);i++){int index=filtered.get(i);var p=index==-2?module.draft:module.points.get().get(index).getAsJsonObject();var b=addRenderableWidget(new MenuButton(left+10,top+98+(i-scroll)*26,rail-20,22,"",()->{selected=index;init();},()->selected==index));b.setMessage(Component.empty().append(WaypointIcons.text(p.has("icon")?p.get("icon").getAsString():"monkey")).append(" ").append(index==-2?"Unsaved area":p.get("name").getAsString()));}
  button(left+10,top+ph-30,rail-20,"+ Current position",()->{module.addHere("Waypoint "+(module.points.get().size()+1));selected=module.points.get().size()-1;init();});
  JsonObject p=point();if(p==null)return;
  boolean compact=ph<310;
  if(compact)button(right,top+10,Math.min(105,rw-62),appearance?"Position":"Appearance",()->{appearance=!appearance;init();});
  if(!compact||!appearance){
  var name=addRenderableWidget(new EditBox(font,right,top+57,rw,20,Component.literal("Waypoint name")));name.setMaxLength(64);name.setValue(p.get("name").getAsString());name.setResponder(s->p.addProperty("name",s));
  for(int i=0;i<3;i++){String axis=List.of("x","y","z").get(i);var e=addRenderableWidget(new EditBox(font,right+i*(rw/3),top+99,rw/3-5,20,Component.literal(axis)));e.setMaxLength(20);e.setValue(String.format(Locale.ROOT,"%.1f",p.get(axis).getAsDouble()));e.setResponder(s->{try{double value=Double.parseDouble(s);if(!Double.isFinite(value)||Math.abs(value)>30_000_000)throw new NumberFormatException();p.addProperty(axis,value);e.setTextColor(0xFFFFFFFF);}catch(NumberFormatException ex){e.setTextColor(0xFFFF6666);}});}
  }
  if(!compact||appearance){
  int offset=compact?96:0;
  int cols=10,iw=Math.min(28,(rw-18)/cols);
  for(int i=0;i<ICONS.size();i++){String icon=ICONS.get(i);var b=addRenderableWidget(new MenuButton(right+i%cols*(iw+2),top+142-offset+i/cols*24,iw,20,"",()->{p.addProperty("icon",icon);init();},()->p.has("icon")&&p.get("icon").getAsString().equals(icon)));b.setMessage(WaypointIcons.text(icon));b.setTooltip(Tooltip.create(Component.literal(icon.replace('_',' '))));}
  button(right,top+196-offset,rw/2-3,"Marker color",()->{var color=new ColorSetting("marker","Waypoint color",module.pointColor(p));minecraft.setScreenAndShow(new ColorPickerScreen(this,color,()->p.addProperty("color",color.get())));});
  button(right+rw/2+3,top+196-offset,rw/2-3,p.get("visible").getAsBoolean()?"Visible":"Hidden",()->{p.addProperty("visible",!p.get("visible").getAsBoolean());init();});
  button(right,top+220-offset,rw/2-3,"Block color",()->{var color=new ColorSetting("blocks","Waypoint block color",module.blockColor(p));minecraft.setScreenAndShow(new ColorPickerScreen(this,color,()->{p.addProperty("blockColor",color.get());MonkeyClient.saveConfig();}));});
  button(right,top+246-offset,rw/2-3,!p.has("showBlocks")||p.get("showBlocks").getAsBoolean()?"Blocks: on":"Blocks: off",()->{p.addProperty("showBlocks",p.has("showBlocks")&&!p.get("showBlocks").getAsBoolean());init();});
  button(right+rw/2+3,top+246-offset,rw/2-3,!p.has("showName")||p.get("showName").getAsBoolean()?"Name: on":"Name: off",()->{p.addProperty("showName",p.has("showName")&&!p.get("showName").getAsBoolean());init();});
  }
  if(selected==-2){button(right,top+ph-30,rw/2-3,"Discard",()->{module.discardDraft();selected=-1;init();});button(right+rw/2+3,top+ph-30,rw/2-3,"Save waypoint",()->{module.saveDraft();selected=module.points.get().size()-1;init();});}
  else button(right,top+ph-30,rw,"Delete waypoint",()->{module.points.get().remove(selected);selected=-1;MonkeyClient.saveConfig();init();});
 }
 @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float d){
  var t=MonkeyClient.theme();g.fill(0,0,width,height,0x95000000);VanillaDraw.panel(g,left,top,pw,ph,false);VanillaDraw.panel(g,left+6,top+36,rail-6,ph-42,true);g.fill(left+rail,top+36,left+rail+1,top+ph,t.line());
  g.text(font,"Waypoints",left+12,top+16,t.ink());if(ph>=310)g.text(font,module.points.get().size()+" saved",left+88,top+16,t.mutedInk());
  var p=point();if(p==null){g.centeredText(font,"Select a waypoint",right+rw/2,top+ph/2-10,t.ink());g.centeredText(font,"Hold "+module.key.display()+" in the world to paint an area",right+rw/2,top+ph/2+8,t.mutedInk());}
  else{if(ph>=310||!appearance){g.text(font,"Name",right,top+44,t.mutedInk());for(int i=0;i<3;i++)g.text(font,List.of("X","Y","Z").get(i),right+i*(rw/3),top+87,t.mutedInk());}if(ph>=310||appearance)g.text(font,"Icon",right,top+(ph<310?34:130),t.mutedInk());
   if(ph>=310)g.text(font,(p.has("blocks")?p.getAsJsonArray("blocks").size()+" painted blocks":"Position marker")+" · "+p.get("dimension").getAsString().replace("minecraft:",""),right,top+277,t.mutedInk());
   if(ph>=335)g.text(font,font.plainSubstrByWidth(p.get("world").getAsString(),rw),right,top+291,t.mutedInk());
  }
  if(filtered.isEmpty())g.text(font,"No waypoints here",left+14,top+106,t.mutedInk());
  super.extractRenderState(g,mx,my,d);
 }
 @Override public boolean mouseScrolled(double x,double y,double dx,double dy){if(x<left+rail){scroll=Math.max(0,scroll-(int)dy);init();return true;}return super.mouseScrolled(x,y,dx,dy);}
 @Override public void onClose(){MonkeyClient.saveConfig();minecraft.setScreenAndShow(parent);}
 @Override public boolean isPauseScreen(){return false;}
}
