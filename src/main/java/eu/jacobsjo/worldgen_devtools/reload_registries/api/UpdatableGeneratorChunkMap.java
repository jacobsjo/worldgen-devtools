package eu.jacobsjo.worldgen_devtools.reload_registries.api;

import net.minecraft.world.level.chunk.ChunkGenerator;

public interface UpdatableGeneratorChunkMap {
    void worldgenDevtools$setGenerator(ChunkGenerator generator);
}
