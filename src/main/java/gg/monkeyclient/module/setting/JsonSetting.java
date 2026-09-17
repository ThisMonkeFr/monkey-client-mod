package gg.monkeyclient.module.setting;
import com.google.gson.*;
/** Structured editor data; copied on save/load to avoid sharing default collections. */
public class JsonSetting extends Setting<JsonArray> {
 public JsonSetting(String id,String label){super(id,label,new JsonArray());}
 @Override public void reset(){value=new JsonArray();}
 @Override public JsonElement save(){return value.deepCopy();}
 @Override public void load(JsonElement json){if(json!=null&&json.isJsonArray())value=json.getAsJsonArray().deepCopy();}
}
