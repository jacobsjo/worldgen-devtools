package eu.jacobsjo.worldgendevtools.client.locatefeature;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.debug.DebugEntryNoop;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.resources.Identifier;

public class LocateFeatureClientInit implements ClientModInitializer {
    public static Identifier FEATURE_POSITIONS_RENDERER;
    public static KeyMapping TOGGLE_FEATURE_POSITIONS_KEY;

    @Override
    public void onInitializeClient() {
        FEATURE_POSITIONS_RENDERER = DebugScreenEntries.register(Identifier.fromNamespaceAndPath("worldgendevtools", "feature_positions"), new DebugEntryNoop());

        TOGGLE_FEATURE_POSITIONS_KEY = KeyMappingHelper.registerKeyMapping(
                new KeyMapping(
                        "worldgendevtools.locatefeature.renderer.keybind",
                        InputConstants.Type.KEYBOARD,
                        9,
                        KeyMapping.Category.DEBUG
                ));
    }
}
