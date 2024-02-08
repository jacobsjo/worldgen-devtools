package eu.jacobsjo.worldgendevtools.resetchunks.mixin;


import com.mojang.datafixers.DataFixer;
import eu.jacobsjo.worldgendevtools.resetchunks.api.ResettableChunkMap;
import eu.jacobsjo.worldgendevtools.worldgensettings.WorldgenSettingsInit;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.server.level.*;
import net.minecraft.util.SortedArraySet;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.file.Path;
import java.util.stream.Collectors;

@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin extends ChunkStorage implements ResettableChunkMap {
    public ChunkMapMixin(RegionStorageInfo regionStorageInfo, Path path, DataFixer dataFixer, boolean bl) {
        super(regionStorageInfo, path, dataFixer, bl);
    }
    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final ServerLevel level;

    @Shadow public abstract DistanceManager getDistanceManager();

    @Shadow @Final private Long2ObjectLinkedOpenHashMap<ChunkHolder> updatingChunkMap;

    @Inject(method="save", at=@At("HEAD"), cancellable = true)
    private void save(ChunkAccess chunk, CallbackInfoReturnable<Boolean> cir){
        if (!this.level.getGameRules().getBoolean(WorldgenSettingsInit.SAVE_CHUNKS)){
            cir.cancel();
        }
    }

    /**
     * Unloads the chunk at the given position and deletes the saved chunk from disk. Then readds any player tickets to allow reloading.
     * @param chunkPos the chunk to reset
     * @return whether the unloading was successfull
     */
    @Override
    public boolean worldgenDevtools$resetChunk(ChunkPos chunkPos) {
        DistanceManager distanceManager = this.getDistanceManager();

        try {
            LOGGER.debug("Removing chunk {} with tickets {}", chunkPos, this.getTicketDebugString(chunkPos.toLong()));

            // this forces the chunk to be unloaded, without regard to its saving status. But that doesn't matter, since we are resetting it anyways.
            this.updatingChunkMap.remove(chunkPos.toLong());
            distanceManager.tickets.remove(chunkPos.toLong()); // deletes tickets from unloaded chunks, to make sure they are reloaded before usage.

            // this removes the chunks from disk, so they are regenerated when reloading the chunk
            this.write(chunkPos, null);

            // this makes the playerTicketManager readd the player tickets we just deleted, so the chunk gets reloaded and sent to the client.
            int level = distanceManager.playerTicketManager.getLevel(chunkPos.toLong());
            distanceManager.playerTicketManager.onLevelChange(chunkPos.toLong(), level, false, distanceManager.playerTicketManager.haveTicketFor(level));
            // other tickets aren't readded, so some chunks (like in spawn-chunks) aren't reloaded after a reset. I don't think this matters though.

        } catch (Exception e) {
            LOGGER.error("Failed to reset chunk {}", chunkPos, e);
            return false;
        }

        return true;
    }

    @Unique
    private String getTicketDebugString(long chunkPos){
        SortedArraySet<Ticket<?>> tickets = this.getDistanceManager().tickets.get(chunkPos);
        if (tickets == null){
            return "[ ]";
        }
        return "[ " + tickets.stream().map(Ticket::toString).collect(Collectors.joining(" | ")) + " ]";
    }
}
