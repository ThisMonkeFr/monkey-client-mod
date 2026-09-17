package gg.monkeyclient.modules;
import gg.monkeyclient.module.Module;
import gg.monkeyclient.module.Category;
import gg.monkeyclient.module.setting.*;
public class Nametag extends Module {
 public static final ThreadLocal<Boolean> DRAWING=ThreadLocal.withInitial(()->false);
 public final BoolSetting own=add(new BoolSetting("own","Show own nametag",true)),logo=add(new BoolSetting("logo","Monkey badge beside own name",true)),themed=add(new BoolSetting("themed","Match theme color",true));
 public final ColorSetting color=add(new ColorSetting("color","Name color",0xFFFFFFFF));
 public final NumberSetting scale=add(new NumberSetting("scale","Nametag scale",1,.5,3,.1)),range=add(new NumberSetting("range","Maximum distance",64,8,256,1));
 public final StringSetting prefix=add(new StringSetting("prefix","Name prefix","",32));
 public Nametag(){super("nametag","Nametag","Own nametag, Monkey badge, text color and scale",Category.HUD,false);}
}
