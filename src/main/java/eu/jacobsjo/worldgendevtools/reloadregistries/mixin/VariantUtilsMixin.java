package eu.jacobsjo.worldgendevtools.reloadregistries.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import eu.jacobsjo.worldgendevtools.reloadregistries.api.OutdatedHolder;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.variant.VariantUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.stream.Stream;

@Mixin(VariantUtils.class)
public class VariantUtilsMixin {

    // Don't spawn outdated variant
    @ModifyExpressionValue(require = 1, method = "selectVariantToSpawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Registry;listElements()Ljava/util/stream/Stream;"))
    private static Stream<Holder.Reference<?>> filterSpawnVariantHolders(Stream<Holder.Reference<?>> original){
        return original.filter(holder -> !((OutdatedHolder) holder).worldgenDevtools$isOutdated());
    }
}

