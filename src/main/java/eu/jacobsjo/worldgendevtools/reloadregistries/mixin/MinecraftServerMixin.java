package eu.jacobsjo.worldgendevtools.reloadregistries.mixin;

import com.google.common.collect.ImmutableList;
import eu.jacobsjo.worldgendevtools.reloadregistries.ReloadRegistriesInit;
import eu.jacobsjo.worldgendevtools.reloadregistries.impl.RegistryReloader;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerConnectionListener;
import net.minecraft.server.packs.PackResources;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.WorldData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Shadow @Final private LayeredRegistryAccess<RegistryLayer> registries;
    @Shadow @Final private Map<ResourceKey<Level>, ServerLevel> levels;
    @Shadow public abstract ServerConnectionListener getConnection();
    @Shadow public abstract WorldData getWorldData();

    /**
     * This is a lambda in the reloadResources method that gets called with the collected packResources. We add the reloading
     * of the registries to the beginning.
     * @param resources the collected resources
     */
    @Inject (method = "method_29437", at = @At("HEAD"))
    private void thenCompose(ImmutableList<PackResources> resources, CallbackInfoReturnable<CompletionStage<?>> cir) {
        if (this.getWorldData().getGameRules().get(ReloadRegistriesInit.RELOAD_REGISTIRES)) {
            RegistryReloader.reloadRegistries(this.registries, this.levels, resources);
        }
    }

    /**
     * After the resources are reloaded we need to resync the registries with the clients. The method returns a {@link CompletableFuture},
     * so we only resync the clients when that is done.
     */
    @Inject(method = "reloadResources", at = @At("RETURN"))
    private void afterReloadResources(Collection<String> selectedIds, CallbackInfoReturnable<CompletableFuture<Void>> cir){
        if (this.getWorldData().getGameRules().get(ReloadRegistriesInit.RELOAD_REGISTIRES) && this.getWorldData().getGameRules().get(ReloadRegistriesInit.SYNC_AFTER_REGISTRY_RELOAD)) {
            cir.getReturnValue().thenAccept(reloadableResources -> RegistryReloader.syncClient(this.getConnection()));
        }
    }

}
