package eu.jacobsjo.worldgen_devtools.mixin;

import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

/**
 * A small fix for a crash that happens when reloading during world generation.
 */
@Mixin(ChunkGenerator.class)
public class ChunkGeneratorMixin {

    /**
     * @author jacobsjo
     * @reason overwriting simple lambda
     */
    @Overwrite
    private static void method_39788(IntSet intSet, FeatureSorter.StepFeatureData stepFeatureData, PlacedFeature placedFeature){
        int id = stepFeatureData.indexMapping().applyAsInt((PlacedFeature)placedFeature);
        /*
         if id is -1 then the placedFeature wasn't found in the indexMapping. This happens when biomeDecoration happends
         during reload but the featureSorter hasn't rerun yet. This can cause some chunks with missinge features.
         */
        if (id >= 0)
            intSet.add(id);
    }
}
