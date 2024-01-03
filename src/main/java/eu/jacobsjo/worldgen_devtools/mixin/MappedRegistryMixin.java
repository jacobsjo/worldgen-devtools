package eu.jacobsjo.worldgen_devtools.mixin;

import com.mojang.serialization.Lifecycle;
import eu.jacobsjo.worldgen_devtools.RegistryResetter;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Map;

@Mixin(MappedRegistry.class)
public class MappedRegistryMixin<T> implements RegistryResetter {


    @Shadow @Final private ObjectList<Holder.Reference<T>> byId;

    @Shadow @Final private Reference2IntMap<T> toId;

    @Shadow @Final private Map<ResourceLocation, Holder.Reference<T>> byLocation;

    @Shadow @Final private Map<ResourceKey<T>, Holder.Reference<T>> byKey;

    @Shadow @Final private Map<T, Holder.Reference<T>> byValue;

    @Shadow @Final private Map<T, Lifecycle> lifecycles;

    @Shadow private volatile Map<TagKey<T>, HolderSet.Named<T>> tags;

    @Shadow private boolean frozen;

    @Shadow private int nextId;

    @Shadow private @Nullable List<Holder.Reference<T>> holdersInOrder;

    @Override
    public void reset() {
        this.byKey.forEach((key, value) -> value.bindValue(null));

        this.byId.clear();
        this.toId.clear();
        this.byLocation.clear();
        //this.byKey.clear();
        this.byValue.clear();
        this.lifecycles.clear();
        this.tags.clear();
        this.frozen = false;
        this.nextId = 0;
        this.holdersInOrder = null;
    }

    @Override
    public void unfreeze(){
        this.frozen = false;
    }
}
