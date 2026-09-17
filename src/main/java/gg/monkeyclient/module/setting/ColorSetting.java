package gg.monkeyclient.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.awt.Color;

/** Packed ARGB. Rainbow mode cycles hue and ignores the stored RGB. */
public class ColorSetting extends Setting<Integer> {
    public boolean rainbow;
    public float rainbowSpeed = 1f;

    public ColorSetting(String id, String label, int argb) { super(id, label, argb); }

    /** The colour to actually draw with this frame. */
    public int resolve() {
        if (!rainbow) return value;
        float period = 5000f / Math.max(0.1f, rainbowSpeed);
        float hue = (System.currentTimeMillis() % (long) period) / period;
        return (value & 0xFF000000) | (Color.HSBtoRGB(hue, 0.75f, 1f) & 0xFFFFFF);
    }

    public int alpha() { return (value >>> 24) & 0xFF; }
    @Override public void reset() { super.reset(); rainbow = false; rainbowSpeed = 1f; }
    public void setAlpha(int a) { value = (value & 0x00FFFFFF) | ((a & 0xFF) << 24); }

    @Override public JsonElement save() {
        return new JsonPrimitive(String.format("%08X", value) + (rainbow ? ":rainbow" : ""));
    }
    @Override public void load(JsonElement json) {
        if (json == null || !json.isJsonPrimitive()) return;
        String s = json.getAsString();
        rainbow = s.endsWith(":rainbow");
        if (rainbow) s = s.substring(0, s.indexOf(':'));
        try { value = (int) Long.parseLong(s, 16); } catch (NumberFormatException ignored) { }
    }
}
