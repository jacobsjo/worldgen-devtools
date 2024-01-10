package eu.jacobsjo.worldgendevtools.reloadregistries.api;

import net.minecraft.world.level.chunk.ChunkGenerator;

public interface UpdatableGeneratorChunkMap {
    void worldgenDevtools$setGenerator(ChunkGenerator generator);
}
