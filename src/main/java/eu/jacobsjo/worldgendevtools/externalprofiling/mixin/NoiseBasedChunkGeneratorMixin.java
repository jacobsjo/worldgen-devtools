package eu.jacobsjo.worldgendevtools.externalprofiling.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.Zone;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.CarverOutput;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(NoiseBasedChunkGenerator.class)
public class NoiseBasedChunkGeneratorMixin {
    @WrapOperation(method = "applyCarvers", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/carver/WorldCarver;carve(Lnet/minecraft/world/level/levelgen/WorldGenerationContext;Lnet/minecraft/util/RandomSource;Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/chunk/CarverOutput;)Z"))
    boolean carve(
            WorldCarver carver,
            WorldGenerationContext worldGenerationContext,
            RandomSource randomSource,
            ChunkPos chunkPos,
            ChunkPos sourcePos,
            CarverOutput carverOutput,
            Operation<Boolean> original,
            @Local(name = "carverHolder") Holder<WorldCarver> carverHolder
    ){
        try (Zone zone = Profiler.get().zone("carver")) {
            zone.addText(carverHolder.getRegisteredName());
            return original.call(carver, worldGenerationContext, randomSource, chunkPos, sourcePos, carverOutput);
        }
    }
}
