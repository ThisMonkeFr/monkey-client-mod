package gg.monkeyclient.ui;
import gg.monkeyclient.MonkeyClient;
import gg.monkeyclient.integration.LauncherBridge;
import com.google.gson.*;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import java.util.*;
import java.util.function.Consumer;
/** Native widgets and bounded, screen-owned textures. No per-frame network work. */
public abstract class NativeScreen extends Screen {
 protected final Screen parent;protected int left,top,pw,ph,body,bottom;protected String status="";protected boolean loaded;
 private int generation;private boolean alive;private int textureSequence;
 private final Map<String,Picture> pictures=new HashMap<>();
 protected record Picture(Identifier id,int width,int height){}
 protected NativeScreen(Screen parent,String title){super(Component.literal(title));this.parent=parent;}
 @Override protected void init(){alive=true;generation++;pw=Math.min(760,width-16);ph=Math.min(430,height-16);left=(width-pw)/2;top=(height-ph)/2;body=top+62;bottom=top+ph-30;rebuild();}
 protected abstract void rebuild();
 protected void chrome(String tab){clearWidgets();clearFocus();button(left+pw-62,top+8,54,"Back",this::onClose);int x=left+10;for(String t:List.of("Friends","Screenshots","Skins")){String target=t.toLowerCase(Locale.ROOT);addRenderableWidget(new MenuButton(x,top+32,Math.min(96,(pw-24)/3),20,t,()->open(parent,target),()->tab.equals(target)));x+=Math.min(100,(pw-20)/3);} }
 public static void open(Screen parent,String tab){Minecraft.getInstance().setScreenAndShow(tab.equals("friends")?new FriendsScreen(parent):new LauncherScreen(parent,tab));}
 protected MenuButton button(int x,int y,int w,String label,Runnable action){return addRenderableWidget(new MenuButton(x,y,Math.max(16,w),20,label,action));}
 protected EditBox field(int x,int y,int w,String label,String value,int limit){var f=addRenderableWidget(new EditBox(font,x,y,Math.max(20,w),20,Component.literal(label)));f.setMaxLength(limit);f.setHint(Component.literal(label));f.setValue(value);return f;}
 public static JsonObject obj(Object...pairs){JsonObject j=new JsonObject();for(int i=0;i<pairs.length;i+=2){Object v=pairs[i+1];if(v instanceof JsonElement e)j.add((String)pairs[i],e);else if(v instanceof Boolean b)j.addProperty((String)pairs[i],b);else if(v instanceof Number n)j.addProperty((String)pairs[i],n);else j.addProperty((String)pairs[i],v==null?null:v.toString());}return j;}
 public static String str(JsonObject j,String key){return j!=null&&j.has(key)&&!j.get(key).isJsonNull()?j.get(key).getAsString():"";}
 public static boolean yes(JsonObject j,String key){return j!=null&&j.has(key)&&!j.get(key).isJsonNull()&&j.get(key).getAsBoolean();}
 protected static JsonArray array(JsonObject j,String key){return j!=null&&j.has(key)&&j.get(key).isJsonArray()?j.getAsJsonArray(key):new JsonArray();}
 protected void call(String path,JsonObject data,Consumer<JsonObject> done){request(path,data,done,true);}
 protected void request(String path,JsonObject data,Consumer<JsonObject> done,boolean show){int current=generation;if(show)status="Loading...";LauncherBridge.json(path,data).whenComplete((r,e)->Minecraft.getInstance().execute(()->{if(!alive||generation!=current)return;if(e!=null){if(show){Throwable cause=e;while(cause.getCause()!=null)cause=cause.getCause();status=cause.getMessage();}return;}if(show)status="";done.accept(r);}));}
 protected void picture(String key,String data){if(data==null||data.isBlank()||pictures.containsKey(key))return;int current=generation;LauncherBridge.WORK.execute(()->{try{int comma=data.indexOf(',');byte[] bytes=Base64.getDecoder().decode(data.substring(comma+1));if(bytes.length>12*1024*1024)return;NativeImage image=decodePicture(bytes);Minecraft.getInstance().execute(()->{if(!alive||generation!=current||pictures.containsKey(key)){image.close();return;}var id=Identifier.fromNamespaceAndPath("monkeyclient","dynamic/native_"+Integer.toHexString(System.identityHashCode(this))+"_"+(textureSequence++));minecraft.getTextureManager().register(id,new DynamicTexture(()->"Monkey library image",image));pictures.put(key,new Picture(id,image.getWidth(),image.getHeight()));});}catch(Exception ignored){}});}
 private static NativeImage decodePicture(byte[] bytes)throws Exception{
  try(var input=javax.imageio.ImageIO.createImageInputStream(new java.io.ByteArrayInputStream(bytes))){
   var readers=javax.imageio.ImageIO.getImageReaders(input);if(!readers.hasNext())throw new java.io.IOException("Unsupported image");var reader=readers.next();
   try{reader.setInput(input,true,true);int w=reader.getWidth(0),h=reader.getHeight(0);if(w<1||h<1||w>8192||h>8192)throw new java.io.IOException("Image is too large");var options=reader.getDefaultReadParam();int skip=Math.max(1,(int)Math.ceil(Math.max(w,h)/1920d));options.setSourceSubsampling(skip,skip,0,0);var decoded=reader.read(0,options);var result=new NativeImage(decoded.getWidth(),decoded.getHeight(),true);for(int y=0;y<decoded.getHeight();y++)for(int x=0;x<decoded.getWidth();x++)result.setPixel(x,y,decoded.getRGB(x,y));return result;}finally{reader.dispose();}
  }
 }
 protected Picture picture(String key){return pictures.get(key);}
 protected void drawPicture(GuiGraphicsExtractor g,String key,int x,int y,int w,int h){var p=pictures.get(key);if(p==null)return;float scale=Math.min(w/(float)p.width,h/(float)p.height);int dw=Math.max(1,(int)(p.width*scale)),dh=Math.max(1,(int)(p.height*scale));g.blit(RenderPipelines.GUI_TEXTURED,p.id,x+(w-dw)/2,y+(h-dh)/2,0f,0f,dw,dh,p.width,p.height,p.width,p.height);}
 protected void clearPictures(){for(var p:pictures.values())minecraft.getTextureManager().release(p.id);pictures.clear();}
 protected void label(GuiGraphicsExtractor g,String text,int x,int y,int w){g.text(font,font.plainSubstrByWidth(text,Math.max(1,w)),x,y,MonkeyClient.theme().ink(),false);}
 @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float dt){var t=MonkeyClient.theme();g.fill(0,0,width,height,0x80000000);g.fill(left,top,left+pw,top+ph,t.backdrop());MonkeyMenuScreen.outline(g,left,top,left+pw,top+ph,t.line());g.text(font,getTitle(),left+12,top+14,t.ink(),false);if(!status.isBlank())label(g,status,left+12,top+ph-14,pw-24);super.extractRenderState(g,mx,my,dt);}
 @Override public void removed(){alive=false;generation++;clearPictures();super.removed();}
 @Override public void onClose(){minecraft.setScreenAndShow(parent);}
 @Override public boolean isPauseScreen(){return false;}
}
