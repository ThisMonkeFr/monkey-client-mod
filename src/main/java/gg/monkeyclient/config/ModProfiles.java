package gg.monkeyclient.config;
import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.*;
import java.io.IOException;
import java.util.*;

/** Local, portable snapshots. Import never interprets paths or executable code. */
public final class ModProfiles {
 public record Profile(String id,String name,JsonObject settings){}
 private final List<Profile> profiles=new ArrayList<>();
 private final Path file=FabricLoader.getInstance().getConfigDir().resolve("monkeyclient/mod-profiles.json");
 private String activeId="";
 private final Path activeFile=file.resolveSibling("active-profile.txt");
 private final Gson gson=new GsonBuilder().setPrettyPrinting().create();
 public ModProfiles() throws IOException {
  if(Files.exists(file)){
   JsonArray root=JsonParser.parseString(Files.readString(file)).getAsJsonArray();
   for(var element:root){var p=element.getAsJsonObject();profiles.add(new Profile(p.get("id").getAsString(),p.get("name").getAsString(),p.getAsJsonObject("settings")));}
  }
  if(profiles.isEmpty())create("Default",ConfigManager.snapshot());
  if(Files.exists(activeFile))activeId=Files.readString(activeFile).strip();else if(!profiles.isEmpty()){activeId=profiles.get(0).id;saveActive();}
 }
 public String activeName(){return profiles.stream().filter(p->p.id.equals(activeId)).map(Profile::name).findFirst().orElse("Custom settings");}
 public boolean active(int index){return profiles.get(index).id.equals(activeId);}
 private void saveActive()throws IOException{Files.createDirectories(activeFile.getParent());Files.writeString(activeFile,activeId);}
 public void preset(String name)throws IOException{if(!java.util.Set.of("PvP","Hoplite").contains(name))throw new IOException("Unknown preset");try(var input=ModProfiles.class.getResourceAsStream("/assets/monkeyclient/presets/"+name.toLowerCase(java.util.Locale.ROOT)+".json")){if(input==null)throw new IOException("Preset missing");importProfile(new String(input.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8));apply(profiles.size()-1);}}
 public List<Profile> all(){return List.copyOf(profiles);}
 public void create(String name,JsonObject settings)throws IOException {if(profiles.size()>=100)throw new IOException("Maximum of 100 profiles");profiles.add(new Profile(UUID.randomUUID().toString(),clean(name),settings.deepCopy()));save();}
 public void rename(int index,String name)throws IOException{var p=profiles.get(index);profiles.set(index,new Profile(p.id,clean(name),p.settings));save();}
 public void update(int index)throws IOException{var p=profiles.get(index);profiles.set(index,new Profile(p.id,p.name,ConfigManager.snapshot()));save();}
 public void delete(int index)throws IOException{profiles.remove(index);save();}
 public void apply(int index){activeId=profiles.get(index).id;try{saveActive();}catch(IOException e){gg.monkeyclient.MonkeyClient.LOG.warn("Could not save active profile",e);}ConfigManager.apply(profiles.get(index).settings.deepCopy());ConfigManager.save();}
 public String export(int index){var p=profiles.get(index);JsonObject out=new JsonObject();out.addProperty("format","monkeyclient-profile");out.addProperty("name",p.name);out.add("settings",p.settings);return gson.toJson(out);}
 public void importProfile(String text)throws IOException{
  if(text==null||text.length()>2_000_000)throw new IOException("Profile is empty or too large");
  try{var root=JsonParser.parseString(text).getAsJsonObject();if(!root.has("format")||!root.get("format").getAsString().equals("monkeyclient-profile"))throw new IOException("This is not a Monkey Client profile");
   var settings=root.getAsJsonObject("settings");if(settings==null||!settings.has("modules")||!settings.get("modules").isJsonObject())throw new IOException("Profile has no module settings");create(root.get("name").getAsString(),settings);
  }catch(JsonParseException|IllegalStateException|NullPointerException ex){throw new IOException("Invalid profile data",ex);}
 }
 private static String clean(String name){String value=name==null?"":name.strip();return value.isEmpty()?"New profile":value.substring(0,Math.min(40,value.length()));}
 private void save()throws IOException{
  Files.createDirectories(file.getParent());JsonArray root=new JsonArray();for(var p:profiles){var row=new JsonObject();row.addProperty("id",p.id);row.addProperty("name",p.name);row.add("settings",p.settings);root.add(row);}
  Path temp=file.resolveSibling(file.getFileName()+".tmp");Files.writeString(temp,gson.toJson(root));try{Files.move(temp,file,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}catch(AtomicMoveNotSupportedException ex){Files.move(temp,file,StandardCopyOption.REPLACE_EXISTING);}
 }
}
