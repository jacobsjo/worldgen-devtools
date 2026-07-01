package eu.jacobsjo.worldgendevtools.client.locatefeature.impl;

import eu.jacobsjo.util.ColorUtil;
import eu.jacobsjo.worldgendevtools.locatefeature.impl.FeaturePositions;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.TextGizmo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.debug.DebugValueAccess;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.phys.Vec3;

import java.util.List;

import static eu.jacobsjo.worldgendevtools.locatefeature.LocateFeatureInit.FEATURE_POSITION_ATTACHMENT;

public class LocationRenderer implements DebugRenderer.SimpleDebugRenderer{
    private static final int RANGE = 2;

    @Override
    public void emitGizmos(double d, double e, double f, DebugValueAccess debugValueAccess, Frustum frustum, float g) {
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
                renderLocationsInChunk(level, x, z, playerPos);
            }
        }
    }

    private void renderLocationsInChunk(ClientLevel level, int chunkX, int chunkZ, Vec3 playerPos){
        ChunkAccess chunk = level.getChunk(chunkX, chunkZ);
        FeaturePositions featurePositions = chunk.getAttached(FEATURE_POSITION_ATTACHMENT);
        if (featurePositions == null) return;

        Object2IntOpenHashMap<BlockPos> posCounts = new Object2IntOpenHashMap<>();

        for (ResourceKey<Feature> key: featurePositions.getFeatureTypes()) {
            List<FeaturePositions.PosAndCount> positions = featurePositions.getPositions(key);

            ColorUtil.RGB color = ColorUtil.randomFromString(key.identifier().toString());

            positions.forEach(pos -> {
                if (pos.pos().distToCenterSqr(playerPos) <= 24 * 24) {
                    int count = posCounts.getOrDefault(pos.pos(), 0);
                    posCounts.put(pos.pos(), count + 1);
                    if (count < 5) {
                        int alpha = 255/(count + 1);
                        Gizmos.cuboid(
                                pos.pos(),
                                -0.4f,
                                GizmoStyle.fill((alpha << 24) | color.asInt())
                        ).setAlwaysOnTop();
                    }
                    if (pos.pos().distToCenterSqr(playerPos) <= 8 * 8 && count <= 6) {
                        String text = count == 6 ? "[and more]" : key.identifier() + (pos.count() > 1 ? " [x" + pos.count() + "]" : "");
                        Gizmos.billboardText(
                                text,
                                new Vec3(pos.pos().getX() + 0.5, pos.pos().getY() + 0.5 + 0.28 + 0.13 * count, pos.pos().getZ() + 0.5),
                                TextGizmo.Style.forColorAndCentered(0xFF000000 | color.asInt()).withScale(0.2f)
                            ).setAlwaysOnTop();
                    }
                }
            });
        }

    }
}
