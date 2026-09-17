package gg.monkeyclient.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.Reader;
import java.nio.file.*;

/**
 * Menu colours. Defaults match the launcher's Jungle theme, and
 * {@link #matchLauncher()} reads the launcher's own saved theme so the two
 * halves of the client look identical without being configured twice.
 */
public class Theme {
    public boolean vanilla = true;
    public int surface(){return vanilla?0xFFC6C6C6:panel;}
    public int backdrop(){return vanilla?0xFFC6C6C6:background;}
    public int ink(){return vanilla?0xFF303030:text;}
    public int mutedInk(){return vanilla?0xFF505050:textDim;}
    public int buttonColor, sliderColor, toggleColor, highlightColor;
    public int button(){return buttonColor==0?panel:buttonColor;}
    public int slider(){return sliderColor==0?accent:sliderColor;}
    public int toggle(){return toggleColor==0?accent:toggleColor;}
    public int highlight(){return highlightColor==0?accent:highlightColor;}
    public int opacity = 88;
    public boolean animations = true;
    public int animationMs = 180;
    public void preset(String name) {
        buttonColor=sliderColor=toggleColor=highlightColor=0;
        switch(name) {
            case "Jungle" -> { accent=0xFF8BCB62; background=0xFF0C130F; panel=0xFF1D2B22; }
            case "Classic" -> { accent=0xFFE25454; background=0xFF151515; panel=0xFF242424; }
            case "Amber" -> { accent=0xFFFFBC78; background=0xFF241B17; panel=0xFF3A2D22; }
            case "Ice" -> { accent=0xFF80CFFF; background=0xFF101821; panel=0xFF23303D; }
            case "Volcano" -> {accent=0xFFFF7548;background=0xFF160E0D;panel=0xFF30201E;}
            case "Abyss" -> {accent=0xFF56D2DD;background=0xFF08131E;panel=0xFF142B3D;}
            case "Amethyst" -> {accent=0xFFC39AF9;background=0xFF151022;panel=0xFF2B2141;}
            case "Ember" -> {accent=0xFFF5BD63;background=0xFF17110B;panel=0xFF322617;}
            case "Frost" -> {accent=0xFFAAD9FF;background=0xFF111C2A;panel=0xFF263A50;}
            default -> { return; }
        }
        text=0xFFF4F4F4; textDim=0xFFBCBCBC;
    }
    public int accent = 0xFF8BCB62;
    public int background = 0xFF0C130F;
    public int panel = 0xFF1D2B22;
    public int text = 0xFFF4F9F0;
    public int textDim = 0xFFBCCDB1;

    public int line()    { return vanilla?0xFF555555:blend(panel, 0xFFFFFFFF, 0.15f); }
    public int panelHi() { return blend(panel, 0xFFFFFFFF, 0.08f); }
    public int accentDim(){ return (accent & 0x00FFFFFF) | 0x40000000; }

    /**
     * Pull the launcher's colours out of its config. The launcher stores its
     * state next to the game data, so on every platform this is one hop from
     * the Minecraft directory.
     */
    public boolean matchLauncher() {
        for (Path p : launcherConfigCandidates()) {
            if (!Files.isReadable(p)) continue;
            try (Reader r = Files.newBufferedReader(p)) {
                JsonObject root = JsonParser.parseReader(r).getAsJsonObject();
                if (!root.has("theme") || root.get("theme").isJsonNull()) continue;
                JsonObject t = root.getAsJsonObject("theme");
                accent = hex(t, "accent", accent);
                background = hex(t, "bg", background);
                panel = hex(t, "panel", panel);
                text = hex(t,"text",text);
                textDim = hex(t,"textDim",textDim);
                buttonColor=sliderColor=toggleColor=highlightColor=0;
                return true;
            } catch (Exception ignored) { }
        }
        return false;
    }

    private static Path[] launcherConfigCandidates() {
        String home = System.getProperty("user.home", ".");
        String appdata = System.getenv("APPDATA");
        return new Path[] {
            net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir().resolve("monkeyclient").resolve("launcher.json"),
            appdata == null ? Paths.get(home, ".monkey-client", "launcher.json")
                            : Paths.get(appdata, "monkey-client", "launcher.json"),
            Paths.get(home, "Library", "Application Support", "monkey-client", "launcher.json"),
            Paths.get(home, ".config", "monkey-client", "launcher.json")
        };
    }

    private static int hex(JsonObject o, String key, int fallback) {
        if (!o.has(key)) return fallback;
        String s = o.get(key).getAsString().replace("#", "");
        try { return 0xFF000000 | (int) Long.parseLong(s, 16); }
        catch (NumberFormatException e) { return fallback; }
    }

    public static int blend(int a, int b, float t) {
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        return 0xFF000000
            | ((int) (ar + (br - ar) * t) << 16)
            | ((int) (ag + (bg - ag) * t) << 8)
            |  (int) (ab + (bb - ab) * t);
    }
}
