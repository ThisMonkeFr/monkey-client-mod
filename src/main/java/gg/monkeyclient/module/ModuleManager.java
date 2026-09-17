package gg.monkeyclient.module;

import gg.monkeyclient.modules.*;
import java.util.*;

/** Owns every module and ticks only the enabled ones. */
public class ModuleManager {
    private final List<Module> modules = new ArrayList<>();
    private final Map<String, Module> byId = new HashMap<>();

    public ModuleManager() {
        register(new FpsDisplay());
        register(new CpsDisplay());
        register(new Keystrokes());
        register(new PingDisplay());
        register(new ArmorHud());
        register(new PotionDisplay());
        register(new CoordsDisplay());
        register(new Brightness());
        register(new Zoom());
        register(new ToggleSprint());
        register(new GuiScale());
        register(new BorderlessFullscreen());
        register(new FogEditor());
        register(new BlockOutlines());
        register(new CustomCrosshair());
        register(new AppleSkin());
        register(new ContainerPreview());
        register(new Waypoints());
        register(new Nametag());
        register(new TiersDisplay());
        register(new ItemEditor());
        register(new MotionBlur());
    }

    private void register(Module m) {
        modules.add(m);
        byId.put(m.id, m);
    }

    public List<Module> all() { return modules; }
    public Module byId(String id) { return byId.get(id); }

    public List<Module> inCategory(Category c) {
        return modules.stream().filter(m -> m.category == c).toList();
    }

    /** Disabled modules cost one boolean check per tick and nothing else. */
    public void tick() {
        for (int i = 0; i < modules.size(); i++) {
            Module m = modules.get(i);
            if (m.isEnabled()) m.onTick();
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Module> T get(Class<T> type) {
        for (Module m : modules) if (type.isInstance(m)) return (T) m;
        return null;
    }
}
