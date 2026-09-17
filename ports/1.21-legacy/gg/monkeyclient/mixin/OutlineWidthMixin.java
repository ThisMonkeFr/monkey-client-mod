package gg.monkeyclient.mixin;
import gg.monkeyclient.MonkeyClient;
import gg.monkeyclient.modules.BlockOutlines;
import gg.monkeyclient.render.OutlineContext;
import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(RenderSystem.class)
public class OutlineWidthMixin {
 @Inject(method="getShaderLineWidth",at=@At("RETURN"),cancellable=true,require=1)
 private static void monkey$width(CallbackInfoReturnable<Float> cir){if(MonkeyClient.ready()&&OutlineContext.active.get()){var m=MonkeyClient.modules().get(BlockOutlines.class);if(m.isEnabled())cir.setReturnValue(m.thickness.getFloat());}}
}
