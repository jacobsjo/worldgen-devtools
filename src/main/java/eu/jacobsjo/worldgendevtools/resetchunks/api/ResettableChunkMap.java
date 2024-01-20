package eu.jacobsjo.worldgendevtools.resetchunks.api;

import net.minecraft.world.level.ChunkPos;

public interface ResettableChunkMap {
    boolean worldgenDevtools$resetChunk(ChunkPos chunkPos);
}
