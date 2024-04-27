package eu.jacobsjo.worldgendevtools.client.coloredjigsawblock.impl;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
@Environment(EnvType.CLIENT)
public class JigsawBlockEntityRenderingClientInit implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockEntityRenderers.register(BlockEntityType.JIGSAW, JigsawBlockEntityRenderer::new);
        BuiltinItemRendererRegistry.INSTANCE.register(Items.JIGSAW, new JigsawBlockItemRenderer());
    }
}
