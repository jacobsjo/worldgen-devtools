package eu.jacobsjo.worldgendevtools.jfrprofiling.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import eu.jacobsjo.worldgendevtools.jfrprofiling.api.FeatureGenerationEvent;
import net.minecraft.core.Registry;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkGenerator.class)
public class ChunkGeneratorMixin {
    @Inject(
            method="applyBiomeDecoration",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/placement/PlacedFeature;placeWithBiomeCheck(Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;)Z")
    )
    private void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager, CallbackInfo ci, @Local(ordinal = 1) Registry<PlacedFeature> placedFeatureRegistry, @Local PlacedFeature placedFeature, @Local(ordinal = 2) int step, @Share("event") LocalRef<FeatureGenerationEvent> eventRef) {
        String featureKey = placedFeatureRegistry.getResourceKey(placedFeature).map(k -> k.location().toString()).orElse("unregistered");
        FeatureGenerationEvent event = new FeatureGenerationEvent(chunk.getPos(), level.getLevel().dimension(), featureKey, step);
        event.begin();
        eventRef.set(event);
    }

    @Inject(
            method="applyBiomeDecoration",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/placement/PlacedFeature;placeWithBiomeCheck(Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;)Z", shift = At.Shift.AFTER)
    )
    private void applyBiomeDecorationCommit(WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager, CallbackInfo ci, @Share("event") LocalRef<FeatureGenerationEvent> eventRef) {
        FeatureGenerationEvent event = eventRef.get();
        if (event != null) {
            event.commit();
        }
    }
}
