package gg.monkeyclient.ui;
import gg.monkeyclient.MonkeyClient;
import gg.monkeyclient.module.setting.NumberSetting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
public class SettingSlider extends AbstractSliderButton {
    private final NumberSetting setting;
    private final Runnable changed;
    public SettingSlider(int x,int y,int w,NumberSetting setting,Runnable changed) {
        super(x,y,w,18,Component.literal(setting.label),(setting.get()-setting.min)/(setting.max-setting.min));
        this.setting=setting; this.changed=changed;
    }
    @Override protected void updateMessage() {}
    @Override protected void applyValue() { if(setting!=null) { setting.set(setting.min+value*(setting.max-setting.min)); changed.run(); } }
    @Override public void extractWidgetRenderState(GuiGraphicsExtractor g,int mx,int my,float dt) {
        var t=MonkeyClient.theme();
        int x=getX(),y=getY()+8,w=getWidth();
        double progress=(setting.get()-setting.min)/(setting.max-setting.min);
        value=progress;
        if(t.vanilla){
            VanillaDraw.sprite(g,isHoveredOrFocused()?"slider_highlighted":"slider",x,getY(),w,getHeight());
            int handle=x+(int)((w-8)*progress);
            VanillaDraw.sprite(g,isHoveredOrFocused()?"slider_handle_highlighted":"slider_handle",handle,getY(),8,getHeight());return;
        }
        g.fill(x,y,x+w,y+2,t.line());
        g.fill(x,y,x+(int)(w*progress),y+2,t.slider());
        int handle=x+(int)((w-4)*progress);
        g.fill(handle,y-4,handle+4,y+6,isHoveredOrFocused()?t.text:t.slider());
    }
}
