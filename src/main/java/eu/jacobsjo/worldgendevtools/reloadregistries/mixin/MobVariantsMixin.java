package eu.jacobsjo.worldgendevtools.reloadregistries.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import eu.jacobsjo.worldgendevtools.reloadregistries.api.OutdatedHolder;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.animal.CowVariants;
import net.minecraft.world.entity.animal.PigVariants;
import net.minecraft.world.entity.animal.WolfVariants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.stream.Stream;

@Mixin({WolfVariants.class, PigVariants.class, CowVariants.class})
public class MobVariantsMixin {

    // Don't spawn outdated variant
    @ModifyExpressionValue(require = 1, method = {"selectVariantToSpawn", "method_67139", "method_66314", "method_67354"}, remap = false, at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Registry;listElements()Ljava/util/stream/Stream;", remap = true))
    private static Stream<Holder.Reference<?>> filterSpawnVariantHolders(Stream<Holder.Reference<?>> original){
        return original.filter(holder -> !((OutdatedHolder) holder).worldgenDevtools$isOutdated());
    }
}

