package gg.monkeyclient.module.setting;
import gg.monkeyclient.module.Module;
import com.google.gson.*;
/** Nested HUD editor settings stored with their owning module. */
public class ModuleSetting extends Setting<Module> {
 public ModuleSetting(String id,String label,Module module){super(id,label,module);}
 @Override public JsonElement save(){var o=new JsonObject();for(var s:value.settings())o.add(s.id,s.save());return o;}
 @Override public void load(JsonElement j){if(j==null||!j.isJsonObject())return;for(var s:value.settings())if(j.getAsJsonObject().has(s.id))try{s.load(j.getAsJsonObject().get(s.id));}catch(RuntimeException ignored){}}
 @Override public void reset(){value.resetAll();}
}
