package gg.monkeyclient.mixin;
import gg.monkeyclient.MonkeyClient;
import gg.monkeyclient.modules.ContainerPreview;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(AbstractContainerScreen.class)
public class ContainerMixin {
 @Shadow protected Slot hoveredSlot;
 @Inject(method="extractTooltip",at=@At("HEAD"),cancellable=true,require=1)
 private void monkeyclient$preview(GuiGraphicsExtractor g,int x,int y,CallbackInfo ci){
  if(!MonkeyClient.ready())return;
  var m=MonkeyClient.modules().get(ContainerPreview.class);if(!m.isEnabled())return;
  if(m.drawPinned(g,x,y)){ci.cancel();return;}
  if(hoveredSlot!=null&&hoveredSlot.hasItem()&&m.draw(g,hoveredSlot.getItem(),x,y))ci.cancel();
 }
 @Inject(method="mouseClicked",at=@At("HEAD"),cancellable=true,require=1)
 private void monkeyclient$inspectOnly(org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir){
  if(MonkeyClient.ready()){var m=MonkeyClient.modules().get(ContainerPreview.class);if(m.isEnabled()&&m.inspecting())cir.setReturnValue(true);}
 }
}
