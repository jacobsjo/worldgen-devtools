package eu.jacobsjo.worldgendevtools.coloredjigsawblock.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.DataResult;
import eu.jacobsjo.worldgendevtools.coloredjigsawblock.impl.JigsawBlockData;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.FrontAndTop;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public class JigsawBlockItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {

    private static final ResourceLocation EMPTY_RESOURCE_LOCATION = new ResourceLocation("empty");
    @Override
    public void render(ItemStack stack, ItemDisplayContext mode, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        TypedDataComponent<CustomData> blockEntityData = stack.getComponents().getTyped(DataComponents.BLOCK_ENTITY_DATA);
        if (blockEntityData == null) {
            JigsawBlockEntityRenderer.render(EMPTY_RESOURCE_LOCATION, EMPTY_RESOURCE_LOCATION, FrontAndTop.NORTH_UP, poseStack, buffer, overlay, true);
        } else {
            DataResult<JigsawBlockData> data = blockEntityData.value().read(JigsawBlockData.CODEC);
            if (data.isError() || data.result().isEmpty()){
                JigsawBlockEntityRenderer.render(EMPTY_RESOURCE_LOCATION, EMPTY_RESOURCE_LOCATION, FrontAndTop.NORTH_UP, poseStack, buffer, overlay, true);
            } else {
                JigsawBlockEntityRenderer.render(data.result().get().name(), data.result().get().target(), FrontAndTop.NORTH_UP, poseStack, buffer, overlay, true);
            }
        }
    }

}
