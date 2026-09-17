package gg.monkeyclient.mixin;
import gg.monkeyclient.MonkeyClient;
import gg.monkeyclient.modules.BlockOutlines;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(LevelRenderer.class)
public class BlockOutlineMixin {
 @Inject(method="submitBlockOutline",at=@At("HEAD"),cancellable=true,require=1)
 private void monkeyclient$hide(CallbackInfo ci){if(MonkeyClient.ready()){var m=MonkeyClient.modules().get(BlockOutlines.class);if(m.isEnabled()&&!m.visible.get())ci.cancel();}}
 @ModifyVariable(method="submitHitOutline",at=@At("HEAD"),argsOnly=true,ordinal=0,require=1)
 private int monkeyclient$color(int original){if(MonkeyClient.ready()){var m=MonkeyClient.modules().get(BlockOutlines.class);if(m.isEnabled())return m.color();}return original;}
 @ModifyVariable(method="submitHitOutline",at=@At("HEAD"),argsOnly=true,ordinal=0,require=1)
 private float monkeyclient$width(float original){if(MonkeyClient.ready()){var m=MonkeyClient.modules().get(BlockOutlines.class);if(m.isEnabled())return m.thickness.getFloat();}return original;}
}
