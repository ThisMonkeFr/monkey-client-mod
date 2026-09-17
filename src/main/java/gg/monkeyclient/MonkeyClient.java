package gg.monkeyclient;

import gg.monkeyclient.config.ConfigManager;
import gg.monkeyclient.config.Theme;
import gg.monkeyclient.module.ModuleManager;
import gg.monkeyclient.ui.MonkeyMenuScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonkeyClient implements ClientModInitializer {
    public static final String MOD_ID = "monkeyclient";
    public static final Logger LOG = LoggerFactory.getLogger("Monkey Client");

    private static ModuleManager modules;
    private static Theme theme;
    private static volatile boolean ready;

    /** Right Shift must be held, not tapped — a tap is often a game keybind. */
    private static final long MENU_HOLD_MS = 300L;
    private static long shiftDownAt = -1;

    @Override
    public void onInitializeClient() {
        theme = new Theme();
        modules = new ModuleManager();
        try {
            ConfigManager.load();
        } catch (Throwable t) {
            // A bad config must never stop the game from starting.
            LOG.warn("Could not read config, using defaults: {}", t.toString());
        }
        ready = true;
        gg.monkeyclient.modules.AppleSkin.initialize();
        LOG.info("Monkey Client ready with {} modules", modules.all().size());

        // 26.2 has no Fabric HUD event; HudMixin drives HudManager instead.
        ClientTickEvents.END_CLIENT_TICK.register(MonkeyClient::onTick);
    }

    private static void onTick(Minecraft mc) {
        if (!ready()) return;
        modules.get(gg.monkeyclient.modules.ToggleSprint.class).recoverSavedFlight(mc);
        modules.tick();
        gg.monkeyclient.capture.ScreenshotFeedback.tick();
        pollMenuHold(mc);
    }

    private static void pollMenuHold(Minecraft mc) {
        long window = mc.getWindow().handle();
        // No Minecraft.screen field in 26.2. A grabbed cursor means we are in
        // the world rather than in a menu, which is what we actually care about.
        if (!inGame(window)) { shiftDownAt = -1; return; }
        boolean down = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;

        if (!down) { shiftDownAt = -1; return; }
        if (shiftDownAt < 0) { shiftDownAt = System.currentTimeMillis(); return; }
        if (System.currentTimeMillis() - shiftDownAt >= MENU_HOLD_MS) {
            shiftDownAt = -1;
            mc.setScreenAndShow(new MonkeyMenuScreen());
        }
    }

    /** True when the cursor is captured, i.e. no screen is in the way. */
    public static boolean inGame(long window) {
        return GLFW.glfwGetInputMode(window, GLFW.GLFW_CURSOR) == GLFW.GLFW_CURSOR_DISABLED;
    }

    /** 0 to 1 while the menu key is being held, for the on-screen hint. */
    public static float holdProgress() {
        if (shiftDownAt < 0) return 0f;
        return Math.min(1f, (System.currentTimeMillis() - shiftDownAt) / (float) MENU_HOLD_MS);
    }

    /** Mixins fire during start-up, before this class has finished loading. */
    public static boolean ready() { return ready && modules != null; }

    public static ModuleManager modules() { return modules; }
    public static Theme theme() { return theme; }
    public static void saveConfig() { ConfigManager.save(); }
}
