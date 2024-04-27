    package eu.jacobsjo.worldgendevtools.datapackadding.mixin.client;


import eu.jacobsjo.worldgendevtools.datapackadding.api.client.ExtendablePathAllowList;
import net.minecraft.world.level.validation.PathAllowList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

    @Mixin(PathAllowList.class)
public class PathAllowListMixin implements ExtendablePathAllowList {

    @Shadow @Final @Mutable
    private List<PathAllowList.ConfigEntry> entries;

        @Shadow @Final private Map<String, PathMatcher> compiledPaths;

        @Override
    public void worldgenDevtools$addEntry(PathAllowList.ConfigEntry entry) {
        if (!(this.entries instanceof ArrayList<PathAllowList.ConfigEntry>)) {
            this.entries = new ArrayList<>(this.entries);
        }

        this.entries.add(entry);
        this.compiledPaths.clear();
    }
}
