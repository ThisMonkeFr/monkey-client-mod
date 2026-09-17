package gg.monkeyclient.mixin;
import gg.monkeyclient.MonkeyClient;
import gg.monkeyclient.modules.GuiScale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.Projection;
import com.mojang.blaze3d.systems.RenderPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(GuiRenderer.class)
public class GuiProjectionMixin {
 @Redirect(method="addElementToMesh",at=@At(value="INVOKE",target="Lnet/minecraft/client/renderer/state/gui/GuiElementRenderState;buildVertices(Lcom/mojang/blaze3d/vertex/VertexConsumer;)V"),require=1)
 private void monkeyclient$zoomGui(net.minecraft.client.renderer.state.gui.GuiElementRenderState element,com.mojang.blaze3d.vertex.VertexConsumer consumer){
  var mc=Minecraft.getInstance();float zoom=MonkeyClient.ready()&&mc.gui.screen()==null?MonkeyClient.modules().get(gg.monkeyclient.modules.Zoom.class).visualScale():1;
  var window=mc.getWindow();element.buildVertices(zoom<=1.0001f?consumer:new gg.monkeyclient.render.ZoomVertexConsumer(consumer,zoom,window.getGuiScaledWidth()/2f,window.getGuiScaledHeight()/2f));
 }

 @Redirect(method="draw",at=@At(value="INVOKE",target="Lnet/minecraft/client/renderer/Projection;setupOrtho(FFFFZ)V"),require=1)
 private void monkeyclient$projection(Projection projection,float near,float far,float width,float height,boolean yDown){
  if(MonkeyClient.ready()){var m=MonkeyClient.modules().get(GuiScale.class);if(m.isEnabled()){var w=Minecraft.getInstance().gameRenderer.gameRenderState().windowRenderState;width=(float)(w.width/m.effectiveScale());height=(float)(w.height/m.effectiveScale());}}
  projection.setupOrtho(near,far,width,height,yDown);
 }
 @Inject(method="enableScissor",at=@At("HEAD"),cancellable=true,require=1)
 private void monkeyclient$scissor(ScreenRectangle area,RenderPass pass,CallbackInfo ci){
  if(!MonkeyClient.ready())return;var mc=Minecraft.getInstance();var m=MonkeyClient.modules().get(GuiScale.class);
  double zoom=mc.gui.screen()==null?MonkeyClient.modules().get(gg.monkeyclient.modules.Zoom.class).visualScale():1;
  if(!m.isEnabled()&&zoom<=1.0001)return;
  var w=mc.gameRenderer.gameRenderState().windowRenderState;double s=m.isEnabled()?m.effectiveScale():mc.getWindow().getGuiScale();
  double cx=w.width/s/2,cy=w.height/s/2;
  int left=Math.max(0,(int)Math.floor((cx+(area.left()-cx)*zoom)*s)),top=Math.max(0,(int)Math.floor((cy+(area.top()-cy)*zoom)*s)),right=Math.min(w.width,(int)Math.ceil((cx+(area.right()-cx)*zoom)*s)),bottom=Math.min(w.height,(int)Math.ceil((cy+(area.bottom()-cy)*zoom)*s));
  pass.enableScissor(left,w.height-bottom,Math.max(0,right-left),Math.max(0,bottom-top));ci.cancel();
 }
}
