package gg.monkeyclient.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class BoolSetting extends Setting<Boolean> {
    public BoolSetting(String id, String label, boolean value) { super(id, label, value); }
    public void toggle() { set(!get()); }
    @Override public JsonElement save() { return new JsonPrimitive(value); }
    @Override public void load(JsonElement json) {
        if (json != null && json.isJsonPrimitive()) value = json.getAsBoolean();
    }
}
