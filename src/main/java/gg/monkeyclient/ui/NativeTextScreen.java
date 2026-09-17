package gg.monkeyclient.ui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
public final class NativeTextScreen extends NativeScreen {
 private final String text;private int page,lines;
 public NativeTextScreen(Screen parent,String title,String text){super(parent,title);this.text=text;}
 @Override protected void rebuild(){clearWidgets();lines=Math.max(1,(bottom-top-64)/12);button(left+pw-70,top+8,60,"Back",this::onClose);button(left+12,bottom-22,50,"<",()->{page--;rebuild();}).active=page>0;button(left+68,bottom-22,50,">",()->{page++;rebuild();}).active=(page+1)*lines<font.split(Component.literal(text),pw-28).size();}
 @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float dt){super.extractRenderState(g,mx,my,dt);var wrapped=font.split(Component.literal(text),pw-28);for(int i=page*lines;i<Math.min(wrapped.size(),(page+1)*lines);i++)g.text(font,wrapped.get(i),left+14,top+35+(i-page*lines)*12,gg.monkeyclient.MonkeyClient.theme().ink(),false);}
}
