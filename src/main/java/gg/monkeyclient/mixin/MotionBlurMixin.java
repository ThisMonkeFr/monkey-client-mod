package gg.monkeyclient.mixin;
import gg.monkeyclient.MonkeyClient;
import gg.monkeyclient.modules.MotionBlur;
import net.minecraft.client.renderer.GameRenderer;
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(GameRenderer.class)
public class MotionBlurMixin {
 @Shadow @Final private CrossFrameResourcePool resourcePool;
 @ModifyArg(method="renderLevel",at=@At(value="INVOKE",target="Lnet/minecraft/client/renderer/Projection;setupPerspective(FFFFF)V"),index=2,require=1)
 private float monkeyclient$zoomBothHands(float fov){return MonkeyClient.ready()?MonkeyClient.modules().get(gg.monkeyclient.modules.Zoom.class).handFov(fov):fov;}
 @Inject(method="renderLevel",at=@At("TAIL"),require=1)
 private void monkeyclient$blur(CallbackInfo ci){if(MonkeyClient.ready())MonkeyClient.modules().get(MotionBlur.class).render(resourcePool);}
}
