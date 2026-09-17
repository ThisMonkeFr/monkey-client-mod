package gg.monkeyclient.mixin;
import gg.monkeyclient.MonkeyClient;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Minecraft.class)
public class ForgeTickMixin {
 @Inject(method="tick",at=@At("TAIL"),require=1)
 private void monkey$tick(CallbackInfo ci){if(!MonkeyClient.ready())new MonkeyClient().onInitializeClient();MonkeyClient.onTick((Minecraft)(Object)this);}
}
