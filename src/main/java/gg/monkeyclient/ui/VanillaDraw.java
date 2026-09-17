package gg.monkeyclient.ui;
import gg.monkeyclient.MonkeyClient;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public final class VanillaDraw {
 public static void border(GuiGraphicsExtractor g,int x,int y,int w,int h,int color){g.fill(x,y,x+w,y+1,color);g.fill(x,y+h-1,x+w,y+h,color);g.fill(x,y,x+1,y+h,color);g.fill(x+w-1,y,x+w,y+h,color);}
 public static void panel(GuiGraphicsExtractor g,int x,int y,int w,int h,boolean inset){
  var t=MonkeyClient.theme();
  if(!t.vanilla){g.fill(x,y,x+w,y+h,t.panel);MonkeyMenuScreen.outline(g,x,y,x+w,y+h,t.line());return;}
  g.fill(x,y,x+w,y+h,0xFF101010);
  g.fill(x+1,y+1,x+w-1,y+h-1,inset?0xFF373737:0xFFF6F6F6);
  g.fill(x+3,y+3,x+w-1,y+h-1,inset?0xFFFFFFFF:0xFF555555);
  g.fill(x+3,y+3,x+w-3,y+h-3,inset?0xFF8B8B8B:0xFFC6C6C6);
 }
 public static void sprite(GuiGraphicsExtractor g,String sprite,int x,int y,int w,int h){g.blitSprite(RenderPipelines.GUI_TEXTURED,Identifier.withDefaultNamespace("widget/"+sprite),x,y,w,h);}
}
