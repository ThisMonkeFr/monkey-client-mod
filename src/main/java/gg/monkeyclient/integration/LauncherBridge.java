package gg.monkeyclient.integration;
import com.google.gson.*;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.concurrent.*;
public final class LauncherBridge {
 public static final ExecutorService WORK=Executors.newFixedThreadPool(2,r->{var t=new Thread(r,"Monkey shared data");t.setDaemon(true);return t;});
 private static final HttpClient HTTP=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
 private static final String URL=System.getenv("MONKEY_UI_URL"),TOKEN=System.getenv("MONKEY_UI_TOKEN");
 public static boolean available(){return URL!=null&&URL.matches("http://127\\.0\\.0\\.1:[0-9]+")&&TOKEN!=null&&TOKEN.matches("[a-f0-9]{64}");}
 public static HttpResponse<byte[]> request(String path,JsonObject data)throws Exception{
  if(!available())throw new IllegalStateException("Launch this instance with Monkey Client and keep the launcher open.");
  var b=HttpRequest.newBuilder(URI.create(URL+path)).timeout(Duration.ofSeconds(path.equals("/image/pick")||path.equals("/library/import")?180:20)).header("Authorization","Bearer "+TOKEN);
  if(data!=null)b.header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(data.toString()));else b.GET();
  var r=HTTP.send(b.build(),HttpResponse.BodyHandlers.ofByteArray());if(r.statusCode()>=400){String text=new String(r.body(),java.nio.charset.StandardCharsets.UTF_8);try{text=JsonParser.parseString(text).getAsJsonObject().get("error").getAsString();}catch(Exception ignored){}throw new IllegalStateException(text.isBlank()?"Launcher connection unavailable":text);}return r;
 }
 public static CompletableFuture<JsonObject> json(String path,JsonObject body){return CompletableFuture.supplyAsync(()->{try{return JsonParser.parseString(new String(request(path,body).body(),java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();}catch(Exception e){throw new CompletionException(e);}},WORK);}
 private LauncherBridge(){}
}
