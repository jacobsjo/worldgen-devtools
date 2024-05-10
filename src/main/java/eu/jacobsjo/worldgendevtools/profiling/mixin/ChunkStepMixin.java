package eu.jacobsjo.worldgendevtools.profiling.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import eu.jacobsjo.worldgendevtools.profiling.ProfilingInit;
import eu.jacobsjo.worldgendevtools.profiling.impl.ChunkgenProfilingInformation;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.util.StaticCache2D;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkStep;
import net.minecraft.world.level.chunk.status.WorldGenContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("UnstableApiUsage")
@Mixin(ChunkStep.class)
public class ChunkStepMixin {
    @Shadow @Final ChunkStatus targetStatus;

    @Inject(
            method = "apply",
            at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/util/profiling/jfr/JvmProfiler;onChunkGenerate(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/resources/ResourceKey;Ljava/lang/String;)Lnet/minecraft/util/profiling/jfr/callback/ProfiledDuration;")
    )
    public void generationStart(WorldGenContext worldGenContext, StaticCache2D<GenerationChunkHolder> staticCache2D, ChunkAccess chunkAccess, CallbackInfoReturnable<CompletableFuture<ChunkAccess>> cir, @Share("startTime") LocalRef<Instant> startTime){
        startTime.set(Instant.now());
    }

    @ModifyReturnValue(
            method = "apply",
            at = @At(value = "RETURN", ordinal = 0)
    )
    public CompletableFuture<ChunkAccess> generationEnd(CompletableFuture<ChunkAccess> original, @Local(argsOnly = true) ChunkAccess chunkAccess, @Share("startTime") LocalRef<Instant> startTime){
        ChunkgenProfilingInformation profilingInformation = chunkAccess.getAttachedOrCreate(ProfilingInit.PROFILING_ATTACHMENT);
        profilingInformation.setStatusDurection(this.targetStatus, Duration.between(startTime.get(), Instant.now()));
        return original;
    }
}
