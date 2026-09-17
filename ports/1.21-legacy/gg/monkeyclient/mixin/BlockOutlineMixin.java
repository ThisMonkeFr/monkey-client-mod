package gg.monkeyclient.mixin;
import gg.monkeyclient.MonkeyClient;
import gg.monkeyclient.modules.BlockOutlines;
import gg.monkeyclient.render.OutlineContext;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.state.LevelRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(LevelRenderer.class)
public class BlockOutlineMixin {
 @WrapMethod(method="renderBlockOutline")
 private void monkey$outline(MultiBufferSource.BufferSource buffers,PoseStack pose,boolean translucent,LevelRenderState state,Operation<Void> original){if(MonkeyClient.ready()){var m=MonkeyClient.modules().get(BlockOutlines.class);if(m.isEnabled()&&!m.visible.get())return;}boolean old=OutlineContext.active.get();OutlineContext.active.set(true);try{original.call(buffers,pose,translucent,state);}finally{OutlineContext.active.set(old);}}
 @ModifyVariable(method="renderHitOutline",at=@At("HEAD"),argsOnly=true,ordinal=0,require=1)
 private int monkey$color(int original){if(MonkeyClient.ready()){var m=MonkeyClient.modules().get(BlockOutlines.class);if(m.isEnabled())return m.color();}return original;}
}
