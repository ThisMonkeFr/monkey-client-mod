package gg.monkeyclient.modules;
import gg.monkeyclient.hud.HudElement;
import gg.monkeyclient.module.setting.*;
import gg.monkeyclient.ui.MonkeyMenuScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
public class FpsDisplay extends HudElement {
 private final BoolSetting counter=add(new BoolSetting("counter","Show FPS counter",true));
 private final BoolSetting label=add(new BoolSetting("label","Show FPS suffix",true));
 public final Graph graphElement=new Graph();
 private final ModuleSetting graphSettings=add(new ModuleSetting("graphSettings","FPS graph",graphElement));
 private final int[] history=new int[240];private int head;
 public FpsDisplay(){super("fps","FPS Display","Independent counter and draggable FPS graph",true);}
 @Override public void onTick(){history[head]=Minecraft.getInstance().getFps();head=(head+1)%history.length;}
 @Override public Screen editor(Screen parent){return new MonkeyMenuScreen().showSettings(graphElement,parent);}
 @Override public String editorLabel(){return "EDIT FPS GRAPH";}
 private String text(Minecraft mc){return mc.getFps()+(label.get()?" FPS":"");}
 @Override public boolean visible(Minecraft mc){return counter.get();}
 @Override public int width(Minecraft mc){return textWidth(mc,text(mc));}
 @Override public int height(Minecraft mc){return textHeight(mc);}
 @Override public void render(GuiGraphicsExtractor g,Minecraft mc){text(g,mc,text(mc),0,0,foregroundColor(),shadow.get());}
 public class Graph extends HudElement {
  private final NumberSetting w=add(new NumberSetting("width","Graph width",100,20,240,1)),h=add(new NumberSetting("height","Graph height",35,8,100,1));
  private final ColorSetting color=add(new ColorSetting("graphColor","Graph color",0xFF7CC24A));
  public Graph(){super("fpsGraph","FPS Graph","Drag and style this graph separately from the counter",false);settings().remove(textSize);settings().remove(textCol);settings().remove(shadow);x.setDefault(.02);y.setDefault(.07);}
  @Override public boolean isEnabled(){return FpsDisplay.this.isEnabled()&&enabled.get();}
  @Override public int width(Minecraft mc){return w.getInt();}
  @Override public int height(Minecraft mc){return h.getInt();}
  @Override public void render(GuiGraphicsExtractor g,Minecraft mc){
   int peak=1;for(int v:history)peak=Math.max(peak,v);int color=themeColors.get()?gg.monkeyclient.MonkeyClient.theme().accent:this.color.resolve();
   for(int i=0;i<w.getInt();i++){int v=history[Math.floorMod(head-1-i,history.length)];int bar=Math.max(1,Math.round(v/(float)peak*h.getInt()));int x=w.getInt()-1-i;g.fill(x,h.getInt()-bar,x+1,h.getInt(),color);}
  }
 }
}
