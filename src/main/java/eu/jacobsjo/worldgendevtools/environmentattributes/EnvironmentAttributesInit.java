package eu.jacobsjo.worldgendevtools.environmentattributes;

import eu.jacobsjo.worldgendevtools.environmentattributes.impl.AttributeOverrides;
import eu.jacobsjo.worldgendevtools.environmentattributes.impl.EnvironmentAttributeCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.resources.Identifier;

@SuppressWarnings("UnstableApiUsage")
public class EnvironmentAttributesInit implements ModInitializer {
    public static final AttachmentType<AttributeOverrides> ENVIRONMENT_ATTRIBUTE_OVERRIDES = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath("worldgendevtools", "environment_attribute_overrides"),
            builder -> builder
                    .initializer(AttributeOverrides::new)
                    .persistent(AttributeOverrides.CODEC)
                    .syncWith(AttributeOverrides.STREAM_CODEC, AttachmentSyncPredicate.all())
                    .initializer(AttributeOverrides::new)
    );

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> EnvironmentAttributeCommand.register(dispatcher, registryAccess)));
    }
}
