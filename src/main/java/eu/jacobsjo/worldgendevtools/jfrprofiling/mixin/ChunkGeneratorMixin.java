package eu.jacobsjo.worldgendevtools.jfrprofiling.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import eu.jacobsjo.worldgendevtools.jfrprofiling.api.FeatureGenerationEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChunkGenerator.class)
public class ChunkGeneratorMixin {
    @WrapOperation(
            method = "applyBiomeDecoration",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/placement/PlacedFeature;placeWithBiomeCheck(Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;)Z")
    )
    private boolean profilePlaceWithBiomeCheck(PlacedFeature placedFeature, WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos pos, Operation<Boolean> original, @Local(argsOnly = true) ChunkAccess chunk, @Local(ordinal = 1) Registry<PlacedFeature> placedFeatureRegistry, @Local(ordinal = 2) int step){
        String featureKey = placedFeatureRegistry.getResourceKey(placedFeature).map(k -> k.location().toString()).orElse("unregistered");
        FeatureGenerationEvent event = new FeatureGenerationEvent(chunk.getPos(), level.getLevel().dimension(), featureKey, step);
        event.begin();
        boolean result = original.call(placedFeature, level, generator, random, pos);
        event.commit();
        return result;
    }
}
