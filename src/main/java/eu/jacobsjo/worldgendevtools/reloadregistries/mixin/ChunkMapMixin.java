package eu.jacobsjo.worldgendevtools.reloadregistries.mixin;

import eu.jacobsjo.worldgendevtools.reloadregistries.api.UpdatableGeneratorChunkMap;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.status.WorldGenContext;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

/**
 * This mixin allows to set a new {@link ChunkGenerator} after reloading the registries,
 */
@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin implements UpdatableGeneratorChunkMap {

    @Mutable @Shadow @Final private ChunkGeneratorStructureState chunkGeneratorState;

    @Mutable @Shadow @Final private RandomState randomState;

    @Shadow @Final ServerLevel level;

    @Mutable @Shadow @Final private WorldGenContext worldGenContext;

    @Override
    public void worldgenDevtools$setGenerator(ChunkGenerator generator) {
        RegistryAccess registryAccess = this.level.registryAccess();

        long seed = this.level.getSeed();
        if (generator instanceof NoiseBasedChunkGenerator noiseBasedChunkGenerator) {
            this.randomState = RandomState.create((noiseBasedChunkGenerator.generatorSettings().value()), registryAccess.lookupOrThrow(Registries.NOISE), seed);
        } else {
            this.randomState = RandomState.create(NoiseGeneratorSettings.dummy(), registryAccess.lookupOrThrow(Registries.NOISE), seed);
        }
        this.chunkGeneratorState = generator.createState(registryAccess.lookupOrThrow(Registries.STRUCTURE_SET), this.randomState, seed);
        this.worldGenContext = new WorldGenContext(this.worldGenContext.level(), generator, this.worldGenContext.structureManager(), this.worldGenContext.lightEngine(), this.worldGenContext.mainThreadExecutor(), this.worldGenContext.unsavedListener());
    }
}
