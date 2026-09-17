package gg.monkeyclient.ui;
import com.google.gson.*;
import gg.monkeyclient.MonkeyClient;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.renderer.RenderPipelines;
import java.util.function.Consumer;
/** Minecraft's own screenshot gallery and shared skin/cape library. */
public class LauncherScreen extends NativeScreen {
 private final String tab;private String kind="skin",selected="",lookup="";private JsonArray items=new JsonArray();private JsonObject item;
 private int offset,total,columns,rows,cardW,cardH,limit=12;private boolean current,hasMore;private final Consumer<JsonObject> pick;
 public LauncherScreen(Screen parent,String tab){this(parent,tab,null);}
 public LauncherScreen(Screen parent,String tab,Consumer<JsonObject> pick){super(parent,tab.equals("screenshots")?"Screenshots":"Skins & Capes");this.tab=tab;this.pick=pick;}
 @Override protected void init(){super.init();load();}
 @Override protected void rebuild(){chrome(tab);if(tab.equals("screenshots"))gallery();else library();}
 private void load(){if(tab.equals("screenshots")){call("/screenshots/list",obj("offset",offset,"limit",limit,"current",current),this::received);}else call("/library/list",obj("kind",kind,"offset",offset,"limit",limit),this::received);}
 private void received(JsonObject r){items=array(r,"items");total=r.has("total")?r.get("total").getAsInt():0;hasMore=yes(r,"hasMore");loaded=true;clearPictures();if(items.asList().stream().noneMatch(v->str(v.getAsJsonObject(),"id").equals(selected))){selected="";item=null;}for(var e:items){var row=e.getAsJsonObject();picture(str(row,"id"),str(row,"thumbnail"));}rebuild();if(item!=null)picture("selected",str(item,"data"));}
 private void page(int delta){offset=Math.max(0,offset+delta*limit);selected="";item=null;load();}
 private void pagination(){var prev=button(left+pw-208,top+8,24,"<",()->page(-1));prev.active=offset>0;var next=button(left+pw-92,top+8,24,">",()->page(1));next.active=hasMore;}
 private void gallery(){
  columns=pw<480?3:4;rows=Math.max(1,Math.min(3,(bottom-body-28)/66));limit=columns*rows;cardW=(pw-24)/columns;cardH=(bottom-body-28)/rows;pagination();
  for(int i=0;i<items.size()&&i<limit;i++){var row=items.get(i).getAsJsonObject();String id=str(row,"id");int x=left+12+i%columns*cardW,y=body+i/columns*cardH;
   var b=addRenderableWidget(new MenuButton(x,y,cardW-5,cardH-5,"",()->{selected=id;item=row;rebuild();},()->id.equals(selected)));b.setTooltip(net.minecraft.client.gui.components.Tooltip.create(net.minecraft.network.chat.Component.literal(str(row,"name")+"\n"+str(row,"profile"))));
  }
  int y=bottom-22;button(left+12,y,68,current?"This profile":"All profiles",()->{current=!current;offset=0;load();});button(left+84,y,52,"Refresh",this::load);
  var open=button(left+pw-194,y,58,pick==null?"Open":"Attach",()->{if(pick!=null){pick.accept(item);onClose();}else call("/screenshots/image",obj("id",selected),r->minecraft.setScreenAndShow(new NativeImageScreen(this,str(item,"name"),str(r,"data"))));});open.active=item!=null;
  var folder=button(left+pw-132,y,58,"Folder",()->call("/screenshots/open",obj("id",selected,"reveal",true),r->{}));folder.active=item!=null;
  var remove=button(left+pw-70,y,58,"Delete",()->confirmDelete("/screenshots/delete",obj("id",selected)));remove.active=item!=null;
 }
 private void library(){
  int rail=Math.max(104,Math.min(180,pw/3)),x=left+rail+22,w=pw-rail-34;
  limit=Math.max(1,Math.min(12,(bottom-body-28)/22));pagination();
  button(left+12,body,rail/2-2,"Skins",()->switchKind("skin"));button(left+14+rail/2,body,rail/2-2,"Capes",()->switchKind("cape"));
  for(int i=0;i<items.size()&&i<limit;i++){var r=items.get(i).getAsJsonObject();String id=str(r,"id");addRenderableWidget(new MenuButton(left+12,body+26+i*22,rail,20,(yes(r,"selected")?"* ":"")+str(r,"name"),()->{selected=id;item=null;clearPictures();call("/library/item",obj("kind",kind,"id",id),result->{if(!selected.equals(id))return;item=result.getAsJsonObject("item");picture("selected",str(item,"data"));rebuild();});},()->id.equals(selected)));}
  button(x,body,Math.max(42,w/2-3),"Import PNG",()->call("/library/import",obj("kind",kind),r->{if(!yes(r,"cancelled"))load();}));button(x+w/2+3,body,Math.max(42,w/2-3),"Create "+kind,()->minecraft.setScreenAndShow(new SkinEditorScreen(this,kind,null)));
  if(kind.equals("skin")){var query=field(x,body+26,w-66,"Username",lookup,16);query.setResponder(v->lookup=v);button(x+w-62,body+26,62,"Get skin",()->call("/library/lookup",obj("name",lookup),r->load()));}
  int y=bottom-22,bw=(w-8)/3;
  var wear=button(x,y,bw,kind.equals("cape")?"Wear / Off":"Apply",()->call("/library/equip",obj("kind",kind,"id",selected),r->{var chosen=r.getAsJsonObject("item");gg.monkeyclient.cosmetics.LauncherCape.equip(kind,str(chosen,"data"),yes(chosen,"slim"),yes(r,"equipped"));load();status=kind.equals("skin")?"Skin applied. Other players may need you to rejoin.":"Cape updated.";}));wear.active=item!=null;
  var edit=button(x+bw+4,y,bw,"Edit",()->minecraft.setScreenAndShow(new SkinEditorScreen(this,kind,item)));edit.active=item!=null;
  var del=button(x+2*(bw+4),y,bw,"Delete",()->confirmDelete("/library/delete",obj("kind",kind,"id",selected)));del.active=item!=null;
 }
 private void switchKind(String next){kind=next;offset=0;item=null;selected="";load();rebuild();}
 private void confirmDelete(String path,JsonObject data){minecraft.setScreenAndShow(new ConfirmScreen(yes->{minecraft.setScreenAndShow(this);if(yes)call(path,data,r->{if(yes(r,"wasActive")&&str(r,"kind").equals("cape"))gg.monkeyclient.cosmetics.LauncherCape.equip("cape","",false,false);item=null;selected="";load();});},net.minecraft.network.chat.Component.literal("Delete this "+(tab.equals("screenshots")?"screenshot":kind)+"?"),net.minecraft.network.chat.Component.literal("This also removes it from your launcher library.")));}
 @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float dt){super.extractRenderState(g,mx,my,dt);label(g,total==0?"No items yet":(offset+1)+"-"+Math.min(offset+limit,total)+" / "+total,left+pw-180,top+14,84);
  if(tab.equals("screenshots")){for(int i=0;i<items.size()&&i<limit;i++){var r=items.get(i).getAsJsonObject();int x=left+12+i%columns*cardW,y=body+i/columns*cardH;drawPicture(g,str(r,"id"),x+3,y+3,cardW-11,cardH-24);label(g,str(r,"name"),x+5,y+cardH-17,cardW-15);}}
  else {int rail=Math.max(104,Math.min(180,pw/3)),x=left+rail+22,w=pw-rail-34,y=body+(kind.equals("skin")?56:28);if(item!=null){label(g,str(item,"name"),x,y,w);var p=picture("selected");if(p!=null){if(kind.equals("skin"))drawSkin(g,p,x,y+14,w,bottom-y-43,yes(item,"slim"));else drawPicture(g,"selected",x,y+14,w,bottom-y-43);}}else label(g,"Choose a "+kind+" to preview",x,y,w);}
 }
 public static void drawSkin(GuiGraphicsExtractor g,Picture p,int x,int y,int w,int h,boolean slim){float scale=Math.min(w/44f,h/32f);int s=Math.max(1,(int)scale),arm=slim?3:4,front=x+(w-36*s)/2,back=front+20*s;
  for(int side=0;side<2;side++){int bx=side==0?front:back;skinPart(g,p,bx+4*s,y,8,8,side==0?8:24,8,s);skinPart(g,p,bx+4*s,y+8*s,8,12,side==0?20:32,20,s);skinPart(g,p,bx+(4-arm)*s,y+8*s,arm,12,side==0?44:52,20,s);skinPart(g,p,bx+12*s,y+8*s,arm,12,side==0?44:52,20,s);skinPart(g,p,bx+4*s,y+20*s,4,12,side==0?4:12,20,s);skinPart(g,p,bx+8*s,y+20*s,4,12,side==0?4:12,20,s);skinPart(g,p,bx+4*s,y,8,8,side==0?40:56,8,s);}
 }
 private static void skinPart(GuiGraphicsExtractor g,Picture p,int x,int y,int w,int h,int u,int v,int s){g.blit(RenderPipelines.GUI_TEXTURED,p.id(),x,y,(float)u,(float)v,w*s,h*s,w,h,p.width(),p.height());}
}
