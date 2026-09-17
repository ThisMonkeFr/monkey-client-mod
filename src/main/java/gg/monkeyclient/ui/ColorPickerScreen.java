package gg.monkeyclient.ui;
import gg.monkeyclient.MonkeyClient;
import gg.monkeyclient.module.setting.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import com.mojang.blaze3d.platform.NativeImage;
/** HSV wheel with brightness and opacity; colors apply live. */
public class ColorPickerScreen extends Screen {
 private final Screen parent;private final ColorSetting color;private final Runnable changed;
 private final NumberSetting value=new NumberSetting("v","Brightness",1,0,1,.01),alpha=new NumberSetting("a","Opacity",1,0,1,.01);
 private float hue,saturation;private int left,top;private boolean dragging;private Identifier texture;
 public ColorPickerScreen(Screen parent,ColorSetting color,Runnable changed){super(Component.literal(color.label));this.parent=parent;this.color=color;this.changed=changed;float[] hsv=java.awt.Color.RGBtoHSB(color.get()>>16&255,color.get()>>8&255,color.get()&255,null);hue=hsv[0];saturation=hsv[1];value.set((double)hsv[2]);alpha.set((color.get()>>>24)/255d);}
 @Override protected void init(){clearWidgets();left=width/2-124;top=height/2-105;
  var image=new NativeImage(128,128,true);for(int y=0;y<128;y++)for(int x=0;x<128;x++){double dx=x-63.5,dy=y-63.5,r=Math.hypot(dx,dy)/63;image.setPixel(x,y,r>1?0:java.awt.Color.HSBtoRGB((float)((Math.atan2(dy,dx)/Math.PI/2+1)%1),(float)r,1));}
  texture=Identifier.fromNamespaceAndPath("monkeyclient","dynamic/color_wheel");minecraft.getTextureManager().register(texture,new DynamicTexture(()->"Color wheel",image));
  addRenderableWidget(new SettingSlider(left+150,top+65,90,value,this::apply));addRenderableWidget(new SettingSlider(left+150,top+110,90,alpha,this::apply));
  addRenderableWidget(new MenuButton(left+150,top+148,90,20,color.rainbow?"Rainbow: on":"Rainbow: off",()->{color.rainbow=!color.rainbow;changed.run();init();}));
  addRenderableWidget(new MenuButton(left+8,top+180,232,20,"Done",this::onClose));
 }
 private void apply(){color.set(((int)Math.round(alpha.get()*255)<<24)|(java.awt.Color.HSBtoRGB(hue,saturation,value.getFloat())&0xFFFFFF));changed.run();}
 private boolean pick(double x,double y){double dx=x-(left+72),dy=y-(top+98);double r=Math.hypot(dx,dy);if(!dragging&&r>64)return false;hue=(float)((Math.atan2(dy,dx)/Math.PI/2+1)%1);saturation=(float)Math.min(1,r/63);apply();return true;}
 @Override public boolean mouseClicked(MouseButtonEvent e,boolean twice){if(e.button()==0&&pick(e.x(),e.y())){dragging=true;return true;}return super.mouseClicked(e,twice);}
 @Override public boolean mouseDragged(MouseButtonEvent e,double dx,double dy){if(dragging)return pick(e.x(),e.y());return super.mouseDragged(e,dx,dy);}
 @Override public boolean mouseReleased(MouseButtonEvent e){dragging=false;return super.mouseReleased(e);}
 @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float dt){var t=MonkeyClient.theme();g.fill(0,0,width,height,0x90000000);VanillaDraw.panel(g,left,top,248,210,false);g.text(font,font.plainSubstrByWidth(color.label,225),left+10,top+12,t.ink());
  g.blit(RenderPipelines.GUI_TEXTURED,texture,left+8,top+34,0,0,128,128,128,128);int x=left+72+(int)(Math.cos(hue*Math.PI*2)*saturation*63),y=top+98+(int)(Math.sin(hue*Math.PI*2)*saturation*63);VanillaDraw.border(g,x-3,y-3,7,7,0xFF000000);VanillaDraw.border(g,x-2,y-2,5,5,0xFFFFFFFF);
  g.fill(left+150,top+33,left+240,top+48,color.resolve());g.text(font,"Brightness",left+150,top+54,t.ink());g.text(font,"Opacity",left+150,top+98,t.ink());super.extractRenderState(g,mx,my,dt);
 }
 @Override public void onClose(){MonkeyClient.saveConfig();minecraft.setScreenAndShow(parent);}
 @Override public boolean isPauseScreen(){return false;}
}
