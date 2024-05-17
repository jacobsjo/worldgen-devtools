package eu.jacobsjo.worldgendevtools.jfrprofiling.api;

import jdk.jfr.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.jfr.JfrProfiler;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.GenerationStep;

import java.util.Locale;

@Name(FeatureGenerationEvent.EVENT_NAME)
@Label("Feature Placement")
@Category({JfrProfiler.ROOT_CATEGORY, JfrProfiler.WORLD_GEN_CATEGORY})
public class FeatureGenerationEvent extends Event {
    public static final String EVENT_NAME = "minecraft.FeaturePlacement";
    public static final EventType TYPE = EventType.getEventType(FeatureGenerationEvent.class);

    @Name(value="chunkPosX")
    @Label(value="Chunk X Position")
    public final int chunkPosX;

    @Name(value="chunkPosZ")
    @Label(value="Chunk Z Position")
    public final int chunkPosZ;

    @Name(value="level")
    @Label(value="Level")
    public final String level;

    @Name(value="step")
    @Label(value="Step")
    public final int step;

    @Name(value="stepName")
    @Label(value="Step Name")
    public final String stepName;

    @Name(value="placedFeature")
    @Label(value="Placed Feature")
    public final String placedFeature;

    public FeatureGenerationEvent(ChunkPos chunkPos, ResourceKey<Level> level, String placedFeature, int step) {
        this.chunkPosX = chunkPos.x;
        this.chunkPosZ = chunkPos.z;
        this.level = level.location().toString();
        this.placedFeature = placedFeature;
        this.step = step;
        this.stepName = GenerationStep.Decoration.values()[step].name().toLowerCase(Locale.ROOT);
    }
}
