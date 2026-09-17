package gg.monkeyclient.ui;
import com.google.gson.*;
import net.minecraft.client.gui.screens.Screen;
public final class FriendRequestScreen extends NativeScreen {
 private String name="";
 public FriendRequestScreen(Screen parent){super(parent,"Add a friend");}
 @Override protected void rebuild(){clearWidgets();var f=field(left+16,body,pw-32,"Minecraft username",name,16);f.setResponder(v->name=v);button(left+16,body+28,100,"Send request",()->{JsonArray args=new JsonArray();args.add(name);call("/net",obj("method","addFriend","args",args),r->{status="Friend request sent";});});button(left+124,body+28,70,"Back",this::onClose);}
}
