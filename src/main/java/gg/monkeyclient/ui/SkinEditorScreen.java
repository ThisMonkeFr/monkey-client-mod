package gg.monkeyclient.ui;
import com.google.gson.*;
import com.mojang.blaze3d.platform.NativeImage;
import gg.monkeyclient.integration.LauncherBridge;
import gg.monkeyclient.module.setting.ColorSetting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.input.*;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import java.util.*;
/** Pixel editor: one texture upload per changed stroke, not per frame. */
public final class SkinEditorScreen extends NativeScreen {
 private final String kind;private String id,name,tool="Pen";private boolean slim,dirty,dragging,saving;private final int iw=64;private int ih,px,py,cell,lastX=-1,lastY=-1;
 private int[] pixels;private DynamicTexture texture;private Identifier textureId;
 private final ArrayDeque<int[]> undo=new ArrayDeque<>(),redo=new ArrayDeque<>();
 private final ColorSetting colour=new ColorSetting("paint","Paint colour",0xFFFF7A18);
 public SkinEditorScreen(Screen parent,String kind,JsonObject item){super(parent,kind.equals("skin")?"Skin studio":"Cape studio");this.kind=kind;ih=kind.equals("skin")?64:32;id=str(item,"id");name=item==null?"My "+kind:str(item,"name");slim=yes(item,"slim");pixels=new int[iw*ih];
  if(item!=null&&!str(item,"data").isBlank())try(var image=decode(kind,str(item,"data"))){for(int y=0;y<ih;y++)for(int x=0;x<iw;x++)pixels[y*iw+x]=y<image.getHeight()?image.getPixel(x*image.getWidth()/iw,y*image.getHeight()/ih):0;if(image.getWidth()!=64){id="";name+=" (edited copy)";} }catch(Exception e){status="Could not load this texture.";}
  else {for(int y=0;y<ih;y++)for(int x=0;x<iw;x++)pixels[y*iw+x]=kind.equals("cape")?0xFFFF7A18:0xFF787878;}
 }
 private static NativeImage decode(String kind,String data)throws Exception{var image=NativeImage.read(Base64.getDecoder().decode(data.split(",",2)[1]));return kind.equals("skin")?gg.monkeyclient.cosmetics.SkinPixels.modern(image):image;}
 @Override protected void init(){super.init();upload();}
 @Override protected void rebuild(){clearWidgets();cell=Math.max(1,Math.min((pw-154)/iw,(ph-100)/ih));px=left+12;py=body;int x=px+iw*cell+14,w=left+pw-12-x;
  var f=field(left+12,top+33,pw-88,"Name",name,48);f.setResponder(v->{name=v;dirty=true;});button(left+pw-68,top+33,56,"Back",this::onClose);
  String[] tools={"Pen","Erase","Fill","Pick"};for(int i=0;i<4;i++){String t=tools[i];addRenderableWidget(new MenuButton(x+i%2*(w/2+2),py+i/2*24,w/2-2,20,t,()->{tool=t;rebuild();},()->tool.equals(t)));}
  button(x,py+50,w,"Colour",()->minecraft.setScreenAndShow(new ColorPickerScreen(this,colour,()->{})));
  button(x,py+74,w/2-2,"Undo",()->restore(undo,redo)).active=!undo.isEmpty();button(x+w/2+2,py+74,w/2-2,"Redo",()->restore(redo,undo)).active=!redo.isEmpty();
  if(kind.equals("skin"))button(x,py+98,w,slim?"Slim arms":"Classic arms",()->{slim=!slim;dirty=true;rebuild();});
  button(left+12,bottom-22,90,saving?"Saving...":"Save to library",this::save).active=!saving;
  button(left+110,bottom-22,65,"Clear",()->minecraft.setScreenAndShow(new ConfirmScreen(ok->{minecraft.setScreenAndShow(this);if(ok){push();Arrays.fill(pixels,0);changed();}},net.minecraft.network.chat.Component.literal("Clear texture?"),net.minecraft.network.chat.Component.literal("You can undo this change."))));
 }
 private void push(){undo.addLast(pixels.clone());while(undo.size()>24)undo.removeFirst();redo.clear();}
 private void restore(ArrayDeque<int[]> from,ArrayDeque<int[]> to){if(from.isEmpty())return;to.addLast(pixels.clone());pixels=from.removeLast();changed();rebuild();}
 private void changed(){dirty=true;upload();}
 private void upload(){if(texture==null){var image=new NativeImage(iw,ih,true);textureId=Identifier.fromNamespaceAndPath("monkeyclient","dynamic/editor_"+Integer.toHexString(System.identityHashCode(this)));texture=new DynamicTexture(()->"Skin studio",image);minecraft.getTextureManager().register(textureId,texture);}var image=texture.getPixels();for(int y=0;y<ih;y++)for(int x=0;x<iw;x++)image.setPixel(x,y,pixels[y*iw+x]);texture.upload();}
 private void paint(int x,int y){if(x<0||x>=iw||y<0||y>=ih)return;int at=y*iw+x;if(tool.equals("Pick")){colour.set(pixels[at]);tool="Pen";rebuild();return;}int c=tool.equals("Erase")?0:colour.get();if(tool.equals("Fill")){int from=pixels[at];if(from==c)return;var q=new ArrayDeque<Integer>();q.add(at);pixels[at]=c;while(!q.isEmpty()){int a=q.removeFirst(),ax=a%iw,ay=a/iw;for(int n:new int[]{ax>0?a-1:-1,ax<iw-1?a+1:-1,ay>0?a-iw:-1,ay<ih-1?a+iw:-1})if(n>=0&&pixels[n]==from){pixels[n]=c;q.add(n);}}}else pixels[at]=c;changed();}
 private boolean inside(double x,double y){return x>=px&&y>=py&&x<px+iw*cell&&y<py+ih*cell;}
 @Override public boolean mouseClicked(MouseButtonEvent e,boolean twice){if(e.button()==0&&inside(e.x(),e.y())){push();lastX=(int)(e.x()-px)/cell;lastY=(int)(e.y()-py)/cell;paint(lastX,lastY);dragging=tool.equals("Pen")||tool.equals("Erase");return true;}return super.mouseClicked(e,twice);}
 @Override public boolean mouseDragged(MouseButtonEvent e,double dx,double dy){if(dragging){int x=Math.max(0,Math.min(iw-1,(int)(e.x()-px)/cell)),y=Math.max(0,Math.min(ih-1,(int)(e.y()-py)/cell)),steps=Math.max(Math.abs(x-lastX),Math.abs(y-lastY));for(int i=1;i<=steps;i++)paint(lastX+(x-lastX)*i/steps,lastY+(y-lastY)*i/steps);lastX=x;lastY=y;return true;}return super.mouseDragged(e,dx,dy);}
 @Override public boolean mouseReleased(MouseButtonEvent e){dragging=false;rebuild();return super.mouseReleased(e);}
 private void save(){if(saving||name.isBlank())return;saving=true;rebuild();int[] snapshot=pixels.clone();String savedName=name;boolean savedSlim=slim;LauncherBridge.WORK.execute(()->{try{var image=new java.awt.image.BufferedImage(iw,ih,java.awt.image.BufferedImage.TYPE_INT_ARGB);image.setRGB(0,0,iw,ih,snapshot,0,iw);var output=new java.io.ByteArrayOutputStream();javax.imageio.ImageIO.write(image,"png",output);JsonObject item=obj("name",savedName,"slim",savedSlim,"data","data:image/png;base64,"+Base64.getEncoder().encodeToString(output.toByteArray()));if(!id.isBlank())item.addProperty("id",id);LauncherBridge.json("/library/save",obj("kind",kind,"item",item)).whenComplete((r,e)->minecraft.execute(()->{saving=false;if(e==null){dirty=false;id=str(r.getAsJsonObject("item"),"id");status="Saved to your launcher library";}else status=e.getCause().getMessage();rebuild();}));}catch(Exception e){minecraft.execute(()->{saving=false;status="Could not save: "+e.getMessage();rebuild();});}});}
 @Override public void onClose(){if(!dirty){super.onClose();return;}minecraft.setScreenAndShow(new ConfirmScreen(ok->{if(ok)minecraft.setScreenAndShow(parent);else minecraft.setScreenAndShow(this);},net.minecraft.network.chat.Component.literal("Discard unsaved changes?"),net.minecraft.network.chat.Component.literal("Use Save to library to keep your skin or cape.")));}
 @Override public void removed(){if(textureId!=null)minecraft.getTextureManager().release(textureId);texture=null;textureId=null;super.removed();}
 @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float dt){super.extractRenderState(g,mx,my,dt);for(int y=0;y<ih;y+=4)for(int x=0;x<iw;x+=4)g.fill(px+x*cell,py+y*cell,px+Math.min(iw,x+4)*cell,py+Math.min(ih,y+4)*cell,((x/4+y/4)%2==0)?0xFF969696:0xFF666666);if(textureId!=null)g.blit(RenderPipelines.GUI_TEXTURED,textureId,px,py,0f,0f,iw*cell,ih*cell,iw,ih,iw,ih);if(inside(mx,my)){int x=(mx-px)/cell,y=(my-py)/cell;MonkeyMenuScreen.outline(g,px+x*cell,py+y*cell,px+(x+1)*cell,py+(y+1)*cell,0xFFFFFFFF);}int x=px+iw*cell+14,w=left+pw-12-x;g.fill(x,py+124,x+w,py+133,colour.get());if(kind.equals("skin")&&textureId!=null&&bottom-py>180)LauncherScreen.drawSkin(g,new Picture(textureId,iw,ih),x,py+140,w,bottom-py-166,slim);}
}
