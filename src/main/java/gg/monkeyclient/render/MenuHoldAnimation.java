package gg.monkeyclient.render;
import gg.monkeyclient.MonkeyClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
/** Four pixel corners converge as Right Shift is held, then the menu unfolds. */
public final class MenuHoldAnimation {
 public static void draw(GuiGraphicsExtractor g,Minecraft mc){
  float p=MonkeyClient.holdProgress();if(p<=0)return;
  float ease=1-(1-p)*(1-p)*(1-p);int cx=g.guiWidth()/2,cy=g.guiHeight()/2;
  int r=Math.round(35-17*ease),length=4+Math.round(7*ease),color=MonkeyClient.theme().accent|0xff000000;
  for(int dx:new int[]{-1,1})for(int dy:new int[]{-1,1}){
   int x=cx+dx*r,y=cy+dy*r;
   g.fill(dx<0?x:x-length,y,dx<0?x+length:x,y+2,color);
   g.fill(x,dy<0?y:y-length,x+2,dy<0?y+length:y,color);
  }
 }
}
