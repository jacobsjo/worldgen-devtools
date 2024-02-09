package eu.jacobsjo.worldgendevtools.locatefeature.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeaturePositions {
    public static final Codec<FeaturePositions> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.list(PositionsOfFeature.CODEC).fieldOf("positions").forGetter(FeaturePositions::getList)
            ).apply(instance, FeaturePositions::new)
    );

    public FeaturePositions(){
        this.featurePositions = new HashMap<>();
    }

    private FeaturePositions(List<PositionsOfFeature> positions){
        this.featurePositions = new HashMap<>();
        positions.forEach(pos -> featurePositions.put(pos.feature, new ArrayList<>(pos.positions)));
    }

    private final Map<ResourceKey<ConfiguredFeature<?,?>>, List<BlockPos>> featurePositions;

    private List<PositionsOfFeature> getList(){
        return featurePositions.entrySet().stream().map(entry -> new PositionsOfFeature(entry.getKey(), entry.getValue())).toList();
    }

    public List<BlockPos> getPositions(ResourceKey<ConfiguredFeature<?, ?>> feature){
        return this.featurePositions.computeIfAbsent(feature, f -> new ArrayList<>());
    }


    private record PositionsOfFeature(ResourceKey<ConfiguredFeature<?,?>> feature, List<BlockPos> positions){
        private static final Codec<PositionsOfFeature> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        ResourceKey.codec(Registries.CONFIGURED_FEATURE).fieldOf("feature").forGetter(i -> i.feature),
                        Codec.list(BlockPos.CODEC).fieldOf("positions").forGetter(i -> i.positions)
                ).apply(instance, PositionsOfFeature::new)
        );
    }
}
