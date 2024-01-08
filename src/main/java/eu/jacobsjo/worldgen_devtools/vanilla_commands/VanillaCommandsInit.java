package eu.jacobsjo.worldgen_devtools.vanilla_commands;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.commands.ChaseCommand;
import net.minecraft.server.commands.ResetChunksCommand;

public class VanillaCommandsInit implements ModInitializer {
    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            ResetChunksCommand.register(dispatcher);
            ChaseCommand.register(dispatcher);
        });
    }
}
