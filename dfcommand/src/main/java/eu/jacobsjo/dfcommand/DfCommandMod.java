package eu.jacobsjo.dfcommand;

import eu.jacobsjo.dfcommand.command.DfCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class DfCommandMod implements ModInitializer {

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> {
            DfCommand.register(dispatcher);
        }));
    }
}
