package eu.jacobsjo.worldgen_devtools;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.commands.ResetChunksCommand;

public class WorldgenDevtools implements ModInitializer{
    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> {
            ResetChunksCommand.register(dispatcher);
        }));

    }
}
