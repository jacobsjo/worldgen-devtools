package eu.jacobsjo.worldgendevtools.externalprofiling.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import eu.jacobsjo.worldgendevtools.externalprofiling.api.FeatureGenerationEvent;
import me.modmuss50.tracyutils.NameableZone;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.TracyZoneFiller;
import net.minecraft.util.profiling.Zone;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChunkGenerator.class)
public class ChunkGeneratorMixin {
    @WrapOperation(
            method = "applyBiomeDecoration",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/placement/PlacedFeature;placeWithBiomeCheck(Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;)Z")
    )
    private boolean profilePlaceWithBiomeCheck(PlacedFeature placedFeature, WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos pos, Operation<Boolean> original, @Local(argsOnly = true) ChunkAccess chunk, @Local(ordinal = 1) Registry<PlacedFeature> placedFeatureRegistry, @Local(ordinal = 2) int step){
        String featureKey = placedFeatureRegistry.getResourceKey(placedFeature).map(k -> k.location().toString()).orElse("unregistered");
        FeatureGenerationEvent event = new FeatureGenerationEvent(chunk.getPos(), level.getLevel().dimension(), featureKey, step);
        boolean result;
        event.begin();
        try (Zone zone = Profiler.get().zone("placedFeature")) {
            if (zone.profiler instanceof TracyZoneFiller) {
                ((NameableZone) ((TracyZoneFiller) zone.profiler).activeZone()).tracy_utils$setName(featureKey);
            }
            result = original.call(placedFeature, level, generator, random, pos);
        }

        event.commit();
        return result;
    }

    @WrapMethod(
            method = "tryGenerateStructure"
    )
    private boolean tryGenerateStructure(StructureSet.StructureSelectionEntry structureSelectionEntry, StructureManager structureManager, RegistryAccess registryAccess, RandomState random, StructureTemplateManager structureTemplateManager, long seed, ChunkAccess chunk, ChunkPos chunkPos, SectionPos sectionPos, ResourceKey<Level> resourceKey, Operation<Boolean> original){
        try (Zone zone = Profiler.get().zone("structure")) {
            if (zone.profiler instanceof TracyZoneFiller) {
                ((NameableZone) ((TracyZoneFiller) zone.profiler).activeZone()).tracy_utils$setName(structureSelectionEntry.structure().getRegisteredName());
            }
            zone.addText("chunkPos: " + chunkPos);
            boolean result = original.call(structureSelectionEntry, structureManager, registryAccess, random, structureTemplateManager, seed, chunk, chunkPos, sectionPos, resourceKey);
            if (!result) {
                zone.addText("not placed");
            }
            return result;
        }
    }
}
