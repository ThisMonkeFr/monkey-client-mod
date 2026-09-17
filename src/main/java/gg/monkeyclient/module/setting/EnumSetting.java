package gg.monkeyclient.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.List;

public class EnumSetting extends Setting<String> {
    public final List<String> options;

    public EnumSetting(String id, String label, String value, List<String> options) {
        super(id, label, value);
        this.options = options;
    }

    public void cycle() { set(options.get((options.indexOf(value) + 1) % options.size())); }

    @Override public JsonElement save() { return new JsonPrimitive(value); }
    @Override public void load(JsonElement json) {
        if (json != null && json.isJsonPrimitive() && options.contains(json.getAsString()))
            value = json.getAsString();
    }
}
