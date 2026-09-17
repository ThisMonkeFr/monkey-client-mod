package gg.monkeyclient.modules;
import gg.monkeyclient.MonkeyClient;
import gg.monkeyclient.module.Category;
import gg.monkeyclient.module.Module;
import gg.monkeyclient.module.setting.*;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
public class Brightness extends Module {
    public final NumberSetting defaultGamma = add(new NumberSetting("defaultGamma", "Default gamma (%)", 100, 0, 2000, 1));
    public final NumberSetting toggledGamma = add(new NumberSetting("toggledGamma", "Toggled gamma (%)", 500, 0, 2000, 1));
    public final BoolSetting boosted = add(new BoolSetting("boosted", "Use toggled gamma", true));
    public final KeySetting key = add(new KeySetting("key", "Toggle gamma key", GLFW.GLFW_KEY_G));
    public final BoolSetting smooth = add(new BoolSetting("smooth", "Enable smooth transition", false));
    public final NumberSetting speed = add(new NumberSetting("speed", "Transition speed (% / second)", 3000, 1, 10000, 1));
    private boolean wasDown;
    private double current = Double.NaN;
    private long last;
    public Brightness() { super("brightness", "Brightness", "Toggle between two gamma levels", Category.VISUAL, false); }
    @Override public void onTick() {
        var mc = Minecraft.getInstance();
        boolean down = MonkeyClient.inGame(mc.getWindow().handle()) && key.down(mc);
        if (down && !wasDown) { boosted.toggle(); MonkeyClient.saveConfig(); }
        wasDown = down;
    }
    @Override public void onToggle(boolean on) { current = Double.NaN; last = 0; wasDown = false; }
    public double effective() {
        double target = (boosted.get() ? toggledGamma.get() : defaultGamma.get()) / 100.0;
        long now = System.nanoTime();
        if (Double.isNaN(current)) current = defaultGamma.get() / 100.0;
        double dt = last == 0 ? 0 : Math.min(0.25, (now - last) / 1e9);
        last = now;
        if (!smooth.get()) current = target;
        else { double step = speed.get() / 100.0 * dt; current += Math.max(-step, Math.min(step, target - current)); }
        return current;
    }
}