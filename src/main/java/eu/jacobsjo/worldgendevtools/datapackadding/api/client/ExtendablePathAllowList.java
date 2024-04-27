package eu.jacobsjo.worldgendevtools.datapackadding.api.client;

import net.minecraft.world.level.validation.PathAllowList;

public interface ExtendablePathAllowList {
    void worldgenDevtools$addEntry(PathAllowList.ConfigEntry entry);
}
