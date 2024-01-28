package eu.jacobsjo.worldgendevtools.locatefeature.mixin;

import eu.jacobsjo.worldgendevtools.locatefeature.LocateFeatureInit;
import eu.jacobsjo.worldgendevtools.locatefeature.impl.FeaturePositions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Mixin(PlacedFeature.class)
public class PlacedFeatureMixin {

    @Shadow @Final private Holder<ConfiguredFeature<?, ?>> feature;

    @Shadow @Final private List<PlacementModifier> placement;

    /**
     * @author jacobsjo
     * @reason couldn't find a better way, the lambda is static
     */
    @Overwrite
    private boolean placeWithContext(PlacementContext context, RandomSource source, BlockPos pos){
        Stream<BlockPos> stream = Stream.of(pos);

        for(PlacementModifier placementModifier : this.placement) {
            stream = stream.flatMap(blockPos -> placementModifier.getPositions(context, source, blockPos));
        }

        Optional<ResourceKey<ConfiguredFeature<?, ?>>> key = this.feature.unwrapKey();
        ConfiguredFeature<?, ?> configuredFeature = this.feature.value();

        MutableBoolean mutableBoolean = new MutableBoolean();
        stream.forEach(blockPos -> {
            if (configuredFeature.place(context.getLevel(), context.generator(), source, blockPos)) {
                mutableBoolean.setTrue();

                if (key.isPresent()){
                    FeaturePositions positions = context.getLevel().getChunk(blockPos).getAttachedOrCreate(LocateFeatureInit.FEATURE_POSITION_ATTACHMENT);
                    positions.getPositions(key.get()).add(blockPos);
                }
            }
        });
        return mutableBoolean.isTrue();
    }
}
