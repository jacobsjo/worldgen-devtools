package eu.jacobsjo.worldgendevtools.externalprofiling.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import me.modmuss50.tracyutils.NameableZone;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.util.StaticCache2D;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.TracyZoneFiller;
import net.minecraft.util.profiling.Zone;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkStep;
import net.minecraft.world.level.chunk.status.WorldGenContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.concurrent.CompletableFuture;

@Mixin(ChunkStep.class)
public class ChunkStepMixin {

    @Shadow @Final ChunkStatus targetStatus;

    @WrapMethod(method = "apply")
    public CompletableFuture<ChunkAccess> apply(WorldGenContext worldGenContext, StaticCache2D<GenerationChunkHolder> staticCache2D, ChunkAccess chunkAccess, Operation<CompletableFuture<ChunkAccess>> original){
        try (Zone zone = Profiler.get().zone("chunkStep")) {
            if (zone.profiler instanceof TracyZoneFiller) {
                ((NameableZone) ((TracyZoneFiller) zone.profiler).activeZone()).tracy_utils$setName(this.targetStatus.getName());
            }
            zone.addText("chunkPos: " + chunkAccess.getPos());
            return original.call(worldGenContext, staticCache2D, chunkAccess);
        }
    }
}
