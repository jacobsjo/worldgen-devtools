package eu.jacobsjo.worldgendevtools.reloadregistries.mixin;

import com.mojang.serialization.Lifecycle;
import eu.jacobsjo.worldgendevtools.reloadregistries.api.ReloadableRegistry;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
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
    @Shadow public abstract @Nullable T get(@Nullable ResourceKey<T> key);
    @Shadow public abstract int getId(@Nullable T value);
    @Shadow private @Nullable Map<T, Holder.Reference<T>> unregisteredIntrusiveHolders;
    @Shadow @Final ResourceKey<? extends Registry<T>> key;
    @Shadow @Final private static Logger LOGGER;

    @Shadow public abstract HolderOwner<T> holderOwner();

    @Shadow private Lifecycle registryLifecycle;
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

            this.toId.remove(oldValue, id);
            this.byValue.remove(oldValue);
            this.lifecycles.remove(oldValue);

            if (this.byValue.containsKey(value)) {
                // would also crash if reassigning value to new key, but this shouldn't happen in practice.
                Util.pauseInIde(new IllegalStateException("Adding duplicate value '" + value + "' to registry"));
            }

            Holder.Reference<T> reference = this.byKey.computeIfAbsent(key, resourceKeyx -> Holder.Reference.createStandAlone(this.holderOwner(), resourceKeyx));

            this.byKey.put(key, reference);
            this.byLocation.put(key.location(), reference);
            this.byValue.put(value, reference);
            this.byId.set(id, reference);
            this.toId.put(value, id);
            this.lifecycles.put(value, lifecycle);
            this.registryLifecycle = this.registryLifecycle.add(lifecycle);

            cir.setReturnValue(reference);
            cir.cancel();
        }
    }
}
