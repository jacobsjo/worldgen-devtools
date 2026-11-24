package eu.jacobsjo.worldgendevtools.externalprofiling.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.Zone;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(StructureSet.class)
public class StructureSetMixin {
	
	@WrapMethod(
		method = "tryGenerateStructure"
	)
	private static @Nullable StructureStart tryGenerateStructure(Holder<Structure> structure, ChunkGenerator generator, HolderGetter.Provider registryAccess, RandomState randomState, StructureTemplateManager structureTemplateManager, long seed, ChunkPos chunkPos, LevelHeightAccessor heightAccessor, ResourceKey<Level> dimension, Climate.Sampler climateSampler, Operation<@Nullable StructureStart> original) {
		try (Zone zone = Profiler.get().zone("structure")) {
			zone.addText(structure.getRegisteredName());
			StructureStart start = original.call(structure, generator, registryAccess, randomState, structureTemplateManager, seed, chunkPos, heightAccessor, dimension, climateSampler);
			if (start == null) {
				zone.addText("(not placed)");
			}
			return start;
		}
	}
}
