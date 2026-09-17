package gg.monkeyclient.mixin;
import gg.monkeyclient.MonkeyClient;
import gg.monkeyclient.modules.FogEditor;
import net.minecraft.client.renderer.CloudRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(CloudRenderer.class)
public class CloudEditorMixin {
 private static FogEditor module(){if(!MonkeyClient.ready())return null;var m=MonkeyClient.modules().get(FogEditor.class);return m.isEnabled()?m:null;}
 @Inject(method="render",at=@At("HEAD"),cancellable=true,require=1)
 private void monkey$clouds(CallbackInfo ci){var m=module();if(m!=null&&!m.clouds.get())ci.cancel();}
 @ModifyVariable(method="render",at=@At("HEAD"),argsOnly=true,ordinal=0,require=1)
 private float monkey$height(float h){var m=module();return m==null?h:h+m.cloudHeight.getFloat();}
 @ModifyVariable(method="render",at=@At("HEAD"),argsOnly=true,ordinal=0,require=1)
 private int monkey$color(int color){var m=module();return m!=null&&m.cloudColorOn.get()?m.cloudColor.resolve():color;}
}
