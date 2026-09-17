package gg.monkeyclient.hud;

import gg.monkeyclient.module.Category;
import gg.monkeyclient.module.Module;
import gg.monkeyclient.module.setting.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import java.util.List;

/**
 * A module that draws on screen. Every HUD module gets the same set of
 * placement and styling settings, so the editor can treat them identically —
 * which is the whole point of the universal HUD editor.
 */
public abstract class HudElement extends Module {
    public final BoolSetting themeColors = add(new BoolSetting("themeColors", "Match interface theme colors", true));
    public final BoolSetting shown=add(new BoolSetting("shown","HUD visible",true));
    public final NumberSetting customWidth=add(new NumberSetting("customWidth","Width (0 = automatic)",0,0,600,1));
    public final NumberSetting customHeight=add(new NumberSetting("customHeight","Height (0 = automatic)",0,0,400,1));
    public final NumberSetting textSize=add(new NumberSetting("textSize","Text size",1,.5,3,.1));
    public int contentWidth(Minecraft mc){return customWidth.getInt()>0?customWidth.getInt():width(mc);}
    public int contentHeight(Minecraft mc){return customHeight.getInt()>0?customHeight.getInt():height(mc);}
    protected int textWidth(Minecraft mc,String s){return (int)Math.ceil(mc.font.width(s)*textSize.get());}
    protected int textHeight(Minecraft mc){return (int)Math.ceil(mc.font.lineHeight*textSize.get());}
    public int foregroundColor() { return themeColors.get() ? gg.monkeyclient.MonkeyClient.theme().text : textCol.resolve(); }
    public int backgroundColor() { return themeColors.get() ? (bgCol.get() & 0xFF000000) | (gg.monkeyclient.MonkeyClient.theme().panel & 0xFFFFFF) : bgCol.resolve(); }
    public int borderColor() { return themeColors.get() ? (borderCol.get() & 0xFF000000) | (gg.monkeyclient.MonkeyClient.theme().accent & 0xFFFFFF) : borderCol.resolve(); }

    public final NumberSetting x        = add(new NumberSetting("x", "X position", 0.02, 0, 1, 0.001));
    public final NumberSetting y        = add(new NumberSetting("y", "Y position", 0.02, 0, 1, 0.001));
    public final NumberSetting scale    = add(new NumberSetting("scale", "Scale", 1.0, 0.4, 4.0, 0.05));
    public final ColorSetting  textCol  = add(new ColorSetting("textColor", "Text colour", 0xFFFFFFFF));
    public final BoolSetting   shadow   = add(new BoolSetting("shadow", "Text shadow", true));
    public final BoolSetting   bg       = add(new BoolSetting("background", "Background", true));
    public final ColorSetting  bgCol    = add(new ColorSetting("bgColor", "Background colour", 0x80000000));
    public final BoolSetting   border   = add(new BoolSetting("border", "Border", false));
    public final ColorSetting  borderCol= add(new ColorSetting("borderColor", "Border colour", 0xFF7CC24A));
    public final NumberSetting padding  = add(new NumberSetting("padding", "Padding", 3, 0, 16, 1));
    public final EnumSetting   align    = add(new EnumSetting("align", "Alignment", "Left",
                                              List.of("Left", "Centre", "Right")));

    protected HudElement(String id, String name, String description, boolean onByDefault) {
        super(id, name, description, Category.HUD, onByDefault);
    }

    /** Unscaled content size, used for placement, dragging and backgrounds. */
    public abstract int width(Minecraft mc);
    public abstract int height(Minecraft mc);

    /** Draw at the origin; the manager has already translated and scaled. */
    public abstract void render(GuiGraphicsExtractor g, Minecraft mc);
    public boolean visible(Minecraft mc) { return true; }

    /* --- resolved screen geometry --- */
    public int screenX(Minecraft mc) {
        int w = mc.getWindow().getGuiScaledWidth();
        int offset = align.get().equals("Centre") ? scaledWidth(mc)/2 : align.get().equals("Right") ? scaledWidth(mc) : 0;
        return Math.max(0,Math.min(Math.max(0,w-scaledWidth(mc)),(int)Math.round(x.get()*w)-offset));
    }
    public int screenY(Minecraft mc) {
        int h = mc.getWindow().getGuiScaledHeight();
        return Math.max(0,Math.min(Math.max(0,h-scaledHeight(mc)),(int)Math.round(y.get()*h)));
    }
    public int scaledWidth(Minecraft mc)  { return (int) (contentWidth(mc)  * scale.get()); }
    public int scaledHeight(Minecraft mc) { return (int) (contentHeight(mc) * scale.get()); }

    /**
     * The extractor draws text without a shadow flag, so a shadow is a second
     * pass offset by one pixel — which is what vanilla does anyway.
     */
    protected void text(GuiGraphicsExtractor g, Minecraft mc, String s, int x, int y, int argb, boolean drop) {
        g.pose().pushMatrix();g.pose().translate(x,y);g.pose().scale(textSize.getFloat(),textSize.getFloat());
        g.text(mc.font,s,0,0,argb,drop);g.pose().popMatrix();
    }

    /** Sample text used by the editor so an empty HUD is still draggable. */
    public String previewLabel() { return name; }
}
