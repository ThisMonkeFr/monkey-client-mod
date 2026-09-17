package gg.monkeyclient.ui;
import gg.monkeyclient.config.ModProfiles;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
public class PresetsScreen extends Screen {
 private final Screen parent;private final ModProfiles profiles;private String status="Adds a copy and applies your selected preset.";
 public PresetsScreen(Screen parent,ModProfiles profiles){super(Component.literal("Profile presets"));this.parent=parent;this.profiles=profiles;}
 @Override protected void init(){int y=height/2-24;for(String preset:java.util.List.of("PvP","Hoplite")){addRenderableWidget(new MenuButton(width/2-100,y,200,20,preset,()->{try{profiles.preset(preset);onClose();}catch(Exception e){status=e.getMessage();}}));y+=26;}addRenderableWidget(new MenuButton(width/2-100,y+10,200,20,"Back",this::onClose));}
 @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float dt){g.fill(0,0,width,height,0xEE202020);g.centeredText(font,"Profile presets",width/2,height/2-65,0xFFFFFFFF);g.centeredText(font,font.plainSubstrByWidth(status,width-20),width/2,height/2-45,0xFFCCCCCC);super.extractRenderState(g,mx,my,dt);}
 @Override public void onClose(){minecraft.setScreenAndShow(parent);}
 @Override public boolean isPauseScreen(){return false;}
}
