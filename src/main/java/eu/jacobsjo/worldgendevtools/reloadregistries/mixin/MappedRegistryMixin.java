package eu.jacobsjo.worldgendevtools.reloadregistries.mixin;

import com.mojang.serialization.Lifecycle;
import eu.jacobsjo.worldgendevtools.reloadregistries.api.OutdatedHolder;
import eu.jacobsjo.worldgendevtools.reloadregistries.api.ReloadableRegistry;
import eu.jacobsjo.worldgendevtools.reloadregistries.impl.RegistryReloader;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import net.minecraft.Util;
import net.minecraft.core.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Mixin(MappedRegistry.class)
public abstract class MappedRegistryMixin<T> implements ReloadableRegistry  {
    @Shadow @Final private ObjectList<Holder.Reference<T>> byId;
    @Shadow @Final private Reference2IntMap<T> toId;
    @Shadow @Final private Map<ResourceLocation, Holder.Reference<T>> byLocation;
    @Shadow @Final private Map<ResourceKey<T>, Holder.Reference<T>> byKey;
    @Shadow @Final private Map<T, Holder.Reference<T>> byValue;
    @Shadow private boolean frozen;
    @Shadow public abstract @Nullable T getValue(@Nullable ResourceKey<T> key);
    @Shadow public abstract int getId(@Nullable T value);
    @Shadow private @Nullable Map<T, Holder.Reference<T>> unregisteredIntrusiveHolders;
    @Shadow @Final private ResourceKey<? extends Registry<T>> key;
    @Shadow private Lifecycle registryLifecycle;
    @Shadow @Final private Map<ResourceKey<T>, RegistrationInfo> registrationInfos;

    @Shadow public abstract Optional<Holder.Reference<T>> get(ResourceKey<T> key);

    @Shadow public abstract ResourceKey<? extends Registry<T>> key();

    @Shadow MappedRegistry.TagSet<T> allTags;
    @Unique private boolean reloading = false;
    @Unique private Set<ResourceKey<T>> outdatedKeys = new HashSet<>();
    @Unique private final Set<ResourceKey<T>> requiredNewKeys = new HashSet<>();

    /**
     * configures the registry for reloading and marks all current keys as outdated.
     */
    @Override
    public void worldgenDevtools$startReload(){
        if (this.unregisteredIntrusiveHolders != null){
            throw new IllegalStateException("Trying to reload registry " + key.toString() + " which has intrusive holders.");
        }

        // remove unbound keys, they remained from last failed reload, but shouldn't cause this one to fail.
        this.byKey.entrySet().removeIf(entry -> !entry.getValue().isBound());

        this.outdatedKeys = new HashSet<>(this.byKey.keySet());
        this.requiredNewKeys.clear();

        this.frozen = false;
        this.reloading = true;
        this.allTags = MappedRegistry.TagSet.unbound();
    }

    /**
     * when refreezing a reloading registry, remove all keys that are still outdated.
     */
    @Inject(method = "freeze", at = @At(value = "INVOKE", target = "Ljava/util/Map;forEach(Ljava/util/function/BiConsumer;)V", shift = At.Shift.AFTER))
    public void freeze(CallbackInfoReturnable<Registry<T>> cir){
        if (this.reloading){
            this.reloading = false;

            this.outdatedKeys.forEach(key -> {
                RegistryReloader.LOGGER.info("Outdated element {} remains in registry", key);
                Holder.Reference<T> holder = this.get(key).orElseThrow();
                ((OutdatedHolder) holder).worldgenDevtools$markOutdated(true);
            });
            if (!this.requiredNewKeys.isEmpty()){
                throw new IllegalStateException("References remain to outdated keys from registry " + this.key() + ": " + this.requiredNewKeys);
            }
        }
    }

    /**
     * when registering into a reloading registry, override existing ResourceLocations and unmark key as outdated.
     */
    @SuppressWarnings("unchecked")
    @Inject(method = "register", at = @At("HEAD"), cancellable = true)
    public void register(ResourceKey<T> key, T value, RegistrationInfo registrationInfo, CallbackInfoReturnable<Holder.Reference<T>> cir) {
        if (this.reloading && this.byLocation.containsKey(key.location())){
            // existing element that is changed
            this.outdatedKeys.remove(key);
            this.requiredNewKeys.remove(key);
            T oldValue = this.getValue(key);
            int id = this.getId(oldValue);

            this.toId.remove(oldValue, id);
            this.byValue.remove(oldValue);

            if (this.byValue.containsKey(value)) {
                // would also crash if reassigning value to new key, but this shouldn't happen in practice.
                Util.pauseInIde(new IllegalStateException("Adding duplicate value '" + value + "' to registry"));
            }

            Holder.Reference<T> reference = this.byKey.computeIfAbsent(key, resourceKeyx -> Holder.Reference.createStandAlone((MappedRegistry<T>) (Object) this, resourceKeyx));
            ((OutdatedHolder) reference).worldgenDevtools$markOutdated(false);

            this.byKey.put(key, reference);
            this.byLocation.put(key.location(), reference);
            this.byValue.put(value, reference);
            this.byId.set(id, reference);
            this.toId.put(value, id);
            this.registrationInfos.put(key, registrationInfo);
            this.registryLifecycle = this.registryLifecycle.add(registrationInfo.lifecycle());

            cir.setReturnValue(reference);
            cir.cancel();
        }
    }

    @Inject(method = "getOrCreateHolderOrThrow", at = @At("RETURN"))
    public void getOrCreateHolderOrThrow(ResourceKey<T> key, CallbackInfoReturnable<Holder.Reference<T>> cir){
        if (this.outdatedKeys.contains(key)) {
            this.outdatedKeys.remove(key);
            this.requiredNewKeys.add(key);
        }
    }
}
