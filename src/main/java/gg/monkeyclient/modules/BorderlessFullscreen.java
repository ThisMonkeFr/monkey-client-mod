package gg.monkeyclient.modules;

import gg.monkeyclient.module.Category;
import gg.monkeyclient.module.Module;
import gg.monkeyclient.module.setting.BoolSetting;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/**
 * Borderless windowed, in the spirit of Cubes Without Borders: the window is
 * undecorated and sized to the monitor, so alt-tabbing is instant.
 */
public class BorderlessFullscreen extends Module {
    public final BoolSetting autoApply =
        add(new BoolSetting("auto", "Apply on launch", true));

    private boolean applied;
    private int[] saved;

    public BorderlessFullscreen() {
        super("borderless", "Borderless Fullscreen", "Undecorated window filling the monitor",
              Category.MINECRAFT, false);
    }

    @Override public void onTick() {
        if (!autoApply.get() || applied) return;
        applied = true;
        apply(true);
    }

    @Override public void onToggle(boolean on) {
        applied = on;
        apply(on);
    }

    private void apply(boolean on) {
        Minecraft mc = Minecraft.getInstance();
        long handle = mc.getWindow().handle();
        long monitor = GLFW.glfwGetPrimaryMonitor();
        GLFWVidModeSafe vid = GLFWVidModeSafe.of(monitor);
        if (vid == null) return;

        if (on) {
            if (saved != null) return;
            if (mc.getWindow().isFullscreen()) { mc.getWindow().toggleFullScreen(); mc.getWindow().updateFullscreenIfChanged(); }
            int[] x = new int[1], y = new int[1], w = new int[1], h = new int[1];
            GLFW.glfwGetWindowPos(handle, x, y);
            GLFW.glfwGetWindowSize(handle, w, h);
            saved = new int[]{ x[0], y[0], w[0], h[0] };

            GLFW.glfwSetWindowAttrib(handle, GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE);
            GLFW.glfwSetWindowMonitor(handle, 0L, 0, 0, vid.width(), vid.height(), GLFW.GLFW_DONT_CARE);
        } else if (saved != null) {
            GLFW.glfwSetWindowAttrib(handle, GLFW.GLFW_DECORATED, GLFW.GLFW_TRUE);
            GLFW.glfwSetWindowMonitor(handle, 0L, saved[0], saved[1], saved[2], saved[3], GLFW.GLFW_DONT_CARE);
            saved = null;
        }
    }

    /** Tiny wrapper so a missing monitor cannot throw during startup. */
    private record GLFWVidModeSafe(int width, int height) {
        static GLFWVidModeSafe of(long monitor) {
            if (monitor == 0L) return null;
            var mode = GLFW.glfwGetVideoMode(monitor);
            return mode == null ? null : new GLFWVidModeSafe(mode.width(), mode.height());
        }
    }
}
