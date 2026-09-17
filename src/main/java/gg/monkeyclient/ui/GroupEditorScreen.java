package gg.monkeyclient.ui;
import com.google.gson.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.*;
import java.util.*;
public final class GroupEditorScreen extends NativeScreen {
 private final JsonArray friends;private final JsonObject existing;private final Set<String> selected=new HashSet<>();private String name="",icon="";private int page,count;private boolean owner=true;
 public GroupEditorScreen(Screen parent,JsonArray friends,JsonObject group){super(parent,group==null?"Create a group":"Group settings");this.friends=friends;this.existing=group;if(group!=null){name=str(group,"name");icon=str(group,"icon");}}
 @Override protected void init(){super.init();picture("icon",icon);if(existing!=null)call("/social",new JsonObject(),r->{owner=str(r.getAsJsonObject("account"),"uuid").equals(str(existing,"owner"));rebuild();});}
 @Override protected void rebuild(){clearWidgets();button(left+pw-70,top+8,60,"Back",this::onClose);var f=field(left+14,body,pw-144,"Group name",name,40);f.setResponder(v->name=v);f.active=owner;
  button(left+pw-124,body,110,"Choose icon",()->call("/image/pick",new JsonObject(),r->{if(yes(r,"cancelled"))return;icon=str(r,"data");clearPictures();picture("icon",icon);})).active=owner;
  count=Math.max(1,(bottom-body-64)/23);if(existing==null)for(int i=page*count;i<Math.min(friends.size(),(page+1)*count);i++){var person=friends.get(i).getAsJsonObject();String id=str(person,"uuid");addRenderableWidget(new MenuButton(left+14,body+36+(i-page*count)*23,pw-104,20,(selected.contains(id)?"[x] ":"[ ] ")+str(person,"name"),()->{if(!selected.remove(id)&&selected.size()<31)selected.add(id);rebuild();},()->selected.contains(id)));}
  button(left+14,bottom-22,90,existing==null?"Create group":"Save changes",this::save).active=owner;
  if(existing==null){button(left+112,bottom-22,24,"<",()->{page--;rebuild();}).active=page>0;button(left+142,bottom-22,24,">",()->{page++;rebuild();}).active=(page+1)*count<friends.size();}
  else button(left+112,bottom-22,90,"Leave group",()->minecraft.setScreenAndShow(new ConfirmScreen(ok->{minecraft.setScreenAndShow(this);if(ok){JsonArray args=new JsonArray();args.add(str(existing,"id"));call("/net",obj("method","leaveGroup","args",args),r->minecraft.setScreenAndShow(new FriendsScreen(parent instanceof NativeScreen n?n.parent:parent)));}},net.minecraft.network.chat.Component.literal("Leave this group?"),net.minecraft.network.chat.Component.literal(name))));
 }
 private void save(){if(name.isBlank()||icon.isBlank()||(existing==null&&selected.isEmpty())){status="Choose a name, an icon and at least one friend.";return;}JsonArray people=new JsonArray();selected.forEach(people::add);JsonObject data=obj("name",name,"icon",icon);if(existing==null)data.add("members",people);JsonArray args=new JsonArray();if(existing!=null)args.add(str(existing,"id"));args.add(data);call("/net",obj("method",existing==null?"createGroup":"updateGroup","args",args),r->onClose());}
 @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float dt){super.extractRenderState(g,mx,my,dt);drawPicture(g,"icon",left+pw-76,body+36,60,60);if(existing==null&&friends.isEmpty())label(g,"Add friends before creating a group.",left+14,body+40,pw-104);if(existing!=null)label(g,owner?"Your changes are shared with all members.":"Only the group owner can edit its name and icon.",left+14,body+38,pw-104);}
}
