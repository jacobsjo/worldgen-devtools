package eu.jacobsjo.worldgendevtools.locatefeature.impl;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

import java.util.*;

public class FeaturePositions {
    public static final Codec<FeaturePositions> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.list(PositionsOfFeature.CODEC).fieldOf("positions").forGetter(FeaturePositions::getList)
            ).apply(instance, FeaturePositions::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, FeaturePositions> STREAM_CODEC = StreamCodec.composite(
            PositionsOfFeature.STREAM_CODEC.apply(ByteBufCodecs.list()), FeaturePositions::getList,
            FeaturePositions::new
    );

    public FeaturePositions(){
        this.featurePositions = new HashMap<>();
    }

    private FeaturePositions(List<PositionsOfFeature> positions){
        this.featurePositions = new HashMap<>();
        positions.forEach(pos -> featurePositions.put(pos.feature, new ArrayList<>(pos.positions)));
    }

    private final Map<ResourceKey<ConfiguredFeature<?,?>>, List<PosAndCount>> featurePositions;

    private List<PositionsOfFeature> getList(){
        return featurePositions.entrySet().stream().map(entry -> new PositionsOfFeature(entry.getKey(), entry.getValue())).toList();
    }

    public void addPosiition(ResourceKey<ConfiguredFeature<?, ?>> feature, BlockPos pos) {
        List<PosAndCount> positions = this.featurePositions.computeIfAbsent(feature, f -> new ArrayList<>());
        Optional<PosAndCount> posAndCount = positions.stream().filter(p -> p.pos().equals(pos)).findAny();
        if (posAndCount.isPresent()){
            posAndCount.get().increaseCount();
        } else {
            positions.add(new PosAndCount(pos, 1));
        }
    }

    public List<PosAndCount> getPositions(ResourceKey<ConfiguredFeature<?, ?>> feature){
        return this.featurePositions.getOrDefault(feature, List.of());
    }

    public Set<ResourceKey<ConfiguredFeature<?, ?>>> getFeatureTypes(){
        return this.featurePositions.keySet();
    }


    public static final class PosAndCount {

        private static final Codec<Either<PosAndCount, BlockPos>> EITHER_CODEC = Codec.either(RecordCodecBuilder.create(
                instance -> instance.group(
                        BlockPos.CODEC.fieldOf("pos").forGetter(i -> i.pos),
                        ExtraCodecs.POSITIVE_INT.fieldOf("count").forGetter(i -> i.count)
                ).apply(instance, PosAndCount::new)
        ), BlockPos.CODEC);

        private static final Codec<PosAndCount> CODEC = EITHER_CODEC.xmap(
                either -> either.map(o -> o, pos -> new PosAndCount(pos, 1)),
                posAndCount -> posAndCount.count == 1 ? Either.right(posAndCount.pos) : Either.left(posAndCount)
        );

        private static final StreamCodec<RegistryFriendlyByteBuf, PosAndCount> STREAM_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, PosAndCount::pos,
                ByteBufCodecs.INT, PosAndCount::count,
                PosAndCount::new
        );

        private final BlockPos pos;
        private int count;

        public PosAndCount(BlockPos pos, int count) {
            this.pos = pos;
            this.count = count;
        }

        public BlockPos pos() {
            return pos;
        }

        public int count() {
            return count;
        }

        public void increaseCount() {
            count++;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (PosAndCount) obj;
            return Objects.equals(this.pos, that.pos) &&
                    this.count == that.count;
        }

        @Override
        public int hashCode() {
            return Objects.hash(pos, count);
        }

        @Override
        public String toString() {
            return "PosAndCount[" +
                    "pos=" + pos + ", " +
                    "count=" + count + ']';
        }

        }

    private record PositionsOfFeature(ResourceKey<ConfiguredFeature<?,?>> feature, List<PosAndCount> positions){
        private static final Codec<PositionsOfFeature> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        ResourceKey.codec(Registries.CONFIGURED_FEATURE).fieldOf("feature").forGetter(i -> i.feature),
                        Codec.list(PosAndCount.CODEC).fieldOf("positions").forGetter(i -> i.positions)
                ).apply(instance, PositionsOfFeature::new)
        );

        private static final StreamCodec<RegistryFriendlyByteBuf, PositionsOfFeature> STREAM_CODEC = StreamCodec.composite(
                ResourceKey.streamCodec(Registries.CONFIGURED_FEATURE), PositionsOfFeature::feature,
                PosAndCount.STREAM_CODEC.apply(ByteBufCodecs.list()), PositionsOfFeature::positions,
                PositionsOfFeature::new
        );
    }
}
