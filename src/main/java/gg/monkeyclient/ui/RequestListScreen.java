package gg.monkeyclient.ui;
import com.google.gson.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
public final class RequestListScreen extends NativeScreen {
 private JsonArray requests;private int page,count;
 public RequestListScreen(Screen parent,JsonArray requests){super(parent,"Friend requests");this.requests=requests.deepCopy();}
 @Override protected void rebuild(){clearWidgets();button(left+pw-70,top+8,60,"Back",this::onClose);count=Math.max(1,(bottom-body-24)/28);for(int i=page*count;i<Math.min(requests.size(),(page+1)*count);i++){var r=requests.get(i).getAsJsonObject();int y=body+(i-page*count)*28;button(left+pw-152,y,66,"Accept",()->act("acceptRequest",r));button(left+pw-80,y,66,"Decline",()->act("declineRequest",r));}button(left+12,bottom-22,50,"<",()->{page--;rebuild();}).active=page>0;button(left+68,bottom-22,50,">",()->{page++;rebuild();}).active=(page+1)*count<requests.size();}
 private void act(String action,JsonObject r){JsonArray args=new JsonArray();args.add(str(r,"id"));call("/net",obj("method",action,"args",args),v->{requests.remove(r);page=0;rebuild();status=action.equals("acceptRequest")?"Friend added":"Request declined";});}
 @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float dt){super.extractRenderState(g,mx,my,dt);for(int i=page*count;i<Math.min(requests.size(),(page+1)*count);i++)label(g,str(requests.get(i).getAsJsonObject(),"name"),left+14,body+6+(i-page*count)*28,pw-180);if(requests.isEmpty())label(g,"No pending requests",left+14,body+6,pw-28);}
}
