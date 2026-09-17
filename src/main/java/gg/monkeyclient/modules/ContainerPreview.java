package gg.monkeyclient.modules;
import gg.monkeyclient.module.Module;
import gg.monkeyclient.module.Category;
import gg.monkeyclient.module.setting.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.*;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import org.lwjgl.glfw.GLFW;
import java.util.*;
/** Vanilla nine-slice artwork adapted from ShulkerBoxTooltip (MIT). */
public class ContainerPreview extends Module {
 private final BoolSetting colors=add(new BoolSetting("colors","Use shulker colors",true));
 private final BoolSetting loot=add(new BoolSetting("loot","Show loot table name",false));
 private final BoolSetting always=add(new BoolSetting("always","Preview without Shift",true));
 public final KeySetting inspectKey=add(new KeySetting("inspectKey","Hold with Alt to inspect contents",GLFW.GLFW_KEY_LEFT_CONTROL));
 private Preview pinned;
 private ItemStack pinnedStack=ItemStack.EMPTY;
 private net.minecraft.client.gui.screens.Screen pinnedScreen;
 private int pinnedX,pinnedY;
 private static final Identifier FRAME=Identifier.fromNamespaceAndPath("monkeyclient","shulker_box_tooltip");
 public ContainerPreview(){super("containers","Container Preview","Preview contents; hold Alt for slots and the inspect key for item tooltips.",Category.UTILITY,false);}
 private boolean inspectingKeys(){return key(GLFW.GLFW_KEY_LEFT_ALT,GLFW.GLFW_KEY_RIGHT_ALT)&&inspectKey.down(Minecraft.getInstance());}
 public boolean inspecting(){return pinned!=null&&inspectingKeys()&&Minecraft.getInstance().gui.screen()==pinnedScreen;}
 @Override public void onTick(){if(!inspecting())clearPin();}
 @Override public void onToggle(boolean enabled){if(!enabled)clearPin();}
 private void clearPin(){pinned=null;pinnedScreen=null;pinnedStack=ItemStack.EMPTY;}
 public boolean drawPinned(GuiGraphicsExtractor g,int mx,int my){
  if(!inspecting()){clearPin();return false;}
  var font=Minecraft.getInstance().font;int w=pinned.getWidth(font),h=pinned.getHeight(font);
  g.fill(pinnedX-4,pinnedY-17,pinnedX+w+4,pinnedY+h+4,0xFF202020);
  g.fill(pinnedX-3,pinnedY-16,pinnedX+w+3,pinnedY-2,0xFFC6C6C6);
  g.text(font,font.plainSubstrByWidth(pinnedStack.getHoverName().getString(),w-4),pinnedX,pinnedY-13,0xFF303030,false);
  pinned.extractImage(font,pinnedX,pinnedY,w,h,g);
  var hovered=pinned.at((mx-pinnedX)/pinned.scale,(my-pinnedY)/pinned.scale);
  if(!hovered.isEmpty())g.setTooltipForNextFrame(font,hovered,mx,my);
  return true;
 }
 private boolean key(int a,int b){long w=Minecraft.getInstance().getWindow().handle();return GLFW.glfwGetKey(w,a)==GLFW.GLFW_PRESS||GLFW.glfwGetKey(w,b)==GLFW.GLFW_PRESS;}
 public static List<ItemStack> compact(List<ItemStack> slots){
  List<ItemStack> result=new ArrayList<>();
  for(var stack:slots){if(stack.isEmpty())continue;ItemStack match=null;for(var existing:result)if(ItemStack.isSameItemSameComponents(stack,existing)){match=existing;break;}
   if(match==null)result.add(stack.copy());else match.setCount(match.getCount()+stack.getCount());
  }return result;
 }
 public boolean draw(GuiGraphicsExtractor g,ItemStack stack,int mx,int my){
  if(!always.get()&&!key(GLFW.GLFW_KEY_LEFT_SHIFT,GLFW.GLFW_KEY_RIGHT_SHIFT))return false;
  var mc=Minecraft.getInstance();var contents=stack.get(DataComponents.CONTAINER);var table=stack.get(DataComponents.CONTAINER_LOOT);
  if(contents==null&&(!loot.get()||table==null))return false;
  List<ItemStack> slots=contents==null?new ArrayList<>():new ArrayList<>(contents.allItemsCopyStream().limit(256).toList());
  boolean shulker=stack.getItem() instanceof BlockItem b&&b.getBlock() instanceof ShulkerBoxBlock;
  boolean full=key(GLFW.GLFW_KEY_LEFT_ALT,GLFW.GLFW_KEY_RIGHT_ALT);
  if(full&&shulker)while(slots.size()<27)slots.add(ItemStack.EMPTY);
  if(!full)slots=compact(slots);
  int tint=0xFFFFFFFF;
  if(colors.get()&&stack.getItem() instanceof BlockItem b&&b.getBlock() instanceof ShulkerBoxBlock box)tint=0xFF000000|(box.getColor()==null?0x976797:box.getColor().getTextureDiffuseColor());
  if(full&&inspectingKeys()&&!slots.isEmpty()){
   pinned=new Preview(slots,tint,g.guiWidth(),g.guiHeight());pinnedStack=stack.copy();pinnedScreen=mc.gui.screen();
   pinnedX=Math.max(6,Math.min(mx+12,g.guiWidth()-pinned.getWidth(mc.font)-6));
   pinnedY=Math.max(22,Math.min(my-10,g.guiHeight()-pinned.getHeight(mc.font)-6));
   return drawPinned(g,mx,my);
  }
  List<ClientTooltipComponent> lines=new ArrayList<>();
  lines.add(ClientTooltipComponent.create(stack.getHoverName().getVisualOrderText()));
  if(!slots.isEmpty())lines.add(new Preview(slots,tint,g.guiWidth(),g.guiHeight()));
  else lines.add(ClientTooltipComponent.create(Component.literal(table==null?"Empty":"Contents not generated").withColor(0xAAAAAA).getVisualOrderText()));
  if(loot.get()&&table!=null)lines.add(ClientTooltipComponent.create(Component.literal("Loot: "+table.lootTable().identifier()).getVisualOrderText()));
  lines.add(ClientTooltipComponent.create(Component.literal(full?"Full slots · release Alt for totals":"Merged totals · hold Alt for full slots").withColor(0xAAAAAA).getVisualOrderText()));
  g.tooltip(mc.font,lines,mx,my,DefaultTooltipPositioner.INSTANCE,null);return true;
 }
 private static final class Preview implements ClientTooltipComponent {
  private final List<ItemStack> items;private final int tint,cols,w,h;private final float scale;
  Preview(List<ItemStack> items,int tint,int screenW,int screenH){this.items=items;this.tint=tint;cols=Math.min(9,items.size());w=14+cols*18;h=14+((items.size()+cols-1)/cols)*18;scale=Math.min(1,Math.min((screenW-24f)/w,(screenH-70f)/h));}
  public int getWidth(Font f){return (int)Math.ceil(w*scale);}
  public int getHeight(Font f){return (int)Math.ceil(h*scale)+2;}
  ItemStack at(float x,float y){int col=(int)Math.floor((x-8)/18),row=(int)Math.floor((y-8)/18);int index=row*cols+col;return col>=0&&col<cols&&row>=0&&index<items.size()?items.get(index):ItemStack.EMPTY;}
  public void extractImage(Font f,int x,int y,int width,int height,GuiGraphicsExtractor g){
   g.pose().pushMatrix();g.pose().translate(x,y);g.pose().scale(scale,scale);
   g.blitSprite(RenderPipelines.GUI_TEXTURED,FRAME,0,0,w,h,tint);
   for(int i=0;i<items.size();i++){int sx=8+i%cols*18,sy=8+i/cols*18;var item=items.get(i);if(!item.isEmpty()){g.item(item,sx,sy);g.itemDecorations(f,item,sx,sy,item.getCount()>1?Integer.toString(item.getCount()):null);}}
   g.pose().popMatrix();
  }
 }
}
