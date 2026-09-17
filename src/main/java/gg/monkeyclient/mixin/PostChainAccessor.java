package gg.monkeyclient.mixin;
import net.minecraft.client.renderer.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import java.util.List;
@Mixin(PostChain.class)
public interface PostChainAccessor { @Accessor("passes") List<PostPass> monkeyclient$passes(); }
