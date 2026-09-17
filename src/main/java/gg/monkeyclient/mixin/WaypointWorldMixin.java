package gg.monkeyclient.mixin;
import gg.monkeyclient.MonkeyClient;
import gg.monkeyclient.modules.Waypoints;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(LevelRenderer.class)
public class WaypointWorldMixin {
 @Inject(method="submitFeatures",at=@At("RETURN"),require=1)
 private void monkey$areas(LevelRenderState state,SubmitNodeCollector collector,boolean outline,CallbackInfo ci){if(MonkeyClient.ready()){var m=MonkeyClient.modules().get(Waypoints.class);if(m.isEnabled())m.submitAreas(state,collector);}}
}
