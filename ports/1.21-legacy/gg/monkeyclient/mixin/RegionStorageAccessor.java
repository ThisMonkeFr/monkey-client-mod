package gg.monkeyclient.mixin;
import net.minecraft.world.level.chunk.storage.SimpleRegionStorage;
import net.minecraft.world.level.chunk.storage.IOWorker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(SimpleRegionStorage.class)
public interface RegionStorageAccessor { @Accessor("worker") IOWorker monkey$worker(); }
