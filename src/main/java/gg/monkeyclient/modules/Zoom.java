package gg.monkeyclient.modules;
import gg.monkeyclient.MonkeyClient;
import gg.monkeyclient.module.Category;
import gg.monkeyclient.module.Module;
import gg.monkeyclient.module.setting.*;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import java.util.List;
public class Zoom extends Module {
    public final NumberSetting divisor = add(new NumberSetting("divisor", "Zoom amount", 4, 1.5, 50, 0.5));
    public final KeySetting key = add(new KeySetting("key", "Zoom key", GLFW.GLFW_KEY_C));
    public final EnumSetting mode = add(new EnumSetting("mode", "Behaviour", "Hold", List.of("Hold", "Toggle")));
    public final NumberSetting smooth = add(new NumberSetting("smooth", "Transition time (seconds)", 0.15, 0, 0.6, 0.01));
    public final BoolSetting scroll = add(new BoolSetting("scroll", "Scroll to adjust zoom", true));
    private boolean toggled, wasDown;
    private double factor = 1, scrollDivisor = 4, visualScale = 1, crosshairOpacity = 1;
    private long lastFrame;
    public Zoom() { super("zoom", "Zoom", "Smooth world zoom with scroll control", Category.UTILITY, true); }
    @Override public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        if (!MonkeyClient.inGame(mc.getWindow().handle()) || mc.player == null) { toggled = false; wasDown = false; return; }
        boolean down = key.down(mc), before = toggled;
        toggled = mode.get().equals("Toggle") ? (down && !wasDown ? !toggled : toggled) : down;
        if (toggled && !before) scrollDivisor = divisor.get();
        wasDown = down;
    }
    @Override public void onToggle(boolean on) { toggled = false; wasDown = false; factor = visualScale = crosshairOpacity = 1; lastFrame = 0; }
    public boolean active() { return isEnabled() && toggled; }
    public void advanceFrame() {
        long now = System.nanoTime();
        double dt = lastFrame == 0 ? 0 : Math.min(0.1, (now - lastFrame) / 1e9); lastFrame = now;
        double amount = smooth.get() <= 0 ? 1 : 1 - Math.exp(-dt * 5 / smooth.get());
        factor += ((active() ? 1 / scrollDivisor : 1) - factor) * amount;
        crosshairOpacity += ((active() ? 0 : 1) - crosshairOpacity) * (1 - Math.exp(-dt / (active() ? .035 : .10)));
        if (crosshairOpacity < .002) crosshairOpacity = 0;
        if (crosshairOpacity > .998) crosshairOpacity = 1;
    }
    public double fovFactor() { return factor; }
    public float crosshairAlpha() { return (float)crosshairOpacity; }
    public float visualScale() { return (float)visualScale; }
    public float zoomFov(float original) {
        float result=(float)(original*factor);
        visualScale=Math.tan(Math.toRadians(original)*.5)/Math.tan(Math.toRadians(result)*.5);
        return result;
    }
    public float handFov(float original) {
        return (float)Math.toDegrees(2*Math.atan(Math.tan(Math.toRadians(original)*.5)/visualScale));
    }
    public boolean scroll(double delta) {
        if (!active()) return false;
        if (scroll.get()) scrollDivisor = Math.max(1.5, Math.min(50, scrollDivisor * Math.pow(1.15, delta)));
        return true;
    }
}
