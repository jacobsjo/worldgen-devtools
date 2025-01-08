package eu.jacobsjo.worldgendevtools.resetchunks.mixin;

import eu.jacobsjo.worldgendevtools.resetchunks.api.DeactivateableTicketStorage;
import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.Ticket;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.TicketStorage;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Iterator;
import java.util.List;

@Mixin(TicketStorage.class)
public abstract class TicketStorageMixin implements DeactivateableTicketStorage {
    @Shadow @Final private Long2ObjectOpenHashMap<List<Ticket>> deactivatedTickets;
    @Shadow @Final private Long2ObjectOpenHashMap<List<Ticket>> tickets;
    @Shadow @Nullable private TicketStorage.@Nullable ChunkUpdated loadingChunkUpdatedListener;
    @Shadow @Nullable private TicketStorage.@Nullable ChunkUpdated simulationChunkUpdatedListener;
    @Shadow protected abstract void updateForcedChunks();

    @Shadow public abstract int getTicketLevelAt(long l, boolean bl);

    @Override
    public void worldgenDevtools$deactiveChunk(long pos) {
        List<Ticket> chunkTickets = this.tickets.get(pos);
        Iterator<Ticket> iterator = chunkTickets.iterator();
        boolean forced = false;
        boolean simulating = false;
        boolean loading = false;

        while (iterator.hasNext()) {
            Ticket ticket = (Ticket)iterator.next();
            List<Ticket> list = this.deactivatedTickets.computeIfAbsent(
                    pos, (Long2ObjectFunction<? extends List<Ticket>>)(l -> new ObjectArrayList<>(chunkTickets.size()))
            );
            list.add(ticket);

            iterator.remove();
            if (ticket.getType().doesLoad()) {
                loading = true;
            }

            if (ticket.getType().doesSimulate()) {
                simulating = true;
            }

            if (ticket.getType().equals(TicketType.FORCED)) {
                forced = true;
            }
        }

        if (loading || simulating) {
            if (loading && this.loadingChunkUpdatedListener != null) {
                this.loadingChunkUpdatedListener.update(pos, getTicketLevelAt(pos, false), false);
            }

            if (simulating && this.simulationChunkUpdatedListener != null) {
                this.simulationChunkUpdatedListener.update(pos, getTicketLevelAt(pos, true), false);
            }

            ((TicketStorage)(Object)this).setDirty();
        }

        if (forced) {
            this.updateForcedChunks();
        }
    }
}
