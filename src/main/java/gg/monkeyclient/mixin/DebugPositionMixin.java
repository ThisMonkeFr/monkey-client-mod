package gg.monkeyclient.mixin;

import gg.monkeyclient.MonkeyClient;
import gg.monkeyclient.modules.CoordsDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugEntryPosition;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Locale;

/** Position is a five-line vanilla entry; coords-only replaces its contents. */
@Mixin(DebugEntryPosition.class)
public class DebugPositionMixin {
    @Inject(method="display", at=@At("HEAD"), cancellable=true, require=1)
    private void monkeyclient$xyz(DebugScreenDisplayer display, Level level, LevelChunk clientChunk, LevelChunk serverChunk, CallbackInfo ci) {
        if (!MonkeyClient.ready()) return;
        var editor = MonkeyClient.modules().get(CoordsDisplay.class);
        if (!editor.isEnabled() || !editor.coordsOnly.get()) return;
        var entity = Minecraft.getInstance().getCameraEntity();
        if (entity != null) display.addLine(String.format(Locale.ROOT, "XYZ: %.3f / %.3f / %.3f", entity.getX(), entity.getY(), entity.getZ()));
        ci.cancel();
    }
}
