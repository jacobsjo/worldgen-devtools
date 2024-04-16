package eu.jacobsjo.worldgendevtools.reloadregistries.mixin;


import eu.jacobsjo.worldgendevtools.reloadregistries.api.OutdatedHolder;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Holder.Reference.class)
public class HolderMixin<T> implements OutdatedHolder {
    @Shadow private @Nullable ResourceKey<T> key;
    @Unique
    private boolean outdated = false;

    @Override
    public void worldgenDevtools$markOutdated(boolean outdated) {
        this.outdated = outdated;
    }

    @Override
    public boolean worldgenDevtools$isOutdated() {
        return this.outdated;
    }

}
