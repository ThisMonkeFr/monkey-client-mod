package gg.monkeyclient.module;

import gg.monkeyclient.module.setting.BoolSetting;
import gg.monkeyclient.module.setting.Setting;
import java.util.ArrayList;
import java.util.List;

/**
 * A single client feature. Modules do no work at all while disabled — the
 * manager never calls them — which is what keeps a big module list cheap.
 */
public abstract class Module {
    public final String id;
    public final String name;
    public final String description;
    public final Category category;

    public final BoolSetting enabled;
    private final List<Setting<?>> settings = new ArrayList<>();

    protected Module(String id, String name, String description, Category category, boolean onByDefault) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.enabled = add(new BoolSetting("enabled", "Enabled", onByDefault));
    }

    protected <T extends Setting<?>> T add(T setting) {
        settings.add(setting);
        return setting;
    }

    public List<Setting<?>> settings() { return settings; }
    public boolean isEnabled() { return enabled.get(); }

    /** Called when the toggle flips, so modules can grab or release resources. */
    public void onToggle(boolean on) { }

    /** Once per client tick, only while enabled. */
    public void onTick() { }
    public void onSettingChanged(Setting<?> setting) { }
    public net.minecraft.client.gui.screens.Screen editor(net.minecraft.client.gui.screens.Screen parent) { return null; }
    public String editorLabel() { return "OPEN EDITOR"; }

    public void resetAll() { settings.forEach(Setting::reset); onToggle(isEnabled()); }
}
