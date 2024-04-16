package eu.jacobsjo.worldgendevtools.reloadregistries.mixin;


import eu.jacobsjo.worldgendevtools.reloadregistries.api.OutdatedHolder;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagLoader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.function.Function;

@Mixin(TagLoader.class)
public class TagLoaderMixin<T> {
    @Mutable @Shadow @Final Function<ResourceLocation, Optional<? extends T>> idToValue;

    // disallow tag loading outdated holders
    @Inject(method = "<init>", at = @At("TAIL"))
    private void element(Function<ResourceLocation, Optional<? extends T>> idToValue, String directory, CallbackInfo ci){
        this.idToValue = idToValue.andThen(t -> t.filter(h -> !(h instanceof Holder<?>) || !(((OutdatedHolder) h).worldgenDevtools$isOutdated())));
    }
}
