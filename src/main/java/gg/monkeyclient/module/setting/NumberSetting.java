package gg.monkeyclient.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class NumberSetting extends Setting<Double> {
    public final double min, max, step;

    public NumberSetting(String id, String label, double value, double min, double max, double step) {
        super(id, label, value);
        this.min = min; this.max = max; this.step = step;
    }

    public float getFloat() { return value.floatValue(); }
    public int getInt() { return (int) Math.round(value); }

    @Override public void set(Double v) {
        if (v == null || !Double.isFinite(v)) return;
        // Snap to the step, so a slider never yields 3.4000000000000004.
        double clamped = Math.max(min, Math.min(max, v));
        double snapped = Math.round(clamped / step) * step;
        super.set(Math.max(min, Math.min(max, Math.round(snapped * 1000d) / 1000d)));
    }

    @Override public JsonElement save() { return new JsonPrimitive(value); }
    @Override public void load(JsonElement json) {
        if (json != null && json.isJsonPrimitive()) set(json.getAsDouble());
    }
}
