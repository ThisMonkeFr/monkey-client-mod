package gg.monkeyclient.ui;
import com.google.gson.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.gui.components.EditBox;
import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
/** Messages are fetched on entry or a change notification, never every frame. */
public final class FriendsScreen extends NativeScreen {
 private JsonArray friends=new JsonArray(),groups=new JsonArray(),requests=new JsonArray(),messages=new JsonArray(),members=new JsonArray();
 private JsonObject account=new JsonObject(),conversation,attachment;private String text="";private boolean group,sending,polling;private long revision=-1,nextPoll;
 private int contactsPage,messageScroll,memberPage,rail,side,chatX,chatW;private EditBox composer;private final Set<String> avatars=new HashSet<>();
 public FriendsScreen(Screen parent){super(parent,"Friends");}
 @Override protected void onOpened(){avatars.clear();refresh();}
 private void refresh(){call("/social",new JsonObject(),r->{friends=array(r,"friends");groups=array(r,"groups");requests=array(r,"requests");account=r.getAsJsonObject("account");if(conversation!=null){String id=target();conversation=(group?groups:friends).asList().stream().map(JsonElement::getAsJsonObject).filter(c->id.equals(str(c,group?"id":"uuid"))).findFirst().orElse(null);}loaded=true;rebuild();if(conversation!=null)history();if(!yes(r,"connected"))status="MonkeyNet is reconnecting...";});}
 private void net(String method,ConsumerResult done,JsonElement...args){JsonArray a=new JsonArray();for(var v:args)a.add(v);call("/net",obj("method",method,"args",a),done::accept);}
 private interface ConsumerResult{void accept(JsonObject value);}
 private JsonPrimitive value(String s){return new JsonPrimitive(s);}
 private String target(){return str(conversation,group?"id":"uuid");}
 private void history(){String id=target();boolean wasGroup=group;net(group?"groupHistory":"history",r->{if(!id.equals(target())||wasGroup!=group)return;messages=array(r,"messages");loadAvatars();rebuild();},value(id));if(group)net("groupMembers",r->{if(id.equals(target())&&group){members=array(r,"members");rebuild();}},value(id));}
 private void loadAvatars(){int start=Math.max(0,messages.size()-16);for(int i=start;i<messages.size();i++){var m=messages.get(i).getAsJsonObject();String id=yes(m,"me")?str(account,"uuid"):group?str(m,"from"):str(conversation,"uuid");if(!id.isBlank()&&avatars.add(id))request("/avatar",obj("uuid",id),r->picture(id,str(r,"data")),false);}}
 private void choose(JsonObject c,boolean isGroup){conversation=c;group=isGroup;messages=new JsonArray();members=new JsonArray();attachment=null;messageScroll=memberPage=0;rebuild();history();}
 @Override protected void rebuild(){
  boolean writing=composer!=null&&composer.isFocused();int cursor=composer==null?0:composer.getCursorPosition();
  chrome("friends");rail=Math.max(100,Math.min(160,pw/4));side=group?Math.max(90,Math.min(126,pw/4)):0;chatX=left+rail+20;chatW=pw-rail-side-30;
  int count=Math.max(1,(bottom-body-56)/23),start=contactsPage*count,total=friends.size()+groups.size();if(start>=total&&contactsPage>0){contactsPage--;start=contactsPage*count;}
  button(left+10,body,rail/2-2,"+ Friend",()->minecraft.setScreenAndShow(new FriendRequestScreen(this)));
  button(left+12+rail/2,body,rail/2-2,"Requests",()->minecraft.setScreenAndShow(new RequestListScreen(this,requests)));
  for(int i=start;i<Math.min(total,start+count);i++){boolean isGroup=i>=friends.size();var c=(isGroup?groups.get(i-friends.size()):friends.get(i)).getAsJsonObject();String name=(isGroup?"# ":yes(c,"online")?"+ ":"")+str(c,"name");addRenderableWidget(new MenuButton(left+10,body+25+(i-start)*23,rail,20,name,()->choose(c,isGroup),()->conversation!=null&&target().equals(str(c,isGroup?"id":"uuid"))&&group==isGroup));}
  int y=bottom-22;button(left+10,y,24,"<",()->{contactsPage=Math.max(0,contactsPage-1);rebuild();}).active=contactsPage>0;
  button(left+38,y,rail-56,"New group",()->minecraft.setScreenAndShow(new GroupEditorScreen(this,friends,null)));
  button(left+rail-14,y,24,">",()->{contactsPage++;rebuild();}).active=start+count<total;
  if(conversation==null)return;
  int controlX=chatX+chatW-102;
  if(group){button(controlX,body,48,"Members",()->minecraft.setScreenAndShow(new GroupMembersScreen(this,members,friends,str(account,"uuid"))));button(controlX+52,body,50,"Group",()->minecraft.setScreenAndShow(new GroupEditorScreen(this,friends,conversation)));}
  else button(controlX+46,body,56,"Unfriend",()->minecraft.setScreenAndShow(new ConfirmScreen(ok->{minecraft.setScreenAndShow(this);if(ok)net("removeFriend",r->{conversation=null;refresh();},value(target()));},net.minecraft.network.chat.Component.literal("Remove friend?"),net.minecraft.network.chat.Component.literal(str(conversation,"name")))));
  int cy=bottom-22;composer=field(chatX,cy,chatW-85,"Message",text,1000);composer.setResponder(v->text=v);if(writing){setFocused(composer);composer.setFocused(true);composer.setCursorPosition(Math.min(cursor,text.length()));}
  button(chatX+chatW-81,cy,24,"+",()->minecraft.setScreenAndShow(new LauncherScreen(this,"screenshots",shot->{attachment=shot;})));
  button(chatX+chatW-53,cy,53,sending?"...":"Send",this::send).active=!sending;
  if(attachment!=null)button(chatX+chatW-62,cy-23,62,"Detach",()->{attachment=null;rebuild();});
  if(side>0){int memberCount=Math.max(1,(bottom-body-42)/33),first=memberPage*memberCount;for(int i=first;i<Math.min(members.size(),first+memberCount);i++){var m=members.get(i).getAsJsonObject();String id=str(m,"uuid");if(!id.equals(str(account,"uuid"))&&friends.asList().stream().noneMatch(f->id.equals(str(f.getAsJsonObject(),"uuid"))))button(left+pw-side+5,body+35+(i-first)*33,side-15,"Add friend",()->net("addFriend",r->status="Friend request sent",value(str(m,"name"))));}button(left+pw-side+5,bottom-22,26,"<",()->{memberPage=Math.max(0,memberPage-1);rebuild();}).active=memberPage>0;button(left+pw-36,bottom-22,26,">",()->{memberPage++;rebuild();}).active=first+memberCount<members.size();}
 }
 private void send(){if(sending||conversation==null||(text.isBlank()&&attachment==null))return;sending=true;rebuild();String id=target(),draft=text;boolean toGroup=group;JsonObject shot=attachment;
  java.util.function.Consumer<String> deliver=attachmentId->{JsonArray args=new JsonArray();args.add(id);args.add(draft);if(attachmentId!=null)args.add(attachmentId);gg.monkeyclient.integration.LauncherBridge.json("/net",obj("method",toGroup?"sendGroup":"send","args",args)).whenComplete((r,e)->minecraft.execute(()->{sending=false;if(e==null){if(id.equals(target())&&toGroup==group){if(text.equals(draft))text="";if(attachment==shot)attachment=null;messageScroll=0;history();}status="Sent";}else status="Could not send: "+e.getCause().getMessage();rebuild();}));};
  if(shot==null)deliver.accept(null);else gg.monkeyclient.integration.LauncherBridge.json("/screenshots/attach",obj("id",str(shot,"id"),toGroup?"groupId":"to",id)).whenComplete((r,e)->minecraft.execute(()->{if(e!=null){sending=false;status="Could not attach screenshot: "+e.getCause().getMessage();rebuild();}else deliver.accept(str(r.getAsJsonObject("attachment"),"id"));}));
 }
 @Override public void tick(){super.tick();if(!loaded||polling||System.nanoTime()<nextPoll)return;polling=true;nextPoll=System.nanoTime()+2_000_000_000L;gg.monkeyclient.integration.LauncherBridge.json("/events",null).whenComplete((r,e)->minecraft.execute(()->{polling=false;if(minecraft.gui.screen()!=this||e!=null)return;long next=r.get("revision").getAsLong();if(revision>=0&&next!=revision)refresh();if(r.has("error")&&r.getAsJsonObject("error").get("revision").getAsLong()>revision)status=str(r.getAsJsonObject("error"),"message");revision=next;}));}
 @Override public boolean keyPressed(KeyEvent e){if(e.key()==257&&composer!=null&&composer.isFocused()){send();return true;}return super.keyPressed(e);}
 @Override public boolean mouseScrolled(double x,double y,double dx,double dy){if(x>=chatX&&x<chatX+chatW&&y>body+24&&y<bottom-24){messageScroll=Math.max(0,Math.min(Math.max(0,messages.size()-1),messageScroll+(dy>0?1:-1)));return true;}return super.mouseScrolled(x,y,dx,dy);}
 @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float dt){super.extractRenderState(g,mx,my,dt);if(conversation==null){label(g,"Choose a friend or group",chatX,body+32,chatW);return;}label(g,str(conversation,"name"),chatX,body+6,chatW-108);int y=bottom-(attachment==null?30:54);
  for(int i=messages.size()-1-messageScroll;i>=0;i--){var m=messages.get(i).getAsJsonObject();var lines=font.split(net.minecraft.network.chat.Component.literal(str(m,"t")),Math.max(30,chatW-32)).stream().limit(4).toList();int more=font.split(net.minecraft.network.chat.Component.literal(str(m,"t")),Math.max(30,chatW-32)).size()>4?12:0;int h=23+more+Math.max(1,lines.size())*10+(str(m,"attachmentId").isBlank()?0:17);if(y-h<body+28)break;y-=h;
   String author=yes(m,"me")?str(account,"name"):group?str(m,"name"):str(conversation,"name"),id=yes(m,"me")?str(account,"uuid"):group?str(m,"from"):str(conversation,"uuid");drawPicture(g,id,chatX,y,18,18);label(g,author,chatX+23,y,chatW-90);
   if(m.has("at")){String time=DateTimeFormatter.ofPattern("HH:mm").format(Instant.ofEpochMilli(m.get("at").getAsLong()).atZone(ZoneId.systemDefault()));label(g,time,chatX+chatW-35,y,35);}
   int ly=y+12;for(var line:lines){g.text(font,line,chatX+23,ly,gg.monkeyclient.MonkeyClient.theme().ink(),false);ly+=10;}
   if(more>0){label(g,"[Read more]",chatX+23,ly,chatW-30);ly+=12;}
   if(!str(m,"attachmentId").isBlank()){label(g,"[Open screenshot]",chatX+23,ly,chatW-30);if(mx>=chatX+23&&mx<chatX+chatW&&my>=ly&&my<ly+12)g.fill(chatX+23,ly+10,chatX+Math.min(chatW,120),ly+11,gg.monkeyclient.MonkeyClient.theme().accent);}
  }
  if(attachment!=null)label(g,"Attached: "+str(attachment,"name"),chatX,bottom-43,chatW-70);
  if(side>0){int count=Math.max(1,(bottom-body-42)/33),start=memberPage*count;label(g,"Members ("+members.size()+")",left+pw-side+5,body+5,side-10);for(int i=start;i<Math.min(members.size(),start+count);i++)label(g,str(members.get(i).getAsJsonObject(),"name"),left+pw-side+5,body+24+(i-start)*33,side-10);}
 }
 @Override public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent e,boolean twice){if(e.button()==0&&conversation!=null){int y=bottom-(attachment==null?30:54);for(int i=messages.size()-1-messageScroll;i>=0;i--){var m=messages.get(i).getAsJsonObject();int lines=Math.max(1,font.split(net.minecraft.network.chat.Component.literal(str(m,"t")),Math.max(30,chatW-32)).stream().limit(4).toList().size());int more=font.split(net.minecraft.network.chat.Component.literal(str(m,"t")),Math.max(30,chatW-32)).size()>4?12:0;int h=23+more+lines*10+(str(m,"attachmentId").isBlank()?0:17);if(y-h<body+28)break;y-=h;int ly=y+12+lines*10+more;if(e.x()>=chatX+23&&e.x()<chatX+chatW&&e.y()>=y&&e.y()<ly&&font.split(net.minecraft.network.chat.Component.literal(str(m,"t")),Math.max(30,chatW-32)).size()>4){minecraft.setScreenAndShow(new NativeTextScreen(this,"Message",str(m,"t")));return true;}if(!str(m,"attachmentId").isBlank()&&e.x()>=chatX+23&&e.x()<chatX+chatW&&e.y()>=ly&&e.y()<ly+12){net("attachment",r->minecraft.setScreenAndShow(new NativeImageScreen(this,str(r,"name"),str(r,"data"))),value(str(m,"attachmentId")),new JsonPrimitive(false));return true;}}}return super.mouseClicked(e,twice);}
}
