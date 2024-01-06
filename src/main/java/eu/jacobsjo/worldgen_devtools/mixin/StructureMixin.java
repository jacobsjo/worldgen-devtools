package eu.jacobsjo.worldgen_devtools.mixin;

import eu.jacobsjo.worldgen_devtools.api.HolderStructureStart;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.ChunkPos;
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
    @Inject(method = "generate", at = @At("RETURN"))
    public void generate(RegistryAccess registryAccess, ChunkGenerator chunkGenerator, BiomeSource biomeSource, RandomState randomState, StructureTemplateManager structureTemplateManager, long l, ChunkPos chunkPos, int i, LevelHeightAccessor levelHeightAccessor, Predicate<Holder<Biome>> predicate, CallbackInfoReturnable<StructureStart> cir) {
        Registry<Structure> registry = registryAccess.registry(Registries.STRUCTURE).orElseThrow();
        ((HolderStructureStart) (Object) cir.getReturnValue()).worldgenDevtools$setHolder(registry.getHolderOrThrow(registry.getResourceKey((Structure) (Object) this).orElseThrow()));
    }
}
