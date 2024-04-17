package eu.jacobsjo.worldgendevtools.reloadregistries.mixin;


import eu.jacobsjo.worldgendevtools.reloadregistries.impl.FrozenHolder;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(Level.class)
public class LevelMixin {

    @Shadow public Holder<DimensionType> dimensionTypeRegistration;

    // freeze dimensionType holder, so it doesn't get automatically updated, causing potential crashes when changing world height. DimensionType gets reset manually by resetter.
    @Inject(method = "<init>", at=@At("TAIL"))
    public void freezeHolder(WritableLevelData levelData, ResourceKey<Level> dimension, RegistryAccess registryAccess, Holder<DimensionType> dimensionTypeRegistration, Supplier<ProfilerFiller> profiler, boolean isClientSide, boolean isDebug, long biomeZoomSeed, int maxChainedNeighborUpdates, CallbackInfo ci){
        this.dimensionTypeRegistration = new FrozenHolder<>(dimensionTypeRegistration);
    }
}
