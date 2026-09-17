package gg.monkeyclient.modules;
import gg.monkeyclient.hud.HudElement;
import gg.monkeyclient.module.setting.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import gg.monkeyclient.render.Icons;
import com.google.gson.*;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;
/** Bounded asynchronous lookups. Render hooks only read the cache. */
public class TiersDisplay extends HudElement {
 public final EnumSetting source=add(new EnumSetting("source","Tier source","MCTiers",List.of("MCTiers","SubTiers","MCPVP")));
 private static final List<String> MC_MODES=List.of("best","vanilla","uhc","pot","nethop","smp","sword","axe","mace");
 private static final List<String> SUB_MODES=List.of("best","minecart","dia_crystal","debuff","elytra","speed","creeper","manhunt","dia_smp","bow","bed","og_vanilla","trident");
 private static final List<String> PVP_MODES=List.of("best","overall","End Game","Crystal","Sword","Spear","Mace","Early Game","Shield","Pot","Netherite","Late Game","Diamond SMP","Creeper","SMP","Cart","Bow");
 private static List<String> allModes(){var list=new ArrayList<String>();list.addAll(MC_MODES);list.addAll(SUB_MODES);list.addAll(PVP_MODES);return list;}
 public final EnumSetting mode=add(new EnumSetting("mode","Game mode","best",allModes()));
 public final BoolSetting allSources=add(new BoolSetting("allSources","Best game mode across all tier lists",false));
 private String modesFor="";
 public final BoolSetting tab=add(new BoolSetting("tab","Show in tab",true)),names=add(new BoolSetting("names","Show beside player names",true)),ownHud=add(new BoolSetting("ownHud","Show own tier HUD",true)),retired=add(new BoolSetting("retired","Include retired tiers",true));
 private record Ranking(String provider,String label){}
 private record Entry(Ranking ranking,long expires){}
 private final Map<String,Entry> cache=new ConcurrentHashMap<>();
 private volatile boolean pending;private long nextLookup;
 private final HttpClient http=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).followRedirects(HttpClient.Redirect.NORMAL).build();
 public TiersDisplay(){super("tiers","Tiers Display","Cached MCTiers, SubTiers and MCPVP rankings",false);y.setDefault(.12);}
 private String cacheKey(UUID uuid){return (allSources.get()?"ALL:best":source.get()+":"+mode.get())+":"+retired.get()+":"+uuid;}
 public String label(UUID uuid){var e=cache.get(cacheKey(uuid));return e==null?"":e.ranking.label;}
 public static String modeIcon(String provider,String mode){
  if(!provider.equals("MCPVP"))return mode.toLowerCase(Locale.ROOT);
  return switch(mode.toLowerCase(Locale.ROOT)){
   case "end game","crystal"->"pvp_crystal";case "early game"->"pvp_early";case "late game"->"pvp_late";
   case "diamond smp"->"pvp_dia_smp";case "netherite"->"pvp_nether";default->"pvp_"+mode.toLowerCase(Locale.ROOT);
  };
 }
 private Component badge(UUID uuid){
  String value=label(uuid);if(value.isEmpty())return Component.empty();
  String provider=cache.get(cacheKey(uuid)).ranking.provider;
  int space=value.indexOf(' ');String tier=space<0?value:value.substring(0,space),game=space<0?mode.get():value.substring(space+1);
  var out=Component.empty().append(Icons.text(provider.toLowerCase(Locale.ROOT))).append(" ");
  if(!game.equalsIgnoreCase("overall")&&!game.equalsIgnoreCase("best"))out.append(Icons.text(modeIcon(provider,game))).append(" ");
  return out.append(Component.literal(tier).withColor(foregroundColor()&0xFFFFFF));
 }
 public Component append(Component name,UUID uuid){return label(uuid).isEmpty()?name:badge(uuid).copy().append(Component.literal(" | ").withColor(0x888888)).append(name);}
 private void modes(){if(modesFor.equals(source.get()))return;modesFor=source.get();var choices=modesFor.equals("MCPVP")?PVP_MODES:modesFor.equals("SubTiers")?SUB_MODES:MC_MODES;mode.options.clear();mode.options.addAll(choices);if(!choices.contains(mode.get()))mode.set(choices.getFirst());}
 @Override public void onSettingChanged(Setting<?> s){if(s==source)modes();}
 @Override public void onTick(){
  modes();var mc=Minecraft.getInstance();if(mc.getConnection()==null||pending||System.currentTimeMillis()<nextLookup)return;
  if(cache.size()>512){cache.entrySet().removeIf(e->e.getValue().expires<System.currentTimeMillis());cache.entrySet().stream().sorted(Comparator.comparingLong(e->e.getValue().expires)).limit(Math.max(0,cache.size()-512)).map(Map.Entry::getKey).toList().forEach(cache::remove);}
  for(var player:mc.getConnection().getOnlinePlayers()){
   UUID uuid=player.getProfile().id();String key=cacheKey(uuid);var old=cache.get(key);if(old!=null&&old.expires>System.currentTimeMillis())continue;
   String provider=source.get(),gameMode=mode.get(),name=player.getProfile().name();boolean includeRetired=retired.get();
   pending=true;nextLookup=System.currentTimeMillis()+1200;
   List<CompletableFuture<Ranking>> requests=new ArrayList<>();
   for(String selected:allSources.get()?List.of("MCTiers","SubTiers","MCPVP"):List.of(provider))requests.add(lookup(selected,allSources.get()?"best":gameMode,uuid,name,includeRetired));
   CompletableFuture.allOf(requests.toArray(CompletableFuture[]::new)).whenComplete((ignored,error)->{
    try{Ranking best=new Ranking(provider,"");for(var request:requests){Ranking result=request.getNow(new Ranking(provider,""));if(tierScore(result.label)<tierScore(best.label))best=result;}
     cache.put(key,new Entry(best,System.currentTimeMillis()+(best.label.isEmpty()?300_000:1_800_000)));
    }finally{pending=false;}
   });
   break;
  }
 }
 private CompletableFuture<Ranking> lookup(String provider,String mode,UUID uuid,String name,boolean retired){
  String url=provider.equals("MCPVP")?"https://www.mcpvp.com/profile/"+name:"https://"+(provider.equals("SubTiers")?"subtiers.net":"mctiers.com")+"/api/v2/profile/"+uuid+"/rankings";
  return http.sendAsync(HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(8)).header("User-Agent","MonkeyClient/0.5").GET().build(),HttpResponse.BodyHandlers.ofString()).handle((response,error)->{
   String label="";try{if(error==null&&response.statusCode()==200&&response.body().length()<2_000_000)label=provider.equals("MCPVP")?parseMcpvp(response.body(),mode):parseRankings(response.body(),mode,retired);}catch(RuntimeException ignored){}
   return new Ranking(provider,label);
  });
 }
 public static int tierScore(String label){if(label==null||!label.matches("[HML]T[1-5](?: .*)?"))return Integer.MAX_VALUE;return (label.charAt(2)-'0')*3+(label.charAt(0)=='H'?0:label.charAt(0)=='M'?1:2);}
 public static String parseRankings(String body,String mode,boolean retired){
  JsonObject j=JsonParser.parseString(body).getAsJsonObject();if(j.has("rankings"))j=j.getAsJsonObject("rankings");
  int best=100;String label="";
  for(var entry:j.entrySet())try{
   if(!mode.equalsIgnoreCase("best")&&!entry.getKey().equalsIgnoreCase(mode))continue;
   JsonObject r=entry.getValue().getAsJsonObject();if(!retired&&r.has("retired")&&r.get("retired").getAsBoolean())continue;
   int tier=r.get("tier").getAsInt(),pos=r.get("pos").getAsInt();if(tier<1||tier>5||pos<0||pos>1)continue;
   int rank=tier*2+pos;if(rank<best){best=rank;label=(pos==0?"HT":"LT")+tier+" "+entry.getKey();}
  }catch(RuntimeException ignored){}
  return label;
 }
 public static String parseMcpvp(String body,String mode){
  if(mode.equalsIgnoreCase("best")){
   String best="";
   for(String kit:PVP_MODES){if(kit.equals("best")||kit.equals("overall"))continue;String value=parseMcpvp(body,kit);if(tierScore(value)<tierScore(best))best=value;}
   return best;
  }
  Pattern p=mode.equalsIgnoreCase("overall")?
   Pattern.compile("title=\"Overall tier: ([HML]T[1-5])\""):
   Pattern.compile("title=\""+Pattern.quote(mode.replace('-',' '))+" ([HML]T[1-5])(?: —[^\"]*)?\"",Pattern.CASE_INSENSITIVE);
  var m=p.matcher(body);return m.find()?m.group(1).toUpperCase(Locale.ROOT)+" "+mode:"";
 }
 @Override public boolean visible(Minecraft mc){return ownHud.get()&&mc.player!=null&&!label(mc.player.getUUID()).isEmpty();}
 @Override public int width(Minecraft mc){return mc.player==null?36:(int)Math.ceil(mc.font.width(badge(mc.player.getUUID()))*textSize.get());}
 @Override public int height(Minecraft mc){return textHeight(mc);}
 @Override public void render(GuiGraphicsExtractor g,Minecraft mc){if(mc.player!=null){g.pose().pushMatrix();g.pose().scale(textSize.getFloat(),textSize.getFloat());g.text(mc.font,badge(mc.player.getUUID()),0,0,0xFFFFFFFF,shadow.get());g.pose().popMatrix();}}
}
