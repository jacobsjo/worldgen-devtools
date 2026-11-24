package eu.jacobsjo.worldgendevtools.client.locatefeature;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.core.registries.BuiltInClientRegistries;
import net.minecraft.client.core.registries.ClientRegistries;
import net.minecraft.client.gui.components.debug.DebugEntryNoop;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public class LocateFeatureClientInit implements ClientModInitializer {
    public static ResourceKey<DebugScreenEntry> FEATURE_POSITIONS_RENDERER = ResourceKey.create(ClientRegistries.DEBUG_SCREEN_ENTRY, Identifier.fromNamespaceAndPath("worldgendevtools", "feature_positions"));
    public static KeyMapping TOGGLE_FEATURE_POSITIONS_KEY;

    @Override
    public void onInitializeClient() {
	    Registry.register(BuiltInClientRegistries.DEBUG_SCREEN_ENTRY, FEATURE_POSITIONS_RENDERER, new DebugEntryNoop());

        TOGGLE_FEATURE_POSITIONS_KEY = KeyMappingHelper.registerKeyMapping(
                new KeyMapping(
                        "worldgendevtools.locatefeature.renderer.keybind",
                        InputConstants.Type.KEYBOARD,
                        9,
                        KeyMapping.Category.DEBUG
                ));
    }
}
