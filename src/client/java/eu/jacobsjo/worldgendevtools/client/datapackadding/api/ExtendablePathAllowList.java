package eu.jacobsjo.worldgendevtools.client.datapackadding.api;

import net.minecraft.world.level.validation.PathAllowList;

public interface ExtendablePathAllowList {
    void worldgenDevtools$addEntry(PathAllowList.ConfigEntry entry);
}
