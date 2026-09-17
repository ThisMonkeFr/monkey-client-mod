package gg.monkeyclient.module.setting;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
public class KeySetting extends Setting<Integer> {
    public KeySetting(String id, String label, int key) { super(id, label, key); }
    public boolean down(Minecraft mc) { return value >= 32 && value <= GLFW.GLFW_KEY_LAST && GLFW.glfwGetKey(mc.getWindow().handle(), value) == GLFW.GLFW_PRESS; }
    public String display() {
        if (value < 0) return "Unbound";
        String name = GLFW.glfwGetKeyName(value, 0);
        return name == null ? "Key " + value : name.toUpperCase(java.util.Locale.ROOT);
    }
    @Override public JsonElement save() { return new JsonPrimitive(value); }
    @Override public void load(JsonElement j) { int k = j.getAsInt(); if (k == -1 || (k >= 32 && k <= GLFW.GLFW_KEY_LAST)) set(k); }
}