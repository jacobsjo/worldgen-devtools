package eu.jacobsjo.worldgendevtools.locatefeature.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import eu.jacobsjo.worldgendevtools.locatefeature.LocateFeatureInit;
import eu.jacobsjo.worldgendevtools.locatefeature.impl.FeaturePositions;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.FeaturePlacer;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
@Mixin(FeaturePlacer.class)
public class FeaturePlacerMixin {


    @WrapOperation(
            method = "place(Lnet/minecraft/world/level/levelgen/placement/PlacedFeature;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;Z)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/levelgen/feature/Feature;place(Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;)Z"
            )
    )
    private boolean place(
            Feature feature,
            WorldGenLevel worldGenLevel,
            ChunkGenerator chunkGenerator,
            RandomSource randomSource,
            BlockPos blockPos,
            Operation<Boolean> original,
            @Local(name = "placedFeature") PlacedFeature placedFeature
    ){
        Optional<ResourceKey<Feature>> key = placedFeature.feature().unwrapKey();

        boolean placed = original.call(feature, worldGenLevel, chunkGenerator, randomSource, blockPos);
        if (placed && key.isPresent()) {
            FeaturePositions featurePositionAttachment = worldGenLevel.getChunk(blockPos).getAttachedOrCreate(LocateFeatureInit.FEATURE_POSITION_ATTACHMENT);
            featurePositionAttachment.addPosiition(key.get(), blockPos);
        }
        return placed;
    }

    /*
    private boolean place(final WorldGenLevel level, final ChunkGenerator generator, final RandomSource random, final BlockPos origin){
        Stream<BlockPos> stream = Stream.of(origin);

        for(PlacementModifier placementModifier : this.placement) {
            stream = stream.flatMap(blockPos -> placementModifier.getPositions(context, source, blockPos));
        }

        Optional<ResourceKey<ConfiguredFeature<?, ?>>> key = this.feature.unwrapKey();
        ConfiguredFeature<?, ?> configuredFeature = this.feature.value();

        MutableBoolean mutableBoolean = new MutableBoolean();
        stream.forEach(blockPos -> {
            try (Zone zone =  Profiler.get().zone("configuredFeature")) {
                key.ifPresent(configuredFeatureResourceKey -> zone.addText(configuredFeatureResourceKey.identifier().toString()));

                if (configuredFeature.place(context.getLevel(), context.generator(), source, blockPos)) {
                    mutableBoolean.setTrue();

                    if (key.isPresent()) {
                        FeaturePositions positions = context.getLevel().getChunk(blockPos).getAttachedOrCreate(LocateFeatureInit.FEATURE_POSITION_ATTACHMENT);
                        positions.addPosiition(key.get(),blockPos);
                    }
                } else {
                    zone.addText("(not placed)");
                }
            }
        });
        return mutableBoolean.isTrue();
    }*/
}
