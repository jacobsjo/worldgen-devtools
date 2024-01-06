package eu.jacobsjo.worldgen_devtools.mixin;

import com.mojang.serialization.Lifecycle;
import eu.jacobsjo.worldgen_devtools.api.ReloadableRegistry;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mixin(MappedRegistry.class)
public abstract class MappedRegistryMixin<T> implements ReloadableRegistry {
    @Shadow @Final private ObjectList<Holder.Reference<T>> byId;
    @Shadow @Final private Reference2IntMap<T> toId;
    @Shadow @Final private Map<ResourceLocation, Holder.Reference<T>> byLocation;
    @Shadow @Final private Map<ResourceKey<T>, Holder.Reference<T>> byKey;
    @Shadow @Final private Map<T, Holder.Reference<T>> byValue;
    @Shadow @Final private Map<T, Lifecycle> lifecycles;
    @Shadow private boolean frozen;
    @Shadow private @Nullable List<Holder.Reference<T>> holdersInOrder;
    @Shadow public abstract @Nullable T get(@Nullable ResourceKey<T> key);
    @Shadow public abstract int getId(@Nullable T value);
    @Shadow public abstract Holder.Reference<T> registerMapping(int id, ResourceKey<T> key, T value, Lifecycle lifecycle);
    @Shadow private @Nullable Map<T, Holder.Reference<T>> unregisteredIntrusiveHolders;
    @Shadow @Final private ResourceKey<? extends Registry<T>> key;
    @Shadow @Final private static Logger LOGGER;
    @Unique private boolean reloading = false;
    @Unique private Set<ResourceKey<T>> outdatedKeys;


    /**
     * configures the registry for reloading and marks all current keys as outdated.
     */
    @Override
    public void worldgenDevtools$startReload(){
        if (this.unregisteredIntrusiveHolders != null){
            throw new IllegalStateException("Trying to reload registry " + key.toString() + " which has intrusive holders.");
        }

        this.outdatedKeys = new HashSet<>(byKey.keySet());

        this.holdersInOrder = null;
        this.frozen = false;
        this.reloading = true;
    }

    /**
     * when refreezing a reloading registry, remove all keys that are still outdated.
     */
    @Inject(method = "freeze", at = @At("HEAD"))
    public void freeze(CallbackInfoReturnable<Registry<T>> cir){
        if (this.reloading){
            this.outdatedKeys.forEach(key -> {

                // remove old element from registry
                T value = this.get(key);
                int id = this.getId(value);
                this.toId.remove(value, id);
                this.byId.set(id, null);
                this.byValue.remove(value);
                this.lifecycles.remove(value);
                this.byLocation.remove(key.location());
                this.byKey.get(key).bindValue(null); // make sure outdated holder is unbound, causing Exceptions should they still be in use
                this.byKey.remove(key);
                LOGGER.info("Removing {} from registry", key);
            });
            this.reloading = false;
        }
    }

    /**
     * when registering into a reloading registry, override existing ResourceLocations and unmark key as outdated.
     */
    @Inject(method = "register", at = @At("HEAD"), cancellable = true)
    public void register(ResourceKey<T> key, T value, Lifecycle lifecycle, CallbackInfoReturnable<Holder.Reference<T>> cir) {
        if (this.reloading && this.byLocation.containsKey(key.location())){
            // existing element that is changed
            this.outdatedKeys.remove(key);
            T oldValue = this.get(key);
            int id = this.getId(oldValue);
            this.byLocation.remove(key.location());
            cir.setReturnValue(this.registerMapping(id, key, value, lifecycle));
            this.toId.remove(oldValue, id);
            this.byValue.remove(oldValue);
            this.lifecycles.remove(oldValue);
            cir.cancel();
        }
    }
}
