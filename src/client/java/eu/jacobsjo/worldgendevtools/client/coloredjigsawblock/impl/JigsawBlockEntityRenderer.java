package eu.jacobsjo.worldgendevtools.client.coloredjigsawblock.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.FrontAndTop;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

public class JigsawBlockEntityRenderer implements BlockEntityRenderer<JigsawBlockEntity, JigsawBlockRenderState> {
    private static final Identifier TEXTURE_LOCATION = Identifier.fromNamespaceAndPath("worldgendevtools","textures/entity/jigsaw.png");
    private static final RenderType RENDER_TYPE = RenderTypes.entityTranslucent(TEXTURE_LOCATION);
    private static final float OFFSET = 0.001f;
    private static final Identifier EMPTY_RESOURCE_LOCATION = Identifier.withDefaultNamespace("empty");

    private static final ModelPart modelPart = new MeshDefinition().getRoot().addOrReplaceChild("cube", CubeListBuilder.create().texOffs(0, 0).addBox(0f, 0f, 0f, 16f, 16f, 16f), PartPose.ZERO).bake(64, 64);
    public JigsawBlockEntityRenderer(@SuppressWarnings("unused") BlockEntityRendererProvider.Context conext) { }

    @Override
    public boolean shouldRender(JigsawBlockEntity blockEntity, Vec3 cameraPos) {
        if (!BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos)) return false;

        return !blockEntity.getName().equals(EMPTY_RESOURCE_LOCATION) || !blockEntity.getTarget().equals(EMPTY_RESOURCE_LOCATION);
    }

    @Override
    public void submit(JigsawBlockRenderState blockEntityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        poseStack.scale(1 + OFFSET * 2, 1 + OFFSET * 2, 1 + OFFSET * 2);
        poseStack.translate(-OFFSET, -OFFSET, -OFFSET);
        poseStack.rotateAround(getRotation(blockEntityRenderState.blockState.getValue(JigsawBlock.ORIENTATION)), 0.5f, 0.5f, 0.5f);
        submitNodeCollector.submitModelPart(
                modelPart,
                poseStack,
                RENDER_TYPE,
                0xF000F0,
                OverlayTexture.NO_OVERLAY,
                null,
                blockEntityRenderState.argb,
                blockEntityRenderState.breakProgress);
        poseStack.popPose();
    }

    @Override
    public @NotNull JigsawBlockRenderState createRenderState() {
        return new JigsawBlockRenderState();
    }

    @Override
    public void extractRenderState(JigsawBlockEntity jigsawBlockEntity, JigsawBlockRenderState jigsawBlockRenderState, float f, Vec3 vec3, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(jigsawBlockEntity, jigsawBlockRenderState, f, vec3, crumblingOverlay);

        Identifier location = jigsawBlockEntity.getName().equals(EMPTY_RESOURCE_LOCATION) ? jigsawBlockEntity.getTarget() : jigsawBlockEntity.getName();

        int hash = location.toString().hashCode();
        int r = hash & 0xFF;
        int g = hash >> 8 & 0xFF;
        int b = hash >> 16 & 0xFF;
        jigsawBlockRenderState.argb = 0xFF000000 | r << 16 | g << 8 | b;
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
