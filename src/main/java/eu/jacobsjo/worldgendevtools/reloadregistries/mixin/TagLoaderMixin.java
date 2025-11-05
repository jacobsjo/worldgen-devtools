package eu.jacobsjo.worldgendevtools.reloadregistries.mixin;


import eu.jacobsjo.worldgendevtools.reloadregistries.api.OutdatedHolder;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagLoader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TagLoader.class)
public class TagLoaderMixin<T> {
    @Mutable @Shadow @Final TagLoader.ElementLookup<T> elementLookup;

    // disallow tag loading outdated holders
    @Inject(method = "<init>", at = @At("TAIL"))
    private void filterTagElements(TagLoader.ElementLookup<T> elementLookup, String string, CallbackInfo ci){
        TagLoader.ElementLookup<T> originalLookup = this.elementLookup;
        this.elementLookup = (Identifier, bl) -> originalLookup.get(Identifier, bl).filter(h -> !(h instanceof Holder<?>) || !(((OutdatedHolder) h).worldgenDevtools$isOutdated()));
    }
}
