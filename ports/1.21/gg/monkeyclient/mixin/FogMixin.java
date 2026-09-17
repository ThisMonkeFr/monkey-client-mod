package gg.monkeyclient.mixin;
import gg.monkeyclient.MonkeyClient;
import gg.monkeyclient.modules.FogEditor;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.fog.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import com.llamalad7.mixinextras.sugar.Local;
@Mixin(FogRenderer.class)
public class FogMixin {
 @ModifyArgs(method="setupFog",at=@At(value="INVOKE",target="Lnet/minecraft/client/renderer/fog/FogRenderer;updateBuffer(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V"),require=1)
 private void monkey$fog(Args args,@Local(argsOnly=true)Camera camera){if(!MonkeyClient.ready())return;var m=MonkeyClient.modules().get(FogEditor.class);if(!m.isEnabled())return;
  FogData f=new FogData();f.environmentalStart=args.get(3);f.environmentalEnd=args.get(4);f.renderDistanceStart=args.get(5);f.renderDistanceEnd=args.get(6);f.skyEnd=args.get(7);f.cloudEnd=args.get(8);
  m.apply(f,camera.getFluidInCamera(),args.get(2));args.set(3,f.environmentalStart);args.set(4,f.environmentalEnd);args.set(5,f.renderDistanceStart);args.set(6,f.renderDistanceEnd);args.set(7,f.skyEnd);args.set(8,f.cloudEnd);
 }
}
