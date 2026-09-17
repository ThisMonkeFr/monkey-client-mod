package gg.monkeyclient.modules;
import gg.monkeyclient.module.Module;
import gg.monkeyclient.module.Category;
import gg.monkeyclient.module.setting.*;
import gg.monkeyclient.MonkeyClient;
import gg.monkeyclient.ui.CrosshairEditorScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import java.util.List;
public class CustomCrosshair extends Module {
 public final EnumSetting shape=add(new EnumSetting("shape","Shape","Cross",List.of("Cross","Circle","Square","Custom drawing")));
 public final NumberSetting size=add(new NumberSetting("size","Arm length / radius",5,1,40,.5)),thickness=add(new NumberSetting("thickness","Thickness",1,1,10,.5)),gap=add(new NumberSetting("gap","Gap",3,0,30,.5));
 public final NumberSetting rotation=add(new NumberSetting("rotation","Rotation (degrees)",0,0,360,1)),posX=add(new NumberSetting("x","Horizontal position",.5,0,1,.001)),posY=add(new NumberSetting("y","Vertical position",.5,0,1,.001));
 public final BoolSetting dot=add(new BoolSetting("dot","Center dot",false)),outline=add(new BoolSetting("outline","Outline",true)),themed=add(new BoolSetting("themed","Match theme",true));
 public final NumberSetting dotSize=add(new NumberSetting("dotSize","Dot size",1,1,10,1)),outlineSize=add(new NumberSetting("outlineSize","Outline thickness",1,1,5,1)),opacity=add(new NumberSetting("opacity","Opacity (%)",100,0,100,1));
 public final ColorSetting color=add(new ColorSetting("color","Crosshair color",0xFFFFFFFF)),outlineColor=add(new ColorSetting("outlineColor","Outline color",0xFF000000));
 public final StringSetting pixels=add(new StringSetting("pixels","Drawing data","0".repeat(1089),1089));
 public CustomCrosshair(){super("crosshair","Custom Crosshair","Draw a crosshair or customize shapes, outlines and position",Category.VISUAL,false);}
 @Override public Screen editor(Screen parent){return new CrosshairEditorScreen(parent,this);}
 @Override public String editorLabel(){return "DRAW CROSSHAIR";}
 public void paintPixel(int x,int y,boolean on){if(x<0||y<0||x>=33||y>=33)return;StringBuilder s=new StringBuilder(pixels.get());while(s.length()<1089)s.append('0');s.setCharAt(y*33+x,on?'1':'0');pixels.set(s.toString());}
 private boolean active(int x,int y){
  double h=thickness.get()/2,r=size.get(),a=Math.abs(x),b=Math.abs(y),d=Math.hypot(x,y);
  boolean mark=switch(shape.get()){
   case "Circle"->Math.abs(d-r)<=h;
   case "Square"->Math.abs(Math.max(a,b)-r)<=h;
   case "Custom drawing"->{int ix=x+16,iy=y+16,index=iy*33+ix;yield ix>=0&&ix<33&&iy>=0&&iy<33&&index<pixels.get().length()&&pixels.get().charAt(index)=='1';}
   default->(a<=h&&b>=gap.get()&&b<gap.get()+r)||(b<=h&&a>=gap.get()&&a<gap.get()+r);
  };
  return mark||(dot.get()&&a<=(dotSize.get()-1)/2&&b<=(dotSize.get()-1)/2);
 }
 public void draw(GuiGraphicsExtractor g,Minecraft mc){
  if(mc.player==null||mc.player.isSpectator())return;
  int radius=shape.get().equals("Custom drawing")?16:(int)Math.ceil(size.get()+gap.get()+thickness.get());
  float alpha=MonkeyClient.modules().get(Zoom.class).crosshairAlpha()*opacity.getFloat()/100;
  int c=(themed.get()?MonkeyClient.theme().accent:color.resolve()),oc=outlineColor.resolve();
  c=((int)((c>>>24)*alpha)<<24)|(c&0xFFFFFF);oc=((int)((oc>>>24)*alpha)<<24)|(oc&0xFFFFFF);
  g.pose().pushMatrix();g.pose().translate((float)(mc.getWindow().getGuiScaledWidth()*posX.get()),(float)(mc.getWindow().getGuiScaledHeight()*posY.get()));g.pose().rotate((float)Math.toRadians(rotation.get()));
  if(outline.get()){int n=outlineSize.getInt();for(int y=-radius;y<=radius;y++)for(int x=-radius;x<=radius;x++)if(active(x,y))g.fill(x-n,y-n,x+n+1,y+n+1,oc);}
  for(int y=-radius;y<=radius;y++)for(int x=-radius;x<=radius;x++)if(active(x,y))g.fill(x,y,x+1,y+1,c);
  g.pose().popMatrix();
 }
}
