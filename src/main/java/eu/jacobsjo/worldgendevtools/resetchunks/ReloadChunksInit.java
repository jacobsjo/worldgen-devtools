package eu.jacobsjo.worldgendevtools.resetchunks;

import eu.jacobsjo.worldgendevtools.resetchunks.impl.ResetChunksCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class ReloadChunksInit implements ModInitializer {
    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> ResetChunksCommand.register(dispatcher));
    }
}
