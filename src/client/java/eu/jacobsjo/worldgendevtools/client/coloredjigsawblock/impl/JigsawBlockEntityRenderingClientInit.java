package eu.jacobsjo.worldgendevtools.client.coloredjigsawblock.impl;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.core.registries.BuiltInClientRegistries;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityTypes;

@Environment(EnvType.CLIENT)
public class JigsawBlockEntityRenderingClientInit implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
	    registerBlockEntityRenderer(BlockEntityTypes.JIGSAW, JigsawBlockEntityRenderer::new);
		Registry.register(
			BuiltInClientRegistries.ITEM_TINT_SOURCE_TYPE,
			Identifier.fromNamespaceAndPath("worldgendevtools", "jigsaw"),
			JigsawTintSource.MAP_CODEC
		);
    }
	
	// Hack because generics are weird
	private static <T extends BlockEntity, S extends BlockEntityRenderState> void registerBlockEntityRenderer(BlockEntityType<? extends T> type, BlockEntityRendererProvider<T, S> renderer) {
		Registry.register(BuiltInClientRegistries.BLOCK_ENTITY_RENDERER, BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(type), renderer);
	}
}
