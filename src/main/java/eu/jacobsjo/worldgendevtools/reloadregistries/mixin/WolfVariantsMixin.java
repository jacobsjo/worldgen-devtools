package eu.jacobsjo.worldgendevtools.reloadregistries.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import eu.jacobsjo.worldgendevtools.reloadregistries.api.OutdatedHolder;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.animal.WolfVariant;
import net.minecraft.world.entity.animal.WolfVariants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.stream.Stream;

@Mixin(WolfVariants.class)
public class WolfVariantsMixin {

    // Don't spawn wolves with outdated variant
    @ModifyExpressionValue(method = "getSpawnVariant", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Registry;holders()Ljava/util/stream/Stream;"))
    private static Stream<Holder.Reference<WolfVariant>> filterSpawnVariantHolders(Stream<Holder.Reference<WolfVariant>> original){
        return original.filter(holder -> !((OutdatedHolder) holder).worldgenDevtools$isOutdated());
    }
}
