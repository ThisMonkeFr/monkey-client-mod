package gg.monkeyclient.mixin;
import gg.monkeyclient.MonkeyClient;
import gg.monkeyclient.modules.Brightness;
import gg.monkeyclient.modules.GuiScale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(OptionInstance.class)
public class GammaMixin {
    @Inject(method = "get", at = @At("RETURN"), cancellable = true, require = 1)
    private void monkeyclient$options(CallbackInfoReturnable<Object> cir) {
        if (!MonkeyClient.ready()) return;
        Minecraft mc=Minecraft.getInstance();
        if (mc == null || mc.options == null) return;
        if ((Object)this == mc.options.gamma() && cir.getReturnValue() instanceof Double) {
            var b=MonkeyClient.modules().get(Brightness.class);
            if (b.isEnabled()) cir.setReturnValue(b.effective());
        } else if ((Object)this == mc.options.guiScale() && cir.getReturnValue() instanceof Integer) {
            var s=MonkeyClient.modules().get(GuiScale.class);
            if (s.isEnabled()) cir.setReturnValue((int)Math.ceil(s.scale.get()));
        }
    }
}
