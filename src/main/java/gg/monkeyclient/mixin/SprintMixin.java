package gg.monkeyclient.mixin;

import gg.monkeyclient.MonkeyClient;
import gg.monkeyclient.modules.ToggleSprint;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Vanilla drops sprint whenever the key is released. Re-applying after the
 * tick is cheaper and less fragile than cancelling the release itself.
 */
@Mixin(LocalPlayer.class)
public class SprintMixin {

    @Inject(method = "tick", at = @At("TAIL"), require = 0)
    private void monkeyclient$holdSprint(CallbackInfo ci) {
        if (!MonkeyClient.ready()) return;
        ToggleSprint sprint = MonkeyClient.modules().get(ToggleSprint.class);
        if (sprint != null && sprint.isEnabled()) sprint.onTick();
    }
}
