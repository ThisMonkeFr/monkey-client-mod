package gg.monkeyclient.ui;
import com.google.gson.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
public final class GroupMembersScreen extends NativeScreen {
 private final JsonArray members,friends;private final String own;private int page,count;
 public GroupMembersScreen(Screen parent,JsonArray members,JsonArray friends,String own){super(parent,"Group members");this.members=members;this.friends=friends;this.own=own;}
 @Override protected void rebuild(){clearWidgets();button(left+pw-70,top+8,60,"Back",this::onClose);count=Math.max(1,(bottom-body-24)/28);for(int i=page*count;i<Math.min(members.size(),(page+1)*count);i++){var r=members.get(i).getAsJsonObject();String id=str(r,"uuid");boolean added=id.equals(own)||friends.asList().stream().anyMatch(f->id.equals(str(f.getAsJsonObject(),"uuid")));var b=button(left+pw-100,body+(i-page*count)*28,86,added?id.equals(own)?"You":"Friend":"Add friend",()->{JsonArray a=new JsonArray();a.add(str(r,"name"));call("/net",obj("method","addFriend","args",a),v->status="Friend request sent");});b.active=!added;}button(left+12,bottom-22,50,"<",()->{page--;rebuild();}).active=page>0;button(left+68,bottom-22,50,">",()->{page++;rebuild();}).active=(page+1)*count<members.size();}
 @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float dt){super.extractRenderState(g,mx,my,dt);for(int i=page*count;i<Math.min(members.size(),(page+1)*count);i++)label(g,str(members.get(i).getAsJsonObject(),"name"),left+14,body+6+(i-page*count)*28,pw-124);}
}
