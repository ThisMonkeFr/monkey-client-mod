package gg.monkeyclient.ui;
import gg.monkeyclient.config.ModProfiles;
import gg.monkeyclient.integration.LauncherBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import com.google.gson.*;
public class ProfileCodeScreen extends Screen {
 private final Screen parent;private final ModProfiles profiles;private final int index;private EditBox code;private MenuButton submit;private String status="Use 4-24 letters, numbers or hyphens.";
 public ProfileCodeScreen(Screen parent,ModProfiles profiles,int index){super(Component.literal(index<0?"Import profile":"Share profile"));this.parent=parent;this.profiles=profiles;this.index=index;}
 @Override protected void init(){code=addRenderableWidget(new EditBox(font,width/2-120,height/2-10,240,20,Component.literal("Profile code")));code.setMaxLength(24);code.setHint(Component.literal("Profile code"));submit=addRenderableWidget(new MenuButton(width/2-120,height/2+20,116,20,index<0?"Import":"Publish code",this::submit));addRenderableWidget(new MenuButton(width/2+4,height/2+20,116,20,"Back",this::onClose));setFocused(code);}
 private void submit(){String value=code.getValue().strip().toUpperCase(java.util.Locale.ROOT);if(!value.matches("[A-Z0-9][A-Z0-9-]{3,23}")){status="Use 4-24 letters, numbers or hyphens.";return;}submit.active=false;status="Connecting...";JsonObject body=new JsonObject();body.addProperty("code",value);if(index>=0)body.add("profile",JsonParser.parseString(profiles.export(index)));
  LauncherBridge.json(index<0?"/profile/import":"/profile/share",body).whenComplete((result,error)->Minecraft.getInstance().execute(()->{submit.active=true;if(error!=null){status=error.getCause()==null?error.getMessage():error.getCause().getMessage();return;}try{if(index<0){profiles.importProfile(result.get("profile").toString());status="Imported. Choose Use profile to apply it.";}else{minecraft.keyboardHandler.setClipboard(value);status="Code copied: "+value;}}catch(Exception ex){status=ex.getMessage();}}));
 }
 @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float dt){g.fill(0,0,width,height,0xEE202020);g.centeredText(font,index<0?"Import by code":"Share a saved profile",width/2,height/2-65,0xFFFFFFFF);g.centeredText(font,index<0?"Enter a code someone shared with you.":"Anyone with your code can import this profile.",width/2,height/2-47,0xFFCCCCCC);g.centeredText(font,font.plainSubstrByWidth(status,width-20),width/2,height/2+57,0xFFFFFFFF);super.extractRenderState(g,mx,my,dt);}
 @Override public void onClose(){minecraft.setScreenAndShow(parent);}
 @Override public boolean isPauseScreen(){return false;}
}
