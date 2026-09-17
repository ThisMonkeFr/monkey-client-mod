package gg.monkeyclient.mixin;
import gg.monkeyclient.MonkeyClient;
import gg.monkeyclient.modules.FogEditor;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.Camera;
import net.minecraft.world.level.MoonPhase;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
@Mixin(SkyRenderer.class)
public class SkyEditorMixin {
 private static FogEditor module(){if(!MonkeyClient.ready())return null;var m=MonkeyClient.modules().get(FogEditor.class);return m.isEnabled()?m:null;}
 @Inject(method="extractRenderState",at=@At("RETURN"),require=1)
 private void monkey$sky(ClientLevel world,float partial,Camera camera,SkyRenderState state,CallbackInfo ci){var m=module();if(m==null)return;if(m.skyColorOn.get())state.skyColor=m.skyColor.resolve()&0xFFFFFF;if(!m.stars.get())state.starBrightness=0;if(!m.sunrise.get())state.sunriseAndSunsetColor=0;}
 @Inject(method={"renderSkyDisc","renderDarkDisc","renderEndSky"},at=@At("HEAD"),cancellable=true,require=1)
 private void monkey$disc(CallbackInfo ci){var m=module();if(m!=null&&!m.sky.get())ci.cancel();}
 @WrapMethod(method="renderSun")
 private void monkey$sun(float brightness,PoseStack pose,Operation<Void> original){var m=module();if(m!=null&&!m.sun.get())return;pose.pushPose();try{if(m!=null)pose.scale(m.sunScale.getFloat(),1,m.sunScale.getFloat());original.call(brightness,pose);}finally{pose.popPose();}}
 @WrapMethod(method="renderMoon")
 private void monkey$moon(MoonPhase phase,float brightness,PoseStack pose,Operation<Void> original){var m=module();if(m!=null&&!m.moon.get())return;pose.pushPose();try{if(m!=null)pose.scale(m.moonScale.getFloat(),1,m.moonScale.getFloat());original.call(phase,brightness,pose);}finally{pose.popPose();}}
}
