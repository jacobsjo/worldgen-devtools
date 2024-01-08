package eu.jacobsjo.worldgen_devtools.reload_registries.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
@Environment(value= EnvType.CLIENT)
@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    /**
     * After the resources are reloaded on an embedded server, we need reload the chunks on the local player. The method
     * returns a {@link CompletableFuture}, so we only reload the chunks when that is done. See also
     * {@link eu.jacobsjo.worldgen_devtools.reload_registries.mixin.MinecraftServerMixin#afterReloadResources} for resyncing remove clients.
     */
    @Inject(method = "reloadResources", at = @At("RETURN"))
    private void afterReloadResources(Collection<String> selectedIds, CallbackInfoReturnable<CompletableFuture<Void>> cir){
        cir.getReturnValue().thenAccept(reloadableResources -> Minecraft.getInstance().execute(() -> Minecraft.getInstance().levelRenderer.allChanged()));
    }
}
