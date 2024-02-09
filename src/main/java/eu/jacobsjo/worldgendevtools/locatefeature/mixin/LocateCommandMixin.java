package eu.jacobsjo.worldgendevtools.locatefeature.mixin;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import eu.jacobsjo.worldgendevtools.locatefeature.impl.LocateFeature;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.LocateCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LocateCommand.class)
public class LocateCommandMixin {

    @ModifyArg(
            method = "register",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/brigadier/CommandDispatcher;register(Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;)Lcom/mojang/brigadier/tree/LiteralCommandNode;",
                    remap = false
            )
    )
    private static LiteralArgumentBuilder<CommandSourceStack> register(LiteralArgumentBuilder<CommandSourceStack> command){
        return LocateFeature.addSubcommand(command);
    }
}
