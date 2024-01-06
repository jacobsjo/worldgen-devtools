package eu.jacobsjo.worldgen_devtools.api;

import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.structure.Structure;

public interface HolderStructureStart {
    void worldgenDevtools$setHolder(Holder.Reference<Structure> holder);
}
