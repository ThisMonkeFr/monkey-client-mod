package gg.monkeyclient.modules;

import gg.monkeyclient.hud.HudElement;
import gg.monkeyclient.module.setting.BoolSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class PingDisplay extends HudElement {
    private final gg.monkeyclient.module.setting.EnumSetting location = add(new gg.monkeyclient.module.setting.EnumSetting("location", "Display in", "HUD and Tab", java.util.List.of("HUD and Tab", "Tab only", "HUD only")));
    public boolean showInTab() { return !location.get().equals("HUD only"); }
    @Override public boolean visible(Minecraft mc) { return !location.get().equals("Tab only"); }
    public int colour(int p) { return !colourByQuality.get() ? foregroundColor() : p < 0 ? 0xFF9E9E9E : p < 80 ? 0xFF6FD46F : p < 160 ? 0xFFE5C04B : 0xFFD9584B; }
    private final BoolSetting colourByQuality =
        add(new BoolSetting("colourByQuality", "Colour by latency", false));

    public PingDisplay() { super("ping", "Ping Display", "Your latency to the server", false); }

    private int ping(Minecraft mc) {
        if (mc.player == null || mc.getConnection() == null) return -1;
        var entry = mc.getConnection().getPlayerInfo(mc.player.getUUID());
        return entry == null ? -1 : entry.getLatency();
    }

    private String text(Minecraft mc) {
        int p = ping(mc);
        return p < 0 ? "-- ms" : p + " ms";
    }

    @Override public int width(Minecraft mc) { return textWidth(mc,text(mc)); }
    @Override public int height(Minecraft mc) { return textHeight(mc); }

    @Override public void render(GuiGraphicsExtractor g, Minecraft mc) {
        int colour = foregroundColor();
        if (colourByQuality.get()) {
            int p = ping(mc);
            colour = p < 0 ? 0xFF9E9E9E
                   : p < 80 ? 0xFF6FD46F
                   : p < 160 ? 0xFFE5C04B
                   : 0xFFD9584B;
        }
        text(g, mc, text(mc), 0, 0, colour, shadow.get());
    }
}
