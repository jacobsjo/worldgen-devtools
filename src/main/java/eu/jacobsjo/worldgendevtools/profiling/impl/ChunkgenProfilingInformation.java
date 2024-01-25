package eu.jacobsjo.worldgendevtools.profiling.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.ChunkStatus;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class ChunkgenProfilingInformation {
    private static final Codec<Map<ResourceLocation, Duration>> STATUS_DURATION_CODEC = Codec.simpleMap(ResourceLocation.CODEC, Codec.LONG.xmap(Duration::ofNanos, Duration::toNanos), BuiltInRegistries.CHUNK_STATUS).codec();

    public static final Codec<ChunkgenProfilingInformation> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                STATUS_DURATION_CODEC.fieldOf("status_durations").forGetter((ChunkgenProfilingInformation i) -> i.statusDurations)
            ).apply(instance, ChunkgenProfilingInformation::new)
    );

    private final Map<ResourceLocation, Duration> statusDurations;

    private ChunkgenProfilingInformation(Map<ResourceLocation, Duration> statusDurations){
        this.statusDurations = new HashMap<>(statusDurations);
    }

    public ChunkgenProfilingInformation(){
        this.statusDurations = new HashMap<>();
    }

    public Duration getStatusDurection(ChunkStatus status){
        return this.statusDurations.get(BuiltInRegistries.CHUNK_STATUS.getKey(status));
    }

    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        this.statusDurations.forEach((key, value) -> stringBuilder.append(String.format("%s: %.2fms\n", key.toString(), value.toNanos() * 1e-6d)));
        return stringBuilder.toString();
    }

    public void setStatusDurection(ChunkStatus status, Duration duration){
        this.statusDurations.put(BuiltInRegistries.CHUNK_STATUS.getKey(status), duration);
    }

}
