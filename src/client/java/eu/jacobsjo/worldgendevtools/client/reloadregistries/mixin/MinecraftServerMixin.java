package eu.jacobsjo.worldgendevtools.client.reloadregistries.mixin;

import eu.jacobsjo.worldgendevtools.reloadregistries.ReloadRegistriesInit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

@Environment(value= EnvType.CLIENT)
@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Shadow public abstract GameRules getGameRules();

    @Shadow
    public abstract @Nullable ServerLevel getLevel(ResourceKey<Level> dimension);

    /**
     * After the resources are reloaded on an embedded server, we need reload the chunks on the local player. The method
     * returns a {@link CompletableFuture}, so we only reload the chunks when that is done. See also
     * {@link eu.jacobsjo.worldgendevtools.reloadregistries.mixin.MinecraftServerMixin} for resyncing remote clients.
     */
    @Inject(method = "reloadResources", at = @At("RETURN"))
    private void afterReloadResources(Collection<String> selectedIds, CallbackInfoReturnable<CompletableFuture<Void>> cir){
        if (this.getGameRules().get(ReloadRegistriesInit.RELOAD_REGISTIRES) && this.getGameRules().get(ReloadRegistriesInit.SYNC_AFTER_REGISTRY_RELOAD)) {
            cir.getReturnValue().thenAccept(reloadableResources -> Minecraft.getInstance().execute(() -> {
                Minecraft.getInstance().level.dimensionTypeRegistration = this.getLevel(Minecraft.getInstance().level.dimension()).dimensionTypeRegistration;
                Minecraft.getInstance().level.environmentAttributes = Minecraft.getInstance().level.addEnvironmentAttributeLayers(EnvironmentAttributeSystem.builder()).build();
                Minecraft.getInstance().levelRenderer.allChanged();
            }));
        }
    }
}