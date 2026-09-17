package gg.monkeyclient.integration.cache.mixin.sodium06;
import net.caffeinemc.mods.sodium.client.gui.SodiumGameOptionPages;
import gg.monkeyclient.integration.cache.Bobby;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(value=SodiumGameOptionPages.class,remap=false)
public class LegacySodiumOptionsMixin {
 @ModifyConstant(method="lambda$general$0",constant=@Constant(intValue=32),require=1,remap=false)
 private static int monkey$range(int old){return Math.max(old,Bobby.getInstance().getConfig().getMaxRenderDistance());}
}
