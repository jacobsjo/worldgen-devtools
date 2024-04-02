package eu.jacobsjo.worldgendevtools.profiling.mixin;

import eu.jacobsjo.worldgendevtools.profiling.ProfilingInit;
import eu.jacobsjo.worldgendevtools.profiling.impl.ChunkgenProfilingInformation;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.jfr.callback.ProfiledDuration;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ToFullChunk;
import net.minecraft.world.level.chunk.status.WorldGenContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(ChunkStatus.class)
public class ChunkStatusMixin {

    @Unique
    private static final AttachmentType<Instant> START_INSTANT_ATTACHMENT = AttachmentRegistry.create (new ResourceLocation("worldgendevtools", "profiling"));


    @Inject(
            method = "generate",
            at = @At("HEAD")
    )
    public void generationStart(WorldGenContext worldGenContext, Executor executor, ToFullChunk toFullChunk, List<ChunkAccess> cache, CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir){
        cache.get(cache.size() / 2).setAttached(START_INSTANT_ATTACHMENT, Instant.now());
    }

    @Inject(
            method = "method_57593",
            at = @At("RETURN")
    )
    public void generationEnd(ProfiledDuration profiledDuration, ChunkAccess chunkAccess, CallbackInfoReturnable<ChunkAccess> cir){
        ChunkgenProfilingInformation profilingInformation = chunkAccess.getAttachedOrCreate(ProfilingInit.PROFILING_ATTACHMENT);
        Instant start = Objects.requireNonNull(chunkAccess.getAttached(START_INSTANT_ATTACHMENT));
        profilingInformation.setStatusDurection((ChunkStatus)(Object) this, Duration.between(start, Instant.now()));
    }
}
