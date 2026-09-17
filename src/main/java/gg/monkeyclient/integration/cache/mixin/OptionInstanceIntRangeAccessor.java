package gg.monkeyclient.integration.cache.mixin;

import net.minecraft.client.OptionInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(OptionInstance.IntRange.class)
public interface OptionInstanceIntRangeAccessor {
    @Accessor
    @Mutable
    void setMaxInclusive(int value);
}
