package eu.jacobsjo.worldgendevtools.environmentattributes;

import eu.jacobsjo.worldgendevtools.environmentattributes.impl.AttributeOverrides;
import eu.jacobsjo.worldgendevtools.environmentattributes.impl.EnvironmentAttributeCommand;
import eu.jacobsjo.worldgendevtools.locatefeature.impl.FeaturePositions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.permissions.Permissions;

@SuppressWarnings("UnstableApiUsage")
public class EnvironmentAttributesInit implements ModInitializer {
    public static final AttachmentType<AttributeOverrides> ENVIRONMENT_ATTRIBUTE_OVERRIDES = AttachmentRegistry.<AttributeOverrides>create(
            Identifier.fromNamespaceAndPath("worldgendevtools", "feature_positions"),
            builder -> builder
                    .initializer(AttributeOverrides::new)
                    .syncWith(AttributeOverrides.STREAM_CODEC)
    );

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> EnvironmentAttributeCommand.register(dispatcher, registryAccess)));
        ServerWorldEvents.LOAD.register((minecraftServer, serverLevel) -> AttributeOverrides.clearOverrides());
    }
}
