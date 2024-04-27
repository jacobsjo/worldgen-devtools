package eu.jacobsjo.worldgendevtools.dfcommand;

import eu.jacobsjo.worldgendevtools.dfcommand.command.DfCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class DfCommandInit implements ModInitializer {

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> DfCommand.register(dispatcher)));
    }
}
