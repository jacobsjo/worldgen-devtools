package eu.jacobsjo.worldgendevtools.profiling.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ChunkgenProfilingInformation {
    private static final Codec<Map<Identifier, Duration>> STATUS_DURATION_CODEC = Codec.simpleMap(Identifier.CODEC, Codec.LONG.xmap(Duration::ofNanos, Duration::toNanos), BuiltInRegistries.CHUNK_STATUS).codec();

    public static final Codec<ChunkgenProfilingInformation> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                STATUS_DURATION_CODEC.fieldOf("status_durations").forGetter((ChunkgenProfilingInformation i) -> i.statusDurations)
            ).apply(instance, ChunkgenProfilingInformation::new)
    );

    private final Map<Identifier, Duration> statusDurations;

    private ChunkgenProfilingInformation(Map<Identifier, Duration> statusDurations){
        this.statusDurations = new HashMap<>(statusDurations);
    }

    public ChunkgenProfilingInformation(){
        this.statusDurations = new HashMap<>();
    }

    public Duration getStatusDuration(Identifier status) {
        Duration duration = this.statusDurations.get(status);
        if (duration == null){
            return Duration.ZERO;
        }
        return duration;
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

    public static ChunkgenProfilingInformation sum(Collection<ChunkgenProfilingInformation> informations){
        Map<Identifier, Duration> durationSums = new HashMap<>();

        BuiltInRegistries.CHUNK_STATUS.keySet().forEach(status -> durationSums.put(status, informations.stream().map(i -> i.getStatusDuration(status)).reduce(Duration.ZERO, Duration::plus)));

        return new ChunkgenProfilingInformation(durationSums);
    }

}
