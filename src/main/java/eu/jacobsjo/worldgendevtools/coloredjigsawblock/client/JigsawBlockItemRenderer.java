package eu.jacobsjo.worldgendevtools.coloredjigsawblock.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.FrontAndTop;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class JigsawBlockItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {
    private static final ResourceLocation EMPTY_RESOURCE_LOCATION = new ResourceLocation("empty");
    @Override
    public void render(ItemStack stack, ItemDisplayContext mode, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        CompoundTag tag = stack.getTagElement("BlockEntityTag");
        if (tag == null) {
            JigsawBlockEntityRenderer.render(EMPTY_RESOURCE_LOCATION, EMPTY_RESOURCE_LOCATION, FrontAndTop.NORTH_UP, poseStack, buffer, light, overlay, true);
        } else {
            ResourceLocation name = new ResourceLocation(tag.getString("name"));
            ResourceLocation target = new ResourceLocation(tag.getString("target"));

            JigsawBlockEntityRenderer.render(name, target, FrontAndTop.NORTH_UP, poseStack, buffer, light, overlay, true);
        }
    }
}
