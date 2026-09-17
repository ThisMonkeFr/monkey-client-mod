package gg.monkeyclient.modules;
import gg.monkeyclient.module.Category;
import gg.monkeyclient.module.Module;
import gg.monkeyclient.module.setting.NumberSetting;
import net.minecraft.client.Minecraft;
public class GuiScale extends Module {
    public final NumberSetting scale = add(new NumberSetting("scale", "GUI scale", 3, 1, 10, .1));
    private double applied = -1;
    public GuiScale() { super("guiscale", "GUI Scale", "Adjust the interface scale with live resize", Category.MINECRAFT, false); }
    public double effectiveScale() {
        var mc=Minecraft.getInstance();var screen=mc.gui.screen();
        if(screen!=null&&screen.getClass().getPackageName().equals("gg.monkeyclient.ui")&&!(screen instanceof gg.monkeyclient.ui.HudEditorScreen))
            return Math.min(scale.get(),Math.max(1,Math.min(mc.getWindow().getWidth()/480d,mc.getWindow().getHeight()/320d)));
        return scale.get();
    }
    @Override public void onTick() {
        var mc=Minecraft.getInstance();
        if (mc.gui.screen() instanceof gg.monkeyclient.ui.MonkeyMenuScreen && mc.mouseHandler.isLeftPressed()) return;
        if (applied != effectiveScale()) { applied=effectiveScale(); mc.resizeGui(); }
    }
    @Override public void onToggle(boolean on) { applied=-1; Minecraft.getInstance().resizeGui(); }
}
