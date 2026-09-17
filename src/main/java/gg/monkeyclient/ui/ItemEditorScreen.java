package gg.monkeyclient.ui;
import gg.monkeyclient.modules.ItemEditor;
import gg.monkeyclient.MonkeyClient;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import java.util.*;
public class ItemEditorScreen extends Screen {
 private final Screen parent;private final ItemEditor module;private String query="";private int scroll;private List<Item> results=List.of();
 public ItemEditorScreen(Screen parent,ItemEditor module){super(Component.literal("Item Editor"));this.parent=parent;this.module=module;}
 @Override protected void init(){
  clearWidgets();int left=Math.max(10,width/2-260),w=Math.min(width-20,520);
  var search=addRenderableWidget(new EditBox(font,left,12,w-88,20,Component.literal("Search all items")));search.setHint(Component.literal("Search item name or minecraft:id"));search.setValue(query);
  search.setResponder(s->{query=s;scroll=0;init();for(var c:children())if(c instanceof EditBox box){setFocused(box);box.setFocused(true);box.setCursorPosition(s.length());break;}});
  addRenderableWidget(new MenuButton(left+w-80,12,80,20,"DONE",this::onClose,()->true));
  String q=query.toLowerCase(Locale.ROOT);results=BuiltInRegistries.ITEM.stream().filter(item->BuiltInRegistries.ITEM.getKey(item).toString().contains(q)||item.getDefaultInstance().getHoverName().getString().toLowerCase(Locale.ROOT).contains(q)).toList();
  int rows=Math.max(1,(height-65)/27);scroll=Math.min(scroll,Math.max(0,results.size()-rows));
  for(int i=scroll;i<Math.min(results.size(),scroll+rows);i++){var item=results.get(i);String id=BuiltInRegistries.ITEM.getKey(item).toString();int y=44+(i-scroll)*27;
   addRenderableWidget(new MenuButton(left+w-150,y,70,22,"HAND",()->minecraft.setScreenAndShow(new MonkeyMenuScreen().showSettings(module.settingsFor(id,"firstPerson"),this)),()->false));
   addRenderableWidget(new MenuButton(left+w-74,y,74,22,"WORLD",()->minecraft.setScreenAndShow(new MonkeyMenuScreen().showSettings(module.settingsFor(id,"world"),this)),()->false));
  }
 }
 @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float d){
  g.fill(0,0,width,height,0xEE101010);int left=Math.max(10,width/2-260),w=Math.min(width-20,520),rows=Math.max(1,(height-65)/27);
  for(int i=scroll;i<Math.min(results.size(),scroll+rows);i++){var item=results.get(i);int y=44+(i-scroll)*27;g.item(item.getDefaultInstance(),left,y+2);g.text(font,font.plainSubstrByWidth(item.getDefaultInstance().getHoverName().getString(),w-180),left+23,y+3,MonkeyClient.theme().text);g.text(font,font.plainSubstrByWidth(BuiltInRegistries.ITEM.getKey(item).toString(),w-180),left+23,y+13,MonkeyClient.theme().textDim);}
  super.extractRenderState(g,mx,my,d);
 }
 @Override public boolean mouseScrolled(double x,double y,double dx,double dy){scroll=Math.max(0,scroll-(int)dy);init();return true;}
 @Override public void onClose(){module.dirty();MonkeyClient.saveConfig();minecraft.setScreenAndShow(parent);}
 @Override public boolean isPauseScreen(){return false;}
}
