package gg.monkeyclient.ui;
import gg.monkeyclient.MonkeyClient;
import gg.monkeyclient.config.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import java.util.function.BooleanSupplier;
public class MenuButton extends AbstractWidget {
    private final Runnable action;
    private final BooleanSupplier selected;
    private float hover;
    private long last=System.nanoTime();
    public MenuButton(int x,int y,int w,int h,String title,Runnable action,BooleanSupplier selected) {
        super(x,y,w,h,Component.literal(title)); this.action=action; this.selected=selected;
    }
    public MenuButton(int x,int y,int w,int h,String title,Runnable action) { this(x,y,w,h,title,action,()->false); }
    @Override public void onClick(MouseButtonEvent e,boolean twice) { action.run(); }
    @Override public boolean keyPressed(KeyEvent e) {
        if (active && isFocused() && (e.key()==257 || e.key()==32 || e.key()==335)) { playDownSound(Minecraft.getInstance().getSoundManager()); action.run(); return true; }
        return false;
    }
    @Override protected void extractWidgetRenderState(GuiGraphicsExtractor g,int mx,int my,float dt) {
        Theme t=MonkeyClient.theme();
        long now=System.nanoTime(); float a=t.animations ? (float)(1-Math.exp(-(now-last)/1e9*18)) : 1; last=now;
        hover+=((isHoveredOrFocused()?1:0)-hover)*a;
        int x=getX(),y=getY(),w=getWidth(),h=getHeight();
        if(t.vanilla){
            VanillaDraw.sprite(g,!active?"button_disabled":isHoveredOrFocused()?"button_highlighted":"button",x,y,w,h);
            if(selected.getAsBoolean()){g.fill(x+2,y+h-3,x+w-2,y+h-1,t.toggle());}
            var font=Minecraft.getInstance().font;var lines=font.split(getMessage(),Math.max(1,w-8));
            var label=lines.isEmpty()?net.minecraft.util.FormattedCharSequence.EMPTY:lines.getFirst();
            g.text(font,label,x+(w-font.width(label))/2,y+(h-8)/2,active?0xFFFFFFFF:0xFFA0A0A0,true);return;
        }
        int fill=Theme.blend(t.button(),t.highlight(),hover*.16f+(selected.getAsBoolean()?.10f:0));
        g.fill(x,y,x+w,y+h,colour(fill));
        MonkeyMenuScreen.outline(g,x,y,x+w,y+h,colour(selected.getAsBoolean()?t.toggle():t.line()));
        g.fill(x+2,y+2,x+w-2,y+3,colour(Theme.blend(fill,0xFFFFFFFF,.13f)));
        g.fill(x+2,y+h-3,x+w-2,y+h-2,colour(0xFF101010));
        var font=Minecraft.getInstance().font;
        var lines=font.split(getMessage(),Math.max(1,w-8));
        var label=lines.isEmpty()?net.minecraft.util.FormattedCharSequence.EMPTY:lines.getFirst();
        g.text(font,label,x+(w-font.width(label))/2,y+(h-8)/2,colour(selected.getAsBoolean()?t.toggle():t.text),false);
    }
    private int colour(int c) { return ((int)(((c>>>24)&255)*alpha)<<24)|(c&0xFFFFFF); }
    @Override protected void updateWidgetNarration(NarrationElementOutput o) { o.add(NarratedElementType.TITLE,getMessage()); defaultButtonNarrationText(o); }
}
