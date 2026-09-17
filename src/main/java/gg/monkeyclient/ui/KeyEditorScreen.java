package gg.monkeyclient.ui;
import gg.monkeyclient.modules.Keystrokes;
import gg.monkeyclient.MonkeyClient;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
public class KeyEditorScreen extends Screen {
 private final Screen parent;private final Keystrokes module;private int scroll;
 public KeyEditorScreen(Screen parent,Keystrokes module){super(Component.literal("Keys"));this.parent=parent;this.module=module;}
 @Override protected void init(){clearWidgets();int left=width/2-140;
  addRenderableWidget(new MenuButton(left,14,140,22,"ADD KEY",()->{module.addKey();init();},()->false));
  addRenderableWidget(new MenuButton(left+150,14,130,22,"DONE",this::onClose,()->true));
  for(int i=scroll;i<Math.min(module.keys.get().size(),scroll+Math.max(1,(height-60)/28));i++){int index=i,y=48+(i-scroll)*28;var k=module.keys.get().get(i).getAsJsonObject();
   addRenderableWidget(new MenuButton(left,y,210,22,k.has("label")?k.get("label").getAsString():"KEY",()->minecraft.setScreenAndShow(new MonkeyMenuScreen().showSettings(module.settingsFor(index),this)),()->false));
   addRenderableWidget(new MenuButton(left+216,y,64,22,"DELETE",()->{module.keys.get().remove(index);init();},()->false));
  }
 }
 @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float d){g.fill(0,0,width,height,0xEE101010);super.extractRenderState(g,mx,my,d);}
 @Override public boolean mouseScrolled(double x,double y,double dx,double dy){scroll=Math.max(0,Math.min(Math.max(0,module.keys.get().size()-1),scroll-(int)dy));init();return true;}
 @Override public void onClose(){MonkeyClient.saveConfig();minecraft.setScreenAndShow(parent);}
 @Override public boolean isPauseScreen(){return false;}
}
