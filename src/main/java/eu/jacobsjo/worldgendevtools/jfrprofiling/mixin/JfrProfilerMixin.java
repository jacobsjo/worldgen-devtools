package eu.jacobsjo.worldgendevtools.jfrprofiling.mixin;

import eu.jacobsjo.worldgendevtools.jfrprofiling.api.FeatureGenerationEvent;
import jdk.jfr.Event;
import net.minecraft.util.profiling.jfr.JfrProfiler;
import net.minecraft.util.profiling.jfr.event.*;
import org.spongepowered.asm.mixin.*;

import java.util.List;

@Mixin(JfrProfiler.class)
public class JfrProfilerMixin {

    @Shadow
    @Final
    @Mutable
    private static final List<Class<? extends Event>> CUSTOM_EVENTS = List.of(
            ChunkGenerationEvent.class,
            ChunkRegionReadEvent.class,
            ChunkRegionWriteEvent.class,
            PacketReceivedEvent.class,
            PacketSentEvent.class,
            NetworkSummaryEvent.class,
            ServerTickTimeEvent.class,
            WorldLoadFinishedEvent.class,
            FeatureGenerationEvent.class
    );
}
