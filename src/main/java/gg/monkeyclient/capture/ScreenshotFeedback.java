package gg.monkeyclient.capture;
import com.mojang.blaze3d.platform.NativeImage;
import gg.monkeyclient.MonkeyClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
/** One small thumbnail, disposed after two seconds. Never rendered into captures. */
public final class ScreenshotFeedback {
 private static final Identifier TEXTURE=Identifier.fromNamespaceAndPath("monkeyclient","dynamic/screenshot_preview");
 private static long shownAt;
 private static boolean visible;
 public static void captured(NativeImage source){
  var preview=new NativeImage(320,180,true);
  for(int y=0;y<180;y++)for(int x=0;x<320;x++)preview.setPixel(x,y,source.getPixel(x*source.getWidth()/320,y*source.getHeight()/180));
  Minecraft.getInstance().execute(()->{
   Minecraft.getInstance().getTextureManager().register(TEXTURE,new DynamicTexture(()->"Screenshot preview",preview));
   shownAt=System.nanoTime();visible=true;
  });
 }
 public static void tick(){if(visible&&(System.nanoTime()-shownAt)>2_000_000_000L){visible=false;Minecraft.getInstance().getTextureManager().release(TEXTURE);}}
 public static void draw(GuiGraphicsExtractor g){
  if(!visible||ScreenshotCapture.active())return;
  var mc=Minecraft.getInstance();int sw=mc.getWindow().getGuiScaledWidth(),sh=mc.getWindow().getGuiScaledHeight();
  float age=(System.nanoTime()-shownAt)/1_000_000_000f;
  if(age<.18f)g.fill(0,0,sw,sh,((int)(60*(1-age/.18f))<<24)|0xFFFFFF);
  int w=Math.min(128,sw/3),h=w*9/16;
  float ease=Math.min(1,age/.22f);ease=1-(1-ease)*(1-ease)*(1-ease);
  float out=Math.max(0,(age-1.8f)/.2f);
  int x=sw-w-10+(int)((w+16)*(1-ease+out)),y=sh-h-24;
  g.fill(x-3,y-3,x+w+3,y+h+17,0xE0202020);
  g.blit(RenderPipelines.GUI_TEXTURED,TEXTURE,x,y,0f,0f,w,h,320,180,320,180);
  g.text(mc.font,"Screenshot captured",x+2,y+h+4,0xFFFFFFFF);
 }
 private ScreenshotFeedback(){}
}
