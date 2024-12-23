package eu.jacobsjo.worldgendevtools.client.locatefeature;

import eu.jacobsjo.worldgendevtools.client.locatefeature.impl.LocationRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class LocateFeatureClientInit implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        WorldRenderEvents.LAST.register(LocationRenderer::render);
    }



}
