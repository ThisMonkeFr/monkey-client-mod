package gg.monkeyclient.ui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
public final class NativeImageScreen extends NativeScreen {
 private final String data;
 public NativeImageScreen(Screen parent,String name,String data){super(parent,name);this.data=data;}
 @Override protected void onOpened(){picture("image",data);}
 @Override protected void rebuild(){clearWidgets();button(left+pw-62,top+8,54,"Back",this::onClose);}
 @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float dt){super.extractRenderState(g,mx,my,dt);drawPicture(g,"image",left+10,top+36,pw-20,ph-54);}
}
