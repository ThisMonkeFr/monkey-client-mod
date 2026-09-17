package gg.monkeyclient.mixin;
import gg.monkeyclient.MonkeyClient;
import gg.monkeyclient.modules.GuiScale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import com.mojang.blaze3d.systems.RenderPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(GuiRenderer.class)
public class GuiProjectionMixin {
 @Redirect(method="addElementToMesh",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/render/state/GuiElementRenderState;buildVertices(Lcom/mojang/blaze3d/vertex/VertexConsumer;)V"),require=1)
 private void monkey$zoom(net.minecraft.client.gui.render.state.GuiElementRenderState element,com.mojang.blaze3d.vertex.VertexConsumer consumer){var mc=Minecraft.getInstance();float zoom=MonkeyClient.ready()&&mc.screen==null?MonkeyClient.modules().get(gg.monkeyclient.modules.Zoom.class).visualScale():1;var w=mc.getWindow();element.buildVertices(zoom<=1.0001f?consumer:new gg.monkeyclient.render.ZoomVertexConsumer(consumer,zoom,w.getGuiScaledWidth()/2f,w.getGuiScaledHeight()/2f));}
 @ModifyArg(method="draw",at=@At(value="INVOKE",target="Lnet/minecraft/client/renderer/CachedOrthoProjectionMatrixBuffer;getBuffer(FF)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;"),index=0,require=1)
 private float monkey$width(float original){if(MonkeyClient.ready()){var m=MonkeyClient.modules().get(GuiScale.class);if(m.isEnabled())return(float)(Minecraft.getInstance().getWindow().getWidth()/m.effectiveScale());}return original;}
 @ModifyArg(method="draw",at=@At(value="INVOKE",target="Lnet/minecraft/client/renderer/CachedOrthoProjectionMatrixBuffer;getBuffer(FF)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;"),index=1,require=1)
 private float monkey$height(float original){if(MonkeyClient.ready()){var m=MonkeyClient.modules().get(GuiScale.class);if(m.isEnabled())return(float)(Minecraft.getInstance().getWindow().getHeight()/m.effectiveScale());}return original;}
 @Inject(method="enableScissor",at=@At("HEAD"),cancellable=true,require=1)
 private void monkey$scissor(ScreenRectangle area,RenderPass pass,CallbackInfo ci){if(!MonkeyClient.ready())return;var mc=Minecraft.getInstance();var m=MonkeyClient.modules().get(GuiScale.class);double zoom=mc.screen==null?MonkeyClient.modules().get(gg.monkeyclient.modules.Zoom.class).visualScale():1;if(!m.isEnabled()&&zoom<=1.0001)return;var w=mc.getWindow();double s=m.isEnabled()?m.effectiveScale():w.getGuiScale(),cx=w.getWidth()/s/2,cy=w.getHeight()/s/2;int l=Math.max(0,(int)Math.floor((cx+(area.left()-cx)*zoom)*s)),t=Math.max(0,(int)Math.floor((cy+(area.top()-cy)*zoom)*s)),r=Math.min(w.getWidth(),(int)Math.ceil((cx+(area.right()-cx)*zoom)*s)),b=Math.min(w.getHeight(),(int)Math.ceil((cy+(area.bottom()-cy)*zoom)*s));pass.enableScissor(l,w.getHeight()-b,Math.max(0,r-l),Math.max(0,b-t));ci.cancel();}
}
