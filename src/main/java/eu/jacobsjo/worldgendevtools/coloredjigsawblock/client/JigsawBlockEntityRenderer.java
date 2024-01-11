package eu.jacobsjo.worldgendevtools.coloredjigsawblock.client;

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
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("worldgendevtools","textures/entity/jigsaw.png");
    private static final RenderType RENDER_TYPE = RenderType.entityTranslucentCull(TEXTURE_LOCATION);

    private static final float OFFSET = 0.001f;

    private static final ResourceLocation EMPTY_RESOURCE_LOCATION = new ResourceLocation("empty");
    BlockEntityRendererProvider.Context context;

    private static final ModelPart modelPart = new MeshDefinition().getRoot().addOrReplaceChild("cube", CubeListBuilder.create().texOffs(0, 0).addBox(0f, 0f, 0f, 16f, 16f, 16f), PartPose.ZERO).bake(64, 64);
    public JigsawBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public void render(JigsawBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {

        JigsawBlockEntityRenderer.render(blockEntity.getName(), blockEntity.getTarget(), blockEntity.getBlockState().getValue(JigsawBlock.ORIENTATION), poseStack, buffer, packedOverlay, false);
    }

    public static void render(ResourceLocation name, ResourceLocation target, FrontAndTop orientation, PoseStack poseStack, MultiBufferSource buffer, int packedOverlay, boolean renderEmpty) {
        ResourceLocation location = name.equals(EMPTY_RESOURCE_LOCATION) ? target : name;

        if (location.equals(EMPTY_RESOURCE_LOCATION)) {
            if (renderEmpty){
                render(0.843f, 0.761f, 0.843f, orientation, poseStack, buffer, packedOverlay);
            }
            return;
        }

        int hash = location.toString().hashCode();
        float r = ((float) (hash & 0xFF)) / 0xFF;
        float g = ((float) (hash >> 8 & 0xFF)) / 0xFF;
        float b = ((float) (hash >> 16 & 0xFF)) / 0xFF;

        render(r, g, b, orientation, poseStack, buffer, packedOverlay);
    }

    public static void render(float r, float g, float b, FrontAndTop orientation, PoseStack poseStack, MultiBufferSource buffer, int packedOverlay){
        poseStack.pushPose();
        poseStack.scale(1 + OFFSET * 2, 1 + OFFSET * 2, 1 + OFFSET * 2);
        poseStack.translate(-OFFSET, -OFFSET, -OFFSET);
        poseStack.rotateAround(getRotation(orientation), 0.5f, 0.5f, 0.5f);
        modelPart.render(poseStack, buffer.getBuffer(RENDER_TYPE), 0xF000F0, packedOverlay, r, g, b, 1.0f);
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
