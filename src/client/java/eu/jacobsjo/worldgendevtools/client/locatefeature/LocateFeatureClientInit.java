package eu.jacobsjo.worldgendevtools.client.locatefeature;

import eu.jacobsjo.worldgendevtools.client.locatefeature.impl.LocationRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.gui.components.debug.DebugEntryNoop;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.resources.ResourceLocation;

public class LocateFeatureClientInit implements ClientModInitializer {
    public static ResourceLocation FEATURE_POSITIONS_RENDERER;

    @Override
    public void onInitializeClient() {
        FEATURE_POSITIONS_RENDERER = DebugScreenEntries.register(ResourceLocation.fromNamespaceAndPath("worldgendevtools", "feature_positions"), new DebugEntryNoop());
        WorldRenderEvents.LAST.register(LocationRenderer::render);
    }


}
