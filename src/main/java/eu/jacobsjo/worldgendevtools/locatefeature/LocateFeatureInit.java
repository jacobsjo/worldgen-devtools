package eu.jacobsjo.worldgendevtools.locatefeature;

import eu.jacobsjo.worldgendevtools.locatefeature.impl.FeaturePositions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("UnstableApiUsage")
public class LocateFeatureInit implements ModInitializer {
    public static final AttachmentType<FeaturePositions> FEATURE_POSITION_ATTACHMENT = AttachmentRegistry.<FeaturePositions>builder()
            .initializer(FeaturePositions::new)
            .persistent(FeaturePositions.CODEC)
            .buildAndRegister(new ResourceLocation("worldgendevtools", "feature_positions"));

    @Override
    public void onInitialize() {

    }

}

