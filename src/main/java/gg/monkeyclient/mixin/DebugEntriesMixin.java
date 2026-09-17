package gg.monkeyclient.mixin;
import gg.monkeyclient.MonkeyClient;
import gg.monkeyclient.modules.CoordsDisplay;
import net.minecraft.client.gui.components.debug.DebugScreenEntryList;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Collection;
@Mixin(DebugScreenEntryList.class)
public class DebugEntriesMixin {
    @Shadow public boolean isOverlayVisible() { throw new AssertionError(); }
    @Inject(method = "getCurrentlyEnabled", at = @At("HEAD"), cancellable = true, require = 1)
    private void monkeyclient$entries(CallbackInfoReturnable<Collection<Identifier>> cir) {
        if (!MonkeyClient.ready()) return;
        var editor = MonkeyClient.modules().get(CoordsDisplay.class);
        if (editor.isEnabled()) cir.setReturnValue(isOverlayVisible() ? editor.selected() : java.util.List.of());
    }
    @Inject(method = "isCurrentlyEnabled", at = @At("HEAD"), cancellable = true, require = 1)
    private void monkeyclient$entry(Identifier id, CallbackInfoReturnable<Boolean> cir) {
        if (!MonkeyClient.ready()) return;
        var editor = MonkeyClient.modules().get(CoordsDisplay.class);
        if (editor.isEnabled()) cir.setReturnValue(isOverlayVisible() && editor.includes(id));
    }
}