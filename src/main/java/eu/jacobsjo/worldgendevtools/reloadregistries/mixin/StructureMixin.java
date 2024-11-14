package eu.jacobsjo.worldgendevtools.reloadregistries.mixin;

import eu.jacobsjo.worldgendevtools.reloadregistries.api.HolderStructureStart;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(Structure.class)
public abstract class StructureMixin {

    /**
     * Calls {@link HolderStructureStart#worldgenDevtools$setHolder} after creation of a {@link StructureStart}. See {@link StructureStartMixin}.
     */
    @SuppressWarnings("DataFlowIssue")
    @Inject(method = "generate", at = @At("RETURN"), cancellable = true)
    public void generate(Holder<Structure> holder, ResourceKey<Level> resourceKey, RegistryAccess registryAccess, ChunkGenerator chunkGenerator, BiomeSource biomeSource, RandomState randomState, StructureTemplateManager structureTemplateManager, long l, ChunkPos chunkPos, int i, LevelHeightAccessor levelHeightAccessor, Predicate<Holder<Biome>> predicate, CallbackInfoReturnable<StructureStart> cir) {
        try {
            Registry<Structure> registry = registryAccess.lookupOrThrow(Registries.STRUCTURE);
            ((HolderStructureStart) (Object) cir.getReturnValue()).worldgenDevtools$setHolder(registry.get(registry.getResourceKey((Structure) (Object) this).orElseThrow()).orElseThrow());
        } catch(Exception e)  {
            cir.setReturnValue(StructureStart.INVALID_START);
        }
    }
}
