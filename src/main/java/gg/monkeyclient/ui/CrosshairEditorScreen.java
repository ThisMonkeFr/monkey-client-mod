package gg.monkeyclient.ui;
import gg.monkeyclient.MonkeyClient;
import gg.monkeyclient.modules.CustomCrosshair;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
public class CrosshairEditorScreen extends Screen {
 private final Screen parent;private final CustomCrosshair module;private int cell,left,top;
 public CrosshairEditorScreen(Screen parent,CustomCrosshair module){super(Component.literal("Crosshair drawing"));this.parent=parent;this.module=module;}
 @Override protected void init(){cell=Math.max(2,Math.min(10,(height-75)/33));left=(width-cell*33)/2;top=32;
  addRenderableWidget(new MenuButton(width/2-102,height-30,98,20,"CLEAR",()->module.pixels.set("0".repeat(1089)),()->false));
  addRenderableWidget(new MenuButton(width/2+4,height-30,98,20,"USE DRAWING",()->{module.shape.set("Custom drawing");onClose();},()->true));}
 @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float d){
  g.fill(0,0,width,height,0xDD101010);g.text(font,"Left drag: draw   Right drag: erase",left,14,MonkeyClient.theme().text);
  String s=module.pixels.get();for(int y=0;y<33;y++)for(int x=0;x<33;x++){int i=y*33+x;g.fill(left+x*cell,top+y*cell,left+(x+1)*cell-1,top+(y+1)*cell-1,i<s.length()&&s.charAt(i)=='1'?MonkeyClient.theme().accent:((x==16||y==16)?0xFF444444:0xFF252525));}
  super.extractRenderState(g,mx,my,d);
 }
 private boolean paint(MouseButtonEvent e){if(e.x()<left||e.y()<top||e.x()>=left+33*cell||e.y()>=top+33*cell)return false;module.paintPixel((int)(e.x()-left)/cell,(int)(e.y()-top)/cell,e.button()==0);return true;}
 @Override public boolean mouseClicked(MouseButtonEvent e,boolean twice){return paint(e)||super.mouseClicked(e,twice);}
 @Override public boolean mouseDragged(MouseButtonEvent e,double dx,double dy){return paint(e)||super.mouseDragged(e,dx,dy);}
 @Override public void onClose(){MonkeyClient.saveConfig();minecraft.setScreenAndShow(parent);}
 @Override public boolean isPauseScreen(){return false;}
}
