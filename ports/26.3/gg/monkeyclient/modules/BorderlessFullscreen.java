package gg.monkeyclient.modules;
import gg.monkeyclient.module.Category;
import gg.monkeyclient.module.Module;
import gg.monkeyclient.module.setting.BoolSetting;
import net.minecraft.client.Minecraft;
/** SDL releases expose borderless fullscreen directly through Minecraft's window. */
public class BorderlessFullscreen extends Module {
 public final BoolSetting autoApply=add(new BoolSetting("auto","Apply on launch",true));
 private boolean applied,savedFullscreen,savedExclusive;
 public BorderlessFullscreen(){super("borderless","Borderless Fullscreen","Undecorated window filling the monitor",Category.MINECRAFT,false);}
 @Override public void onTick(){if(autoApply.get()&&!applied)apply(true);}
 @Override public void onToggle(boolean on){apply(on);}
 private void apply(boolean on){var mc=Minecraft.getInstance();var window=mc.getWindow();if(on&&!applied){savedFullscreen=mc.options.fullscreen().get();savedExclusive=window.isExclusiveFullscreen();window.setExclusiveFullscreen(false);window.setFullscreen(true);window.updateFullscreenIfChanged();applied=true;}else if(!on&&applied){window.setExclusiveFullscreen(savedExclusive);window.setFullscreen(savedFullscreen);window.updateFullscreenIfChanged();applied=false;}}
}
