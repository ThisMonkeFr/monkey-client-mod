package gg.monkeyclient.config;

import com.google.gson.*;
import gg.monkeyclient.MonkeyClient;
import gg.monkeyclient.module.Module;
import gg.monkeyclient.module.setting.Setting;
import net.fabricmc.loader.api.FabricLoader;
import java.io.*;
import java.nio.file.*;

/**
 * Persists everything to one file. Settings serialise themselves, so adding a
 * module never means touching this class, and unknown keys from a newer
 * version are ignored rather than throwing.
 */
public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("monkeyclient.json");
    }

    public static JsonObject snapshot() {
        JsonObject root = new JsonObject();
        root.addProperty("version", 4);

        JsonObject modules = new JsonObject();
        for (Module m : MonkeyClient.modules().all()) {
            JsonObject mo = new JsonObject();
            for (Setting<?> s : m.settings()) mo.add(s.id, s.save());
            modules.add(m.id, mo);
        }
        root.add("modules", modules);

        Theme t = MonkeyClient.theme();
        JsonObject theme = new JsonObject();
        theme.addProperty("vanilla",t.vanilla);
        theme.addProperty("accent", t.accent);
        theme.addProperty("background", t.background);
        theme.addProperty("panel", t.panel);
        theme.addProperty("text", t.text);
        theme.addProperty("textDim", t.textDim);
        theme.addProperty("opacity", t.opacity);
        theme.addProperty("animations", t.animations);
        theme.addProperty("animationMs", t.animationMs);
        theme.addProperty("buttonColor",t.buttonColor);
        theme.addProperty("sliderColor",t.sliderColor);
        theme.addProperty("toggleColor",t.toggleColor);
        theme.addProperty("highlightColor",t.highlightColor);
        root.add("theme", theme);
        return root;
    }
    public static void save() {
        JsonObject root=snapshot();
        try {
            Files.createDirectories(file().getParent());
            Path tmp = file().resolveSibling("monkeyclient.json.tmp");
            try (Writer w = Files.newBufferedWriter(tmp)) { GSON.toJson(root, w); }
            Files.move(tmp, file(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            MonkeyClient.LOG.warn("Could not save config: {}", e.getMessage());
        }
    }

    public static void load() {
        if (!Files.isReadable(file())) return;
        try (Reader r = Files.newBufferedReader(file())) {
            JsonObject root = JsonParser.parseReader(r).getAsJsonObject();
            apply(root);
        } catch (Exception e) {
            MonkeyClient.LOG.warn("Config unreadable, starting fresh: {}", e.getMessage());
        }
    }
    public static void apply(JsonObject root) {
            if (root.has("modules")) {
                JsonObject modules = root.getAsJsonObject("modules");
                for (Module m : MonkeyClient.modules().all()) {
                    if (!modules.has(m.id)) continue;
                    JsonObject mo = modules.getAsJsonObject(m.id);
                    // Migrate old brightness units and the old generic HUD backplates.
                    if (m instanceof gg.monkeyclient.modules.Brightness b && mo.has("level") && !mo.has("toggledGamma"))
                        b.toggledGamma.set(mo.get("level").getAsDouble() * 100);
                    boolean oldHud = (m.id.equals("armor") && !mo.has("style")) || (m.id.equals("potions") && !mo.has("timer"));
                    for (Setting<?> s : m.settings())
                        if (mo.has(s.id) && !(oldHud && (s.id.equals("background") || s.id.equals("padding")))) {
                            try { s.load(mo.get(s.id));if(MonkeyClient.ready())m.onSettingChanged(s); }
                            catch (RuntimeException badValue) { MonkeyClient.LOG.warn("Ignoring invalid setting {}.{}",m.id,s.id); }
                        }
                    if(MonkeyClient.ready())m.onToggle(m.isEnabled());
                }
            }
            if (root.has("theme")) {
                JsonObject t = root.getAsJsonObject("theme");
                Theme th = MonkeyClient.theme();
                th.vanilla=root.has("version")&&root.get("version").getAsInt()>=4&&t.has("vanilla")&&t.get("vanilla").getAsBoolean();
                if (t.has("accent")) th.accent = t.get("accent").getAsInt();
                if (t.has("background")) th.background = t.get("background").getAsInt();
                if (t.has("panel")) th.panel = t.get("panel").getAsInt();
                if (t.has("text")) th.text = t.get("text").getAsInt();
                if (t.has("textDim")) th.textDim = t.get("textDim").getAsInt();
                if (t.has("opacity")) th.opacity = Math.max(30, Math.min(100,t.get("opacity").getAsInt()));
                if (t.has("animations")) th.animations = t.get("animations").getAsBoolean();
                if (t.has("animationMs")) th.animationMs = Math.max(50,Math.min(600,t.get("animationMs").getAsInt()));
                if(t.has("buttonColor"))th.buttonColor=t.get("buttonColor").getAsInt();
                if(t.has("sliderColor"))th.sliderColor=t.get("sliderColor").getAsInt();
                if(t.has("toggleColor"))th.toggleColor=t.get("toggleColor").getAsInt();
                if(t.has("highlightColor"))th.highlightColor=t.get("highlightColor").getAsInt();
            }
    }
}
