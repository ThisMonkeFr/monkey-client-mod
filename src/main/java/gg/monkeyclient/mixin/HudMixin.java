package gg.monkeyclient.mixin;
import gg.monkeyclient.MonkeyClient;
import gg.monkeyclient.hud.HudManager;
import gg.monkeyclient.modules.*;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.RenderPipelines;
@Mixin(Hud.class)
public class HudMixin {
    @Shadow public boolean isHidden() { throw new AssertionError(); }
    @Inject(method = "extractRenderState", at = @At("TAIL"), require = 1)
    private void monkeyclient$drawHud(GuiGraphicsExtractor g, DeltaTracker delta, CallbackInfo ci) {
        if (MonkeyClient.ready() && !isHidden() && Minecraft.getInstance().player != null
            && !(Minecraft.getInstance().gui.screen() instanceof gg.monkeyclient.ui.HudEditorScreen))
            HudManager.renderAll(g, Minecraft.getInstance());
    }
    @Inject(method = "extractEffects", at = @At("HEAD"), cancellable = true, require = 1)
    private void monkeyclient$replaceEffects(GuiGraphicsExtractor g, DeltaTracker d, CallbackInfo ci) {
        if (MonkeyClient.ready() && MonkeyClient.modules().get(PotionDisplay.class).isEnabled()) ci.cancel();
    }
    @Inject(method="extractCrosshair",at=@At("HEAD"),cancellable=true,require=1)
    private void monkeyclient$customCrosshair(GuiGraphicsExtractor g,DeltaTracker d,CallbackInfo ci) {
        if(MonkeyClient.ready()){var m=MonkeyClient.modules().get(CustomCrosshair.class);if(m.isEnabled()){m.draw(g,Minecraft.getInstance());ci.cancel();}}
    }
    @WrapOperation(method="extractCrosshair",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"),require=1)
    private void monkeyclient$fadeCrosshair(GuiGraphicsExtractor g,RenderPipeline pipeline,Identifier sprite,int x,int y,int w,int h,Operation<Void> original) {
        float alpha=MonkeyClient.ready()?MonkeyClient.modules().get(Zoom.class).crosshairAlpha():1;
        if(alpha>=.998f)original.call(g,pipeline,sprite,x,y,w,h);
        else if(alpha>.002f)g.blitSprite(RenderPipelines.GUI_TEXTURED,sprite,x,y,w,h,((int)(255*alpha)<<24)|0xFFFFFF);
    }
    @Inject(method="extractRenderState",at=@At("TAIL"),require=1)
    private void monkeyclient$food(GuiGraphicsExtractor g,DeltaTracker d,CallbackInfo ci) {
        if(MonkeyClient.ready()&&!isHidden()){
            var mc=Minecraft.getInstance();
            var w=MonkeyClient.modules().get(Waypoints.class);if(w.isEnabled())w.draw(g,mc);
        }
    }
}
