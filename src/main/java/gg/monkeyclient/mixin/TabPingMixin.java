package gg.monkeyclient.mixin;
import gg.monkeyclient.MonkeyClient;
import gg.monkeyclient.modules.PingDisplay;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(PlayerTabOverlay.class)
public class TabPingMixin {
    @Inject(method="extractPingIcon",at=@At("HEAD"),cancellable=true,require=1)
    private void monkeyclient$hideBars(org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci){if(MonkeyClient.ready()){var p=MonkeyClient.modules().get(PingDisplay.class);if(p.isEnabled()&&p.showInTab())ci.cancel();}}
    // Appending before vanilla measures the name also reserves the correct column width.
    @Inject(method = "getNameForDisplay", at = @At("RETURN"), cancellable = true, require = 1)
    private void monkeyclient$ping(PlayerInfo player, CallbackInfoReturnable<Component> cir) {
        if (!MonkeyClient.ready()) return;
        var tiers=MonkeyClient.modules().get(gg.monkeyclient.modules.TiersDisplay.class);
        if(tiers.isEnabled()&&tiers.tab.get())cir.setReturnValue(tiers.append(cir.getReturnValue(),player.getProfile().id()));
        var p=MonkeyClient.modules().get(PingDisplay.class);
        if (!p.isEnabled() || !p.showInTab()) return;
        int ms=player.getLatency();
        cir.setReturnValue(cir.getReturnValue().copy().append(Component.literal("  "+(ms<0?"--":ms)+" ms").withStyle(s -> s.withColor(p.colour(ms) & 0xFFFFFF))));
    }
}
