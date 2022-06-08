package eu.jacobsjo.dfcommand;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class RandomState {
    final PositionalRandomFactory random;
    private final Registry<NormalNoise.NoiseParameters> noises;
    private final Map<ResourceKey<NormalNoise.NoiseParameters>, NormalNoise> noiseIntances;

    private final DensityFunction.Visitor visitor;

    public static RandomState create(RegistryAccess registryAccess, ResourceKey<NoiseGeneratorSettings> resourceKey, long l) {
        return create((NoiseGeneratorSettings)registryAccess.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY).getOrThrow(resourceKey), registryAccess.registryOrThrow(Registry.NOISE_REGISTRY), l);
    }

    public static RandomState create(NoiseGeneratorSettings noiseGeneratorSettings, Registry<NormalNoise.NoiseParameters> registry, long l) {
        return new RandomState(noiseGeneratorSettings, registry, l);
    }

    private RandomState(NoiseGeneratorSettings noiseGeneratorSettings, Registry<NormalNoise.NoiseParameters> registry, final long l) {
        this.random = noiseGeneratorSettings.getRandomSource().newInstance(l).forkPositional();
        this.noises = registry;
        this.noiseIntances = new ConcurrentHashMap();
        final boolean bl = noiseGeneratorSettings.useLegacyRandomSource();

        class NoiseWiringHelper implements DensityFunction.Visitor {
            private final Map<DensityFunction, DensityFunction> wrapped = new HashMap();

            NoiseWiringHelper() {
            }

            private RandomSource newLegacyInstance(long lx) {
                return new LegacyRandomSource(l + lx);
            }

            public DensityFunction.NoiseHolder visitNoise(DensityFunction.NoiseHolder noiseHolder) {
                Holder<NormalNoise.NoiseParameters> holder = noiseHolder.noiseData();
                NormalNoise normalNoise;
                if (bl) {
                    if (Objects.equals(holder.unwrapKey(), Optional.of(Noises.TEMPERATURE))) {
                        normalNoise = NormalNoise.createLegacyNetherBiome(this.newLegacyInstance(0L), new NormalNoise.NoiseParameters(-7, 1.0, 1.0));
                        return new DensityFunction.NoiseHolder(holder, normalNoise);
                    }

                    if (Objects.equals(holder.unwrapKey(), Optional.of(Noises.VEGETATION))) {
                        normalNoise = NormalNoise.createLegacyNetherBiome(this.newLegacyInstance(1L), new NormalNoise.NoiseParameters(-7, 1.0, 1.0));
                        return new DensityFunction.NoiseHolder(holder, normalNoise);
                    }

                    if (Objects.equals(holder.unwrapKey(), Optional.of(Noises.SHIFT))) {
                        normalNoise = NormalNoise.create(RandomState.this.random.fromHashOf(Noises.SHIFT.location()), new NormalNoise.NoiseParameters(0, 0.0));
                        return new DensityFunction.NoiseHolder(holder, normalNoise);
                    }
                }

                normalNoise = RandomState.this.getOrCreateNoise(holder.unwrapKey().orElseThrow());
                return new DensityFunction.NoiseHolder(holder, normalNoise);
            }

            private DensityFunction wrapNew(DensityFunction densityFunction) {
                if (densityFunction instanceof BlendedNoise blendedNoise) {
                    RandomSource randomSource = bl ? this.newLegacyInstance(0L) : RandomState.this.random.fromHashOf(new ResourceLocation("terrain"));
                    return blendedNoise.withNewRandom(randomSource);
                } else {
                    return (InstanceOfHelper.isInstanceOfEndIslandDensityFunction(densityFunction) ? DensityFunctions.endIslands(l) : densityFunction);
                }
            }

            public DensityFunction apply(@NotNull DensityFunction densityFunction) {
                return this.wrapped.computeIfAbsent(densityFunction, this::wrapNew);
            }
        }

         this.visitor = new NoiseWiringHelper();
    }

    public NormalNoise getOrCreateNoise(ResourceKey<NormalNoise.NoiseParameters> resourceKey) {
        return (NormalNoise)this.noiseIntances.computeIfAbsent(resourceKey, (resourceKey2) -> {
            return Noises.instantiate(this.noises, this.random, resourceKey);
        });
    }

    public DensityFunction.Visitor getVisitor(){
        return this.visitor;
    }
}
