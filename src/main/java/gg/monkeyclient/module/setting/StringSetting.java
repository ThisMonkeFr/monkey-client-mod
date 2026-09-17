package gg.monkeyclient.module.setting;
import com.google.gson.*;
public class StringSetting extends Setting<String> {
 public final int maxLength;
 public StringSetting(String id,String label,String value,int maxLength){super(id,label,value);this.maxLength=maxLength;}
 @Override public void set(String s){value=s==null?"":s.substring(0,Math.min(s.length(),maxLength));}
 @Override public JsonElement save(){return new JsonPrimitive(value);}
 @Override public void load(JsonElement j){if(j!=null&&j.isJsonPrimitive())set(j.getAsString());}
}
