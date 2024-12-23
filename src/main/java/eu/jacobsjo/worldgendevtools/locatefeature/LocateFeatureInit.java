package eu.jacobsjo.worldgendevtools.locatefeature;

import eu.jacobsjo.worldgendevtools.locatefeature.impl.FeaturePositions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("UnstableApiUsage")
public class LocateFeatureInit implements ModInitializer {
    public static final AttachmentType<FeaturePositions> FEATURE_POSITION_ATTACHMENT = AttachmentRegistry.<FeaturePositions>create(
        ResourceLocation.fromNamespaceAndPath("worldgendevtools", "feature_positions"),
        builder -> builder
            .initializer(FeaturePositions::new)
            .persistent(FeaturePositions.CODEC)
            .syncWith(FeaturePositions.STREAM_CODEC, (attachmentTarget, serverPlayer) -> serverPlayer.hasPermissions(2))
    );

    @Override
    public void onInitialize() {

    }

}

