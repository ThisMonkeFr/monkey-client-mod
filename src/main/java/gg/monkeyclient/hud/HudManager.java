package gg.monkeyclient.hud;

import gg.monkeyclient.MonkeyClient;
import net.minecraft.client.Minecraft;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Draws every enabled HUD element, and owns the drag state for the editor. */
public class HudManager {

    public static void renderAll(GuiGraphicsExtractor g, Minecraft mc) {
        for (HudElement e : elements()) {
            if (!e.isEnabled() || !e.shown.get() || !e.visible(mc)) continue;
            draw(g, mc, e, false);
        }
    }

    /** Shared by the live HUD and the editor, so what you drag is what you get. */
    public static void draw(GuiGraphicsExtractor g, Minecraft mc, HudElement e, boolean editing) {
        int px = e.screenX(mc), py = e.screenY(mc);
        int w = e.contentWidth(mc), h = e.contentHeight(mc);
        float s = e.scale.getFloat();

        g.pose().pushMatrix();
        g.pose().translate(px, py);
        g.pose().scale(s, s);

        int pad = e.padding.getInt();
        if (e.bg.get() || editing) {
            int colour = editing && !e.bg.get() ? 0x40000000 : e.backgroundColor();
            g.fill(-pad, -pad, w + pad, h + pad, colour);
        }
        if (e.border.get() || editing) {
            int colour = editing && !e.border.get() ? 0x60FFFFFF : e.borderColor();
            outline(g, -pad, -pad, w + pad, h + pad, colour);
        }
        g.pose().pushMatrix();g.pose().scale(w/(float)Math.max(1,e.width(mc)),h/(float)Math.max(1,e.height(mc)));
        e.render(g, mc);g.pose().popMatrix();
        g.pose().popMatrix();
    }

    private static void outline(GuiGraphicsExtractor g, int x1, int y1, int x2, int y2, int colour) {
        g.fill(x1, y1, x2, y1 + 1, colour);
        g.fill(x1, y2 - 1, x2, y2, colour);
        g.fill(x1, y1, x1 + 1, y2, colour);
        g.fill(x2 - 1, y1, x2, y2, colour);
    }

    public static List<HudElement> elements() {
        List<HudElement> out = new ArrayList<>();
        for (var m : MonkeyClient.modules().all())
            if (m instanceof HudElement e) out.add(e);
        out.add(MonkeyClient.modules().get(gg.monkeyclient.modules.FpsDisplay.class).graphElement);
        return out;
    }

    /** Topmost element under the cursor, for click-to-drag. */
    public static HudElement at(Minecraft mc, double mx, double my) {
        List<HudElement> list = elements();
        for (int i = list.size() - 1; i >= 0; i--) {
            HudElement e = list.get(i);
            if (!e.isEnabled()) continue;
            int pad = e.padding.getInt();
            double x1 = e.screenX(mc) - pad * e.scale.get();
            double y1 = e.screenY(mc) - pad * e.scale.get();
            double x2 = x1 + e.scaledWidth(mc) + pad * 2 * e.scale.get();
            double y2 = y1 + e.scaledHeight(mc) + pad * 2 * e.scale.get();
            if (mx >= x1 && mx <= x2 && my >= y1 && my <= y2) return e;
        }
        return null;
    }
}
