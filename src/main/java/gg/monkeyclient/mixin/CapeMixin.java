package gg.monkeyclient.mixin;
import gg.monkeyclient.cosmetics.LauncherCape;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.player.PlayerSkin;
import com.mojang.authlib.GameProfile;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(PlayerInfo.class)
public class CapeMixin {
 @Shadow public GameProfile getProfile(){throw new AssertionError();}
 @Inject(method="getSkin",at=@At("RETURN"),cancellable=true,require=1)
 private void monkeyclient$cape(CallbackInfoReturnable<PlayerSkin> cir){if(gg.monkeyclient.MonkeyClient.ready())cir.setReturnValue(LauncherCape.apply(getProfile().id(),cir.getReturnValue()));}
}
