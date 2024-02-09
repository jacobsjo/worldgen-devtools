package eu.jacobsjo.worldgendevtools.profiling.mixin;

import com.mojang.datafixers.util.Either;
import eu.jacobsjo.worldgendevtools.profiling.ProfilingInit;
import eu.jacobsjo.worldgendevtools.profiling.impl.ChunkgenProfilingInformation;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

@Mixin(ChunkStatus.class)
public class ChunkStatusMixin {

    @Unique
    private static final AttachmentType<Instant> START_INSTANT_ATTACHMENT = AttachmentRegistry.create (new ResourceLocation("worldgendevtools", "profiling"));


    @Inject(
            method = "generate",
            at = @At("HEAD")
    )
    public void generationStart(Executor exectutor, ServerLevel level, ChunkGenerator chunkGenerator, StructureTemplateManager structureTemplateManager, ThreadedLevelLightEngine lightEngine, Function<ChunkAccess, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> task, List<ChunkAccess> cache, CallbackInfoReturnable<CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> cir){
        cache.get(cache.size() / 2).setAttached(START_INSTANT_ATTACHMENT, Instant.now());
    }

    @Inject(
            method = "method_52270",
            at = @At("RETURN")
    )
    public void generationEnd(ChunkAccess chunkAccess, CallbackInfo ci){
        ChunkgenProfilingInformation profilingInformation = chunkAccess.getAttachedOrCreate(ProfilingInit.PROFILING_ATTACHMENT);
        Instant start = Objects.requireNonNull(chunkAccess.getAttached(START_INSTANT_ATTACHMENT));
        profilingInformation.setStatusDurection((ChunkStatus)(Object) this, Duration.between(start, Instant.now()));
    }
}
