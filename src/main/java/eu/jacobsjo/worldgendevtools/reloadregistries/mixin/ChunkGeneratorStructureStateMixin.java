package eu.jacobsjo.worldgendevtools.reloadregistries.mixin;


import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import eu.jacobsjo.worldgendevtools.reloadregistries.api.OutdatedHolder;
import net.minecraft.core.Holder;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.stream.Stream;

@Mixin(ChunkGeneratorStructureState.class)
public class ChunkGeneratorStructureStateMixin {
    // Don't generate structures from outdated structure sets after reloading.
    @ModifyExpressionValue(method = "createForNormal", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/HolderLookup;listElements()Ljava/util/stream/Stream;"))
    private static Stream<Holder.Reference<StructureSet>> createForNormal(Stream<Holder.Reference<StructureSet>> original){
        return original.filter(holder -> !((OutdatedHolder) holder).worldgenDevtools$isOutdated());
    }
}
