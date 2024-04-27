package eu.jacobsjo.worldgendevtools.vanillacommands;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.commands.ChaseCommand;

public class VanillaCommandsInit implements ModInitializer {
    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> ChaseCommand.register(dispatcher));
    }
}
