package eu.jacobsjo.worldgendevtools.client.coloredjigsawblock.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.FrontAndTop;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import org.joml.Quaternionf;

public class JigsawBlockEntityRenderer implements BlockEntityRenderer<JigsawBlockEntity> {
    private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.fromNamespaceAndPath("worldgendevtools","textures/entity/jigsaw.png");
    private static final RenderType RENDER_TYPE = RenderType.entityTranslucent(TEXTURE_LOCATION);
    private static final float OFFSET = 0.001f;
    private static final ResourceLocation EMPTY_RESOURCE_LOCATION = ResourceLocation.withDefaultNamespace("empty");

    private static final ModelPart modelPart = new MeshDefinition().getRoot().addOrReplaceChild("cube", CubeListBuilder.create().texOffs(0, 0).addBox(0f, 0f, 0f, 16f, 16f, 16f), PartPose.ZERO).bake(64, 64);
    public JigsawBlockEntityRenderer(@SuppressWarnings("unused") BlockEntityRendererProvider.Context conext) { }

    @Override
    public void render(JigsawBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {

        JigsawBlockEntityRenderer.render(blockEntity.getName(), blockEntity.getTarget(), blockEntity.getBlockState().getValue(JigsawBlock.ORIENTATION), poseStack, buffer, packedOverlay);
    }

    public static void render(ResourceLocation name, ResourceLocation target, FrontAndTop orientation, PoseStack poseStack, MultiBufferSource buffer, int packedOverlay) {
        ResourceLocation location = name.equals(EMPTY_RESOURCE_LOCATION) ? target : name;

        if (location.equals(EMPTY_RESOURCE_LOCATION)) {
            return;
        }

        int hash = location.toString().hashCode();
        int r = hash & 0xFF;
        int g = hash >> 8 & 0xFF;
        int b = hash >> 16 & 0xFF;

        render(0xFF000000 | r << 16 | g << 8 | b, orientation, poseStack, buffer, packedOverlay);
    }

    public static void render(int argb, FrontAndTop orientation, PoseStack poseStack, MultiBufferSource buffer, int packedOverlay){
        poseStack.pushPose();
        poseStack.scale(1 + OFFSET * 2, 1 + OFFSET * 2, 1 + OFFSET * 2);
        poseStack.translate(-OFFSET, -OFFSET, -OFFSET);
        poseStack.rotateAround(getRotation(orientation), 0.5f, 0.5f, 0.5f);
        modelPart.render(poseStack, buffer.getBuffer(RENDER_TYPE), 0xF000F0, packedOverlay, argb);
        poseStack.popPose();
    }

    @Override
    public int getViewDistance() {
        return 128;
    }

    private static Quaternionf getRotation(FrontAndTop frontAndTop){
        return switch (frontAndTop.front()) {
            case UP -> switch (frontAndTop.top()) {
                case UP, DOWN -> throw new IllegalStateException();
                case NORTH -> new Quaternionf();
                case SOUTH -> new Quaternionf().rotationY((float)Math.PI);
                case WEST -> new Quaternionf().rotationY(1.5707964f);
                case EAST -> new Quaternionf().rotationY( -1.5707964f);
            };
            case DOWN -> switch (frontAndTop.top()) {
                case UP, DOWN -> throw new IllegalStateException();
                case NORTH -> new Quaternionf().rotationXYZ((float)Math.PI, (float)Math.PI, 0.0f);
                case SOUTH -> new Quaternionf().rotationXYZ((float)Math.PI, 0.0f, 0.0f);
                case WEST -> new Quaternionf().rotationXYZ((float)Math.PI, 1.5707964f, 0.0f);
                case EAST -> new Quaternionf().rotationXYZ((float)Math.PI,  -1.5707964f, 0.0f);
            };
            case NORTH -> new Quaternionf().rotationXYZ(1.5707964f, 0.0f, (float) Math.PI);
            case SOUTH -> new Quaternionf().rotationX(1.5707964f);
            case WEST -> new Quaternionf().rotationXYZ(1.5707964f, 0.0f, 1.5707964f);
            case EAST -> new Quaternionf().rotationXYZ(1.5707964f, 0.0f, -1.5707964f);
        };

    }
}
