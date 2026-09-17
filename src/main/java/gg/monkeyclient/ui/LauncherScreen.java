package gg.monkeyclient.ui;
import gg.monkeyclient.integration.LauncherBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.*;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.platform.NativeImage;
import com.google.gson.*;
public class LauncherScreen extends Screen {
 private final Screen parent;private String tab,status="Connecting to launcher...";private int x,y,w,h;private long revision=-1,nextFrame;private volatile boolean alive,busy;private boolean loaded,dragging;private long lastMouseMove;
 private static final Identifier TEXTURE=Identifier.fromNamespaceAndPath("monkeyclient","dynamic/launcher");
 public LauncherScreen(Screen parent,String tab){super(Component.literal("Monkey Client"));this.parent=parent;this.tab=tab;}
 @Override protected void init(){alive=true;w=Math.min(width-16,(height-44)*1000/680);h=w*680/1000;x=(width-w)/2;y=36;
  addRenderableWidget(new MenuButton(8,8,60,20,"Back",this::onClose));int tx=76;
  for(String name:java.util.List.of("friends","screenshots","skins")){String current=name;addRenderableWidget(new MenuButton(tx,8,82,20,name.substring(0,1).toUpperCase()+name.substring(1),()->{tab=current;loaded=false;revision=-1;open();},()->tab.equals(current)));tx+=86;}open();
 }
 private void open(){JsonObject b=new JsonObject();b.addProperty("tab",tab);LauncherBridge.json("/open",b).whenComplete((r,e)->Minecraft.getInstance().execute(()->{if(alive){status=e==null?"Loading...":e.getCause()==null?e.getMessage():e.getCause().getMessage();}}));}
 @Override public void tick(){super.tick();if(!alive||busy||System.nanoTime()<nextFrame||!LauncherBridge.available())return;busy=true;nextFrame=System.nanoTime()+100_000_000L;
  LauncherBridge.WORK.execute(()->{try{var response=LauncherBridge.request("/frame?after="+revision,null);if(response.statusCode()==200){var image=NativeImage.read(response.body());long next=Long.parseLong(response.headers().firstValue("X-Frame").orElse("0"));Minecraft.getInstance().execute(()->{if(!alive){image.close();return;}minecraft.getTextureManager().register(TEXTURE,new DynamicTexture(()->"Launcher tab",image));revision=next;loaded=true;});}}catch(Exception e){Minecraft.getInstance().execute(()->{if(alive)status="Keep the launcher open to use this tab.";});}finally{busy=false;}});
 }
 @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float dt){g.fill(0,0,width,height,0xE0101010);if(loaded)g.blit(RenderPipelines.GUI_TEXTURED,TEXTURE,x,y,0f,0f,w,h,1000,680,1000,680);else g.centeredText(font,font.plainSubstrByWidth(status,width-30),width/2,height/2,0xFFFFFFFF);super.extractRenderState(g,mx,my,dt);}
 private boolean inside(double mx,double my){return mx>=x&&my>=y&&mx<x+w&&my<y+h;}
 private final java.util.concurrent.atomic.AtomicInteger pendingInputs=new java.util.concurrent.atomic.AtomicInteger();
 private void input(JsonObject b){if(!alive||pendingInputs.get()>=24)return;pendingInputs.incrementAndGet();LauncherBridge.json("/input",b).whenComplete((r,e)->pendingInputs.decrementAndGet());}
 private void mouse(String type,double mx,double my,int button,double wheel){if(type.equals("mouseMove")){long now=System.nanoTime();if(now-lastMouseMove<33_000_000L)return;lastMouseMove=now;}JsonObject b=new JsonObject();b.addProperty("type",type);b.addProperty("x",(mx-x)*1000/w);b.addProperty("y",(my-y)*680/h);b.addProperty("button",button==0?"left":button==1?"right":"middle");b.addProperty("deltaY",wheel);input(b);}
 @Override public boolean mouseClicked(MouseButtonEvent e,boolean twice){if(inside(e.x(),e.y())){dragging=true;mouse("mouseDown",e.x(),e.y(),e.button(),0);return true;}return super.mouseClicked(e,twice);}
 @Override public boolean mouseReleased(MouseButtonEvent e){if(dragging){dragging=false;mouse("mouseUp",e.x(),e.y(),e.button(),0);return true;}return super.mouseReleased(e);}
 @Override public boolean mouseDragged(MouseButtonEvent e,double dx,double dy){if(dragging){mouse("mouseMove",e.x(),e.y(),e.button(),0);return true;}return super.mouseDragged(e,dx,dy);}
 @Override public void mouseMoved(double mx,double my){if(inside(mx,my))mouse("mouseMove",mx,my,0,0);super.mouseMoved(mx,my);}
 @Override public boolean mouseScrolled(double mx,double my,double dx,double dy){if(inside(mx,my)){mouse("mouseWheel",mx,my,0,dy*60);return true;}return super.mouseScrolled(mx,my,dx,dy);}
 @Override public boolean charTyped(CharacterEvent e){JsonObject b=new JsonObject();b.addProperty("type","text");b.addProperty("text",e.codepointAsString());input(b);return true;}
 @Override public boolean keyPressed(KeyEvent e){if(e.isEscape()){onClose();return true;}String key=switch(e.key()){case 259->"Backspace";case 261->"Delete";case 257,335->"Enter";case 258->"Tab";case 262->"ArrowRight";case 263->"ArrowLeft";case 264->"ArrowDown";case 265->"ArrowUp";case 268->"Home";case 269->"End";default->e.hasControlDown()&&"ACVXZ".indexOf((char)e.key())>=0?String.valueOf((char)e.key()):null;};if(key==null)return true;JsonObject b=new JsonObject();b.addProperty("type","key");b.addProperty("key",key);JsonArray mods=new JsonArray();if(e.hasControlDown())mods.add("control");if(e.hasShiftDown())mods.add("shift");b.add("modifiers",mods);input(b);return true;}
 @Override public void removed(){alive=false;minecraft.getTextureManager().release(TEXTURE);LauncherBridge.json("/close",new JsonObject()).exceptionally(e->null);super.removed();}
 @Override public void onClose(){minecraft.setScreenAndShow(parent);}
 @Override public boolean isPauseScreen(){return false;}
}
