package gg.monkeyclient.module.setting;

import com.google.gson.JsonElement;

/**
 * One configurable value. Settings serialise themselves, so the config file
 * never needs to know which modules exist.
 */
public abstract class Setting<T> {
    public final String id;
    public final String label;
    protected T value;
    private T defaultValue;

    protected Setting(String id, String label, T value) {
        this.id = id; this.label = label; this.value = value; this.defaultValue = value;
    }

    public T get() { return value; }
    public void set(T v) { this.value = v; }
    public void reset() { this.value = defaultValue; }
    public void setDefault(T v) { defaultValue = v; value = v; }
    public boolean isDefault() { return defaultValue.equals(value); }

    public abstract JsonElement save();
    public abstract void load(JsonElement json);
}
