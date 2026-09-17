package gg.monkeyclient.ui;

import gg.monkeyclient.MonkeyClient;
import gg.monkeyclient.config.Theme;
import gg.monkeyclient.hud.HudElement;
import gg.monkeyclient.hud.HudManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * Drag HUD elements straight where you want them. Positions are stored as a
 * fraction of the screen, so a layout built at one resolution still sits in
 * the right place at another.
 */
public class HudEditorScreen extends Screen {
    private final Screen parent;
    private HudElement dragging;
    private double grabX, grabY;

    private boolean grid(){long h=minecraft.getWindow().handle();return org.lwjgl.glfw.GLFW.glfwGetKey(h,341)==1||org.lwjgl.glfw.GLFW.glfwGetKey(h,345)==1;}
    private static final int SNAP = 4;      // pixels

    public HudEditorScreen(Screen parent) {
        super(Component.literal("HUD Editor"));
        this.parent = parent;
    }

    @Override public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        super.extractRenderState(g, mx, my, delta);
        Theme t = MonkeyClient.theme();

        if(grid()){for(int x=0;x<width;x+=8)g.fill(x,0,x+1,height,0x223F9DFF);for(int y=0;y<height;y+=8)g.fill(0,y,width,y+1,0x223F9DFF);}
        for (HudElement e : HudManager.elements())
            if (e.isEnabled()) HudManager.draw(g, minecraft, e, true);

        String hint = "Drag · Scroll: scale · Ctrl: snap · Esc: done";
        int w = font.width(hint);
        g.fill(width / 2 - w / 2 - 6, 6, width / 2 + w / 2 + 6, 22, t.panel);
        g.text(font, hint, width / 2 - w / 2, 11, t.text);
    }

    @Override public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x(), my = event.y();
        HudElement hit = HudManager.at(minecraft, mx, my);
        if (hit != null) {
            dragging = hit;
            grabX = mx - hit.screenX(minecraft);
            grabY = my - hit.screenY(minecraft);
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        double mx = event.x(), my = event.y();
        if (dragging == null) return super.mouseDragged(event, dx, dy);
        int sw = minecraft.getWindow().getGuiScaledWidth();
        int sh = minecraft.getWindow().getGuiScaledHeight();

        double px = mx - grabX, py = my - grabY;
        // Snap to the screen edges and centre lines so layouts stay tidy.
        double rightEdge = sw - dragging.scaledWidth(minecraft);
        double bottomEdge = sh - dragging.scaledHeight(minecraft);
        if (Math.abs(px) < SNAP) px = 0;
        if (Math.abs(px - rightEdge) < SNAP) px = rightEdge;
        if (Math.abs(px - (sw - dragging.scaledWidth(minecraft)) / 2.0) < SNAP)
            px = (sw - dragging.scaledWidth(minecraft)) / 2.0;
        if (Math.abs(py) < SNAP) py = 0;
        if (Math.abs(py - bottomEdge) < SNAP) py = bottomEdge;

        if(grid()){px=Math.round(px/8)*8;py=Math.round(py/8)*8;}
        int anchor = dragging.align.get().equals("Centre") ? dragging.scaledWidth(minecraft)/2 : dragging.align.get().equals("Right") ? dragging.scaledWidth(minecraft) : 0;
        dragging.x.set(Math.max(0, Math.min(1, (px+anchor) / sw)));
        dragging.y.set(Math.max(0, Math.min(1, py / sh)));
        return true;
    }

    @Override public boolean mouseReleased(MouseButtonEvent event) {
        if (dragging != null) { dragging = null; MonkeyClient.saveConfig(); return true; }
        return super.mouseReleased(event);
    }

    @Override public boolean mouseScrolled(double mx, double my, double dx, double dy) {
        HudElement hit = HudManager.at(minecraft, mx, my);
        if (hit == null) return super.mouseScrolled(mx, my, dx, dy);
        hit.scale.set(hit.scale.get() + dy * 0.05);
        return true;
    }

    @Override public void onClose() {
        MonkeyClient.saveConfig();
        minecraft.setScreenAndShow(parent);
    }

    @Override public boolean isPauseScreen() { return false; }
}
