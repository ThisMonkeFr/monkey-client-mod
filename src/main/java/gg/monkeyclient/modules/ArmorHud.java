package gg.monkeyclient.modules;
import gg.monkeyclient.hud.HudElement;
import gg.monkeyclient.module.setting.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import java.util.List;
public class ArmorHud extends HudElement {
    private final EnumSetting style=add(new EnumSetting("style","Style","Minecraft slots",List.of("Minecraft slots","Separate slots","Minimal")));
    private final BoolSetting vertical=add(new BoolSetting("vertical","Stack vertically",false));
    private final BoolSetting durability=add(new BoolSetting("durability","Durability bars",true));
    private final BoolSetting hideEmpty=add(new BoolSetting("hideWhenEmpty","Hide when no armor is worn",true));
    private final BoolSetting warning=add(new BoolSetting("warning","Animated low durability warning",true));
    private final NumberSetting threshold=add(new NumberSetting("threshold","Warning below (%)",20,1,100,1));
    private static final EquipmentSlot[] SLOTS={EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET};
    private static final String[] ICONS={"helmet","chestplate","leggings","boots"};
    private static final Identifier FRAME=Identifier.withDefaultNamespace("hud/hotbar_offhand_left");
    private static final Identifier WARN=Identifier.fromNamespaceAndPath("monkeyclient","textures/armor_warning.png");
    public ArmorHud(){super("armor","Armor HUD","Vanilla armor slots, durability and animated alerts",false);settings().remove(textSize);settings().remove(textCol);settings().remove(shadow);bg.setDefault(false);padding.setDefault(0d);x.setDefault(.02);y.setDefault(.82);}
    private ItemStack stack(Minecraft mc,int i){return mc.player==null?ItemStack.EMPTY:mc.player.getItemBySlot(SLOTS[i]);}
    @Override public boolean visible(Minecraft mc){if(!hideEmpty.get())return true;for(int i=0;i<4;i++)if(!stack(mc,i).isEmpty())return true;return false;}
    private int step(){return style.get().equals("Separate slots")?24:20;}
    @Override public int width(Minecraft mc){return vertical.get()?22:22+3*step();}
    @Override public int height(Minecraft mc){return 12+(vertical.get()?22+3*step():22);}
    @Override public void render(GuiGraphicsExtractor g,Minecraft mc){
        boolean joined=style.get().equals("Minecraft slots")&&!vertical.get();
        if(joined){
            g.blitSprite(RenderPipelines.GUI_TEXTURED,FRAME,29,24,0,1,0,12,21,22);
            for(int i=1;i<3;i++)g.blitSprite(RenderPipelines.GUI_TEXTURED,FRAME,29,24,1,1,1+i*20,12,20,22);
            g.blitSprite(RenderPipelines.GUI_TEXTURED,FRAME,29,24,1,1,61,12,21,22);
        }
        for(int i=0;i<4;i++){
            int sx=vertical.get()?0:i*step(),sy=12+(vertical.get()?i*step():0);
            if(!style.get().equals("Minimal")&&!joined){
                // Native offhand frame crop, exactly 22x22. Shared edges join at 20px.
                g.blitSprite(RenderPipelines.GUI_TEXTURED,FRAME,29,24,0,1,sx,sy,22,22);
            }
            ItemStack st=stack(mc,i);
            if(st.isEmpty())g.blitSprite(RenderPipelines.GUI_TEXTURED,Identifier.withDefaultNamespace("container/slot/"+ICONS[i]),sx+3,sy+3,16,16);
            else {
                g.item(st,sx+3,sy+3);
                if(durability.get())g.itemDecorations(mc.font,st,sx+3,sy+3);
                if(warning.get()&&st.isDamageableItem()&&(st.getMaxDamage()-st.getDamageValue())*100d/st.getMaxDamage()<=threshold.get()){
                    int bounce=(int)Math.round(Math.abs(Math.sin(System.nanoTime()/1e9*6))*3);
                    g.blit(RenderPipelines.GUI_TEXTURED,WARN,sx+7,sy-9-bounce,0,0,8,8,8,8);
                }
            }
        }
    }
}
