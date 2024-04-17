package eu.jacobsjo.worldgendevtools.reloadregistries.mixin;

import eu.jacobsjo.worldgendevtools.reloadregistries.ReloadRegistriesInit;
import eu.jacobsjo.worldgendevtools.reloadregistries.impl.ComponentFormattedException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.ReloadCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ReloadCommand.class)
public class ReloadCommandMixin {
    @Inject(method = "method_29479", at = @At("TAIL"))
    private static void sendFailure(CommandSourceStack source, Throwable throwable, CallbackInfoReturnable<Void> cir){
        if (throwable.getCause() instanceof ComponentFormattedException componentFormattedException){
            source.sendFailure(componentFormattedException.getComponentMessage());
        } else {
            source.sendFailure(Component.literal(throwable.getMessage()));
        }

        if (source.getServer().getGameRules().getRule(ReloadRegistriesInit.RELOAD_REGISTIRES).get()){
            source.sendFailure(Component.translatableWithFallback("worldgendevtools.reloadregistries.failure", "Failed to reload registires. Registries are in indeterminate state. Fix the errors and reload again or reopen the world!"));
        }
    }
}
