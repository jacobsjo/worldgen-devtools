package eu.jacobsjo.worldgendevtools.reloadregistries.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import eu.jacobsjo.worldgendevtools.reloadregistries.api.OutdatedHolder;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.animal.CatVariants;
import net.minecraft.world.entity.animal.frog.FrogVariants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.stream.Stream;

@Mixin({FrogVariants.class, CatVariants.class})
public interface MobVariantsInterfaceMixin {

    // Don't spawn outdated variant
    @ModifyExpressionValue(require=1, method = {"selectVariantToSpawn", "method_67144", "method_67129"}, remap = false, at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Registry;listElements()Ljava/util/stream/Stream;", remap = true))
    private static Stream<Holder.Reference<?>> filterSpawnVariantHolders(Stream<Holder.Reference<?>> original){
        return original.filter(holder -> !((OutdatedHolder) holder).worldgenDevtools$isOutdated());
    }
}

