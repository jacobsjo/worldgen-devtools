package eu.jacobsjo.worldgendevtools.client.locatefeature.impl;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import eu.jacobsjo.util.ColorUtil;
import eu.jacobsjo.worldgendevtools.locatefeature.impl.FeaturePositions;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

import static eu.jacobsjo.worldgendevtools.client.locatefeature.LocateFeatureClientInit.FEATURE_POSITIONS_RENDERER;
import static eu.jacobsjo.worldgendevtools.locatefeature.LocateFeatureInit.FEATURE_POSITION_ATTACHMENT;

@SuppressWarnings("UnstableApiUsage")
public class LocationRenderer {
    private static int RANGE = 2;

    private static final RenderPipeline DEBUG_FILLED_BOX_SEE_THROUGH_PIPELINE = RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation(ResourceLocation.fromNamespaceAndPath("worldgendevtools", "pipeline/debug_filled_box_see_through"))
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_STRIP)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .build();

    private static final RenderType.CompositeRenderType DEBUG_FILLED_BOX_SEE_THROUGH = RenderType.create(
            "debug_filled_box",
            1536,
            false,
            true,
            DEBUG_FILLED_BOX_SEE_THROUGH_PIPELINE,
            RenderType.CompositeState.builder()
                    .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                    .createCompositeState(false)
    );

    public static void render(WorldRenderContext context) {
        if (!Minecraft.getInstance().debugEntries.isCurrentlyEnabled(FEATURE_POSITIONS_RENDERER)) return;
        Entity player = Minecraft.getInstance().getCameraEntity();
        if (player == null) return;
        Vec3 playerPos = player.getPosition(0);
        BlockPos playerBlockPos = BlockPos.containing(playerPos);
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        int centerChunkX = SectionPos.blockToSectionCoord(playerBlockPos.getX());
        int centerChunkZ = SectionPos.blockToSectionCoord(playerBlockPos.getZ());
        for (int x = centerChunkX - RANGE; x <= centerChunkX + RANGE ; x++){
            for (int z = centerChunkZ - RANGE; z <= centerChunkZ + RANGE ; z++) {
                renderLocationsInChunk(context, level, x, z, playerPos);
            }
        }
    }

    private static void renderLocationsInChunk(WorldRenderContext context, ClientLevel level, int chunkX, int chunkZ, Vec3 playerPos){
        ChunkAccess chunk = level.getChunk(chunkX, chunkZ);
        FeaturePositions featurePositions = chunk.getAttached(FEATURE_POSITION_ATTACHMENT);
        if (featurePositions == null) return;

        Object2IntOpenHashMap<BlockPos> posCounts = new Object2IntOpenHashMap<>();

        for (ResourceKey<ConfiguredFeature<?, ?>> key: featurePositions.getFeatureTypes()) {
            List<FeaturePositions.PosAndCount> positions = featurePositions.getPositions(key);
            assert positions != null;

            ColorUtil.RGB color = ColorUtil.randomFromString(key.location().toString());

            positions.forEach(pos -> {
                if (pos.pos().getCenter().closerThan(playerPos, 24)) {
                    int count = posCounts.getOrDefault(pos.pos(), 0);
                    posCounts.put(pos.pos(), count + 1);
                    if (count < 5) {
                        renderDebugBox(context.matrixStack(), context.consumers(), pos.pos(), color.r(), color.g(), color.b(), count);
                    }
                    if (pos.pos().getCenter().closerThan(playerPos, 8)) {
                        if (count < 6) {
                            renderFloatingText(context.matrixStack(), context.consumers(), key.location() + (pos.count() > 1 ? " [x" + pos.count() + "]" : ""), pos.pos().getX() + 0.5, pos.pos().getY() + 0.5, pos.pos().getZ() + 0.5, color.asInt(), 0.015F, true,  -20F - count * 10.0F, true);
                        } else if (count == 6){
                            renderFloatingText(context.matrixStack(), context.consumers(), "[and more]", pos.pos().getX() + 0.5, pos.pos().getY() + 0.5, pos.pos().getZ() + 0.5, 0xD0D0D0, 0.015F, true, -20F - count * 10.0F, true);
                        }
                    }
                }
            });
        }

    }

    private static void renderDebugBox(PoseStack poseStack, MultiBufferSource buffer, BlockPos pos, float red, float green, float blue, int count) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        if (camera.isInitialized()) {
            Vec3 vec3 = camera.getPosition().reverse();
            AABB aABB = new AABB(pos).move(vec3).inflate(-0.4 - count * 0.02);
            VertexConsumer vertexConsumer = buffer.getBuffer(DEBUG_FILLED_BOX_SEE_THROUGH);
            ShapeRenderer.addChainedFilledBoxVertices(poseStack, vertexConsumer, aABB.minX, aABB.minY, aABB.minZ, aABB.maxX, aABB.maxY, aABB.maxZ, red, green, blue, 1.0F);
        }
    }

    public static void renderFloatingText(
            PoseStack poseStack, MultiBufferSource buffer, String text, double x, double y, double z, int color, float scale, boolean bl, float yOffset, boolean transparent
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        Camera camera = minecraft.gameRenderer.getMainCamera();
        if (camera.isInitialized()) {
            Font font = minecraft.font;
            double d = camera.getPosition().x;
            double e = camera.getPosition().y;
            double g = camera.getPosition().z;
            poseStack.pushPose();
            poseStack.translate((float) (x - d), (float) (y - e) + 0.07F, (float) (z - g));
            poseStack.mulPose(camera.rotation());
            poseStack.scale(scale, -scale, scale);
            float h = bl ? (float) (-font.width(text)) / 2.0F : 0.0F;
            font.drawInBatch(
                    text, h, yOffset, 0xFF000000 | color, true, poseStack.last().pose(), buffer, transparent ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, 0, 15728880
            );
            poseStack.popPose();
        }
    }
}
