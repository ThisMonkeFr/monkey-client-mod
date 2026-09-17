package gg.monkeyclient.mixin;
import gg.monkeyclient.MonkeyClient;
import gg.monkeyclient.modules.ItemEditor;
import gg.monkeyclient.cosmetics.ItemAppearanceState;
import net.minecraft.client.renderer.item.*;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.level.Level;
import net.minecraft.core.registries.BuiltInRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ItemModelResolver.class)
public class ItemResolverMixin {
 @Inject(method="updateForTopItem",at=@At("RETURN"),require=1)
 private void monkeyclient$item(ItemStackRenderState state,ItemStack stack,ItemDisplayContext context,Level level,ItemOwner owner,int seed,CallbackInfo ci){
  ItemEditor.Appearance appearance=null;
  if(MonkeyClient.ready()){var m=MonkeyClient.modules().get(ItemEditor.class);if(m.isEnabled())appearance=m.appearance(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(),context);}
  ((ItemAppearanceState)state).monkeyclient$appearance(appearance);
  if(appearance!=null){state.appendModelIdentityElement(appearance.identity());state.setAnimated();state.setOversizedInGui(true);}
 }
}
