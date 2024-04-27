package eu.jacobsjo.worldgendevtools.reloadregistries.mixin;


import eu.jacobsjo.worldgendevtools.reloadregistries.api.OutdatedHolder;
import net.minecraft.core.Holder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Holder.Reference.class)
public class HolderMixin implements OutdatedHolder {
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
