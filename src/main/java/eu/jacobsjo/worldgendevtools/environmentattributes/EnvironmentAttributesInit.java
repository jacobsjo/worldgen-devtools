package eu.jacobsjo.worldgendevtools.environmentattributes;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class EnvironmentAttributesInit implements ModInitializer {
    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> EnvironmentAttributeCommand.register(dispatcher, registryAccess)));
    }
}
