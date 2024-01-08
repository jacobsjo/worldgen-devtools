package eu.jacobsjo.worldgen_devtools.resetchunks.mixin;


import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.commands.ResetChunksCommand;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ResetChunksCommand.class)

public class ResetChunksCommandMixin {

    /**
     * Fixes the /resetChunks command to resend biomes for the reset chunks to clients.
     */
    @Inject(method = "resetChunks", at = @At("RETURN"))
    private static void resetChunks(CommandSourceStack source, int range, boolean skipOldChunks, CallbackInfoReturnable<Integer> cir){
        ServerLevel serverLevel = source.getLevel();
        Vec3 vec3 = source.getPosition();
        ChunkPos chunkPos = new ChunkPos(BlockPos.containing(vec3));
        ServerChunkCache serverChunkCache = serverLevel.getChunkSource();

        List<ChunkAccess> chunks = new ArrayList<>();

        for (int z = chunkPos.z - range; z <= chunkPos.z + range; ++z) {
            for (int x = chunkPos.x - range; x <= chunkPos.x + range; ++x) {
                LevelChunk chunk = serverChunkCache.getChunk(x, z, false);
                if (chunk == null || skipOldChunks && chunk.isOldNoiseGeneration()) continue;
                chunks.add(chunk);
            }
        }

        serverChunkCache.chunkMap.resendBiomesForChunks(chunks);
    }
}
