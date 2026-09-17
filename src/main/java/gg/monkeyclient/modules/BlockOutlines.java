package gg.monkeyclient.modules;
import gg.monkeyclient.module.Module;
import gg.monkeyclient.module.Category;
import gg.monkeyclient.module.setting.*;
public class BlockOutlines extends Module {
 public final BoolSetting visible=add(new BoolSetting("visible","Show target outline",true)),themed=add(new BoolSetting("themed","Match theme color",true));
 public final ColorSetting color=add(new ColorSetting("color","Outline color / opacity / rainbow",0xFF7CC24A));
 public final NumberSetting thickness=add(new NumberSetting("thickness","Line width",2,1,10,.25));
 public BlockOutlines(){super("outlines","Block Outlines","Color, thickness and rainbow target outlines",Category.VISUAL,false);}
 public int color(){return themed.get()&&!color.rainbow?(color.get()&0xFF000000)|(gg.monkeyclient.MonkeyClient.theme().accent&0xFFFFFF):color.resolve();}
}
