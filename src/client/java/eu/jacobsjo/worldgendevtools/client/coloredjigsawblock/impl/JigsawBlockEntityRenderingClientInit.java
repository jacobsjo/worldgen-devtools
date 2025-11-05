package eu.jacobsjo.worldgendevtools.client.coloredjigsawblock.impl;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;
@Environment(EnvType.CLIENT)
public class JigsawBlockEntityRenderingClientInit implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockEntityRenderers.register(BlockEntityType.JIGSAW, JigsawBlockEntityRenderer::new);
        ItemTintSources.ID_MAPPER.put(Identifier.fromNamespaceAndPath("worldgendevtools", "jigsaw"), JigsawTintSource.MAP_CODEC );
    }
}
