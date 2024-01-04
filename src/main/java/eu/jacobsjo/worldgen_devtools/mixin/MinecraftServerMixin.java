package eu.jacobsjo.worldgen_devtools.mixin;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import eu.jacobsjo.worldgen_devtools.RegistryResetter;
import eu.jacobsjo.worldgen_devtools.SwitchToConfigurationCallback;
import eu.jacobsjo.worldgen_devtools.UpdatableGeneratorChunkMap;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.network.ServerConnectionListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Shadow
    private LayeredRegistryAccess<RegistryLayer> registries;

    @Shadow @Final private static Logger LOGGER;

    @Shadow public abstract ServerConnectionListener getConnection();

    @Shadow @Final private Map<ResourceKey<Level>, ServerLevel> levels;

    @Inject (method = "method_29437", at = @At("HEAD"))
    private void thenCompose(RegistryAccess.Frozen frozen, ImmutableList<PackResources> immutableList, CallbackInfoReturnable<CompletionStage<?>> cir) {
        CloseableResourceManager resourceManager = new MultiPackResourceManager(PackType.SERVER_DATA, immutableList);

        // Store old dimensions
        RegistryAccess.Frozen dimensionsContextLayer = this.registries.getAccessForLoading(RegistryLayer.DIMENSIONS);
        RegistryAccess.Frozen dimensionsNewLayer = this.registries.getLayer(RegistryLayer.DIMENSIONS);
        RegistryOps.RegistryInfoLookup dimensionsLookup = getRegistrtyInfoLookup(dimensionsContextLayer, dimensionsNewLayer, false);

        Map<ResourceLocation, JsonElement> generators = new HashMap<>();
        this.levels.forEach((key, level) -> generators.put(key.location(), ChunkGenerator.CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE, dimensionsLookup), level.getChunkSource().getGenerator()).getOrThrow(false, err -> LOGGER.error(err))));


        // Reload Worldgen registries
        Map<ResourceKey<?>, Exception> exceptionMap = new HashMap<>();

        RegistryAccess.Frozen worldgenContextLayer = this.registries.getAccessForLoading(RegistryLayer.WORLDGEN);
        RegistryAccess.Frozen worldgenNewLayer = this.registries.getLayer(RegistryLayer.WORLDGEN);
        RegistryOps.RegistryInfoLookup worldgenLookup = getRegistrtyInfoLookup(worldgenContextLayer, worldgenNewLayer, true);

        RegistryDataLoader.WORLDGEN_REGISTRIES.forEach((RegistryDataLoader.RegistryData<?> data) -> loadData(worldgenLookup, resourceManager, data, worldgenNewLayer, exceptionMap));

        if (!exceptionMap.isEmpty()) {
            logErrors(exceptionMap);
            throw new IllegalStateException("Failed to load registries due to above errors");
        }

        // Reload Dimension registry
        MappedRegistry<LevelStem> levelStemRegistry = new MappedRegistry<>(Registries.LEVEL_STEM, Lifecycle.stable());
        RegistryDataLoader.loadRegistryContents(dimensionsLookup, resourceManager, Registries.LEVEL_STEM, levelStemRegistry, LevelStem.CODEC, exceptionMap);

        Stream<ResourceLocation> dimensionKeys = Stream.concat(levelStemRegistry.keySet().stream(), generators.keySet().stream()).distinct();

        dimensionKeys.forEach(key -> {
            levelStemRegistry.getOptional(key).map(LevelStem::generator) .or(() -> {
                JsonElement json = generators.get(key);
                if (json == null) return Optional.empty();
                return ChunkGenerator.CODEC.parse(RegistryOps.create(JsonOps.INSTANCE, worldgenLookup), json).result();
            }).ifPresent(chunkGenerator -> {
                ServerLevel level = this.levels.get(ResourceKey.create(Registries.DIMENSION, key));
                if (level == null){
                    LOGGER.warn("adding new dimension not supported; trying to add {}", key);
                    return;
                }
                ChunkMap chunkMap = level.getChunkSource().chunkMap;
                ((UpdatableGeneratorChunkMap) chunkMap).worldgenDevtools$setGenerator(chunkGenerator);
            });
        });

        /*
        generators.forEach((key, json) -> {
            DataResult<ChunkGenerator> dataResult2 = json.flatMap((jsonElement) -> {
                return ChunkGenerator.CODEC.parse(RegistryOps.create(JsonOps.INSTANCE, worldgenLookup), jsonElement);
            });
            dataResult2.result().ifPresent((chunkGenerator) -> {
                ChunkMap chunkMap = this.levels.get(key).getChunkSource().chunkMap;
                ((UpdatableGeneratorChunkMap) chunkMap).worldgenDevtools$setGenerator(chunkGenerator);
            });

        } );
         */

        syncClient();

    }

    @Unique
    private static <T> void loadData(
            RegistryOps.RegistryInfoLookup registryInfoLookup,
            ResourceManager resourceManager,
            RegistryDataLoader.RegistryData<T> data,
            RegistryAccess.Frozen layer,
            Map<ResourceKey<?>, Exception> exceptionMap
    ){
        Optional<Registry<T>> registry = layer.registry(data.key());
        if (registry.isEmpty()){
            exceptionMap.put(data.key(), new Exception("Registry doesn't exist"));
            return;
        }

        if (!(registry.get() instanceof WritableRegistry<T>)){
            exceptionMap.put(data.key(), new Exception("Registry is not writable"));
            return;
        }

        RegistryDataLoader.loadRegistryContents(registryInfoLookup, resourceManager, data.key(), (WritableRegistry<T>) registry.get(), data.elementCodec(), exceptionMap);
    }


    @Unique
    private static <T> RegistryOps.RegistryInfo<T> createInfoForNewRegistry(WritableRegistry<T> writableRegistry) {
        return new RegistryOps.RegistryInfo<>(writableRegistry.asLookup(), writableRegistry.createRegistrationLookup(), writableRegistry.registryLifecycle());
    }

    @Unique
    private static <T> RegistryOps.RegistryInfo<T> createInfoForContextRegistry(Registry<T> registry) {
        return new RegistryOps.RegistryInfo<>(registry.asLookup(), registry.asTagAddingLookup(), registry.registryLifecycle());
    }


    @Unique
    private RegistryOps.RegistryInfoLookup getRegistrtyInfoLookup(RegistryAccess.Frozen contextLayer, RegistryAccess.Frozen newLayer, boolean reset){

        final Map<ResourceKey<? extends Registry<?>>, RegistryOps.RegistryInfo<?>> lookupMap = new HashMap<>();
        contextLayer.registries().forEach((registryEntry) -> {
            lookupMap.put(registryEntry.key(), createInfoForContextRegistry(registryEntry.value()));
        });

        newLayer.registries().forEach((registryEntry) -> {
            assert registryEntry.value() instanceof MappedRegistry;
            MappedRegistry<?> registry = (MappedRegistry<?>) registryEntry.value();
            if (reset) {
                ((RegistryResetter) registry).reset();
                lookupMap.put(registryEntry.key(), createInfoForNewRegistry(registry));
            } else {
                lookupMap.put(registryEntry.key(), createInfoForContextRegistry(registryEntry.value()));
            }
        });

        return new RegistryOps.RegistryInfoLookup() {
            public <E> Optional<RegistryOps.RegistryInfo<E>> lookup(ResourceKey<? extends Registry<? extends E>> resourceKey) {
                return Optional.ofNullable((RegistryOps.RegistryInfo<E>) lookupMap.get(resourceKey));
            }
        };
    }

    @Unique
    private void syncClient() {
        for (Connection connection : getConnection().getConnections()) {
            PacketListener var5 = connection.getPacketListener();
            if (var5 instanceof ServerGamePacketListenerImpl impl) {
                ((SwitchToConfigurationCallback) impl).worldgenDevtools$onSwitchToConfiguration(() -> {
                    PacketListener listener = connection.getPacketListener();
                    if (listener instanceof ServerConfigurationPacketListenerImpl impl2) {
                        impl2.startConfiguration();
                    }
                });
                impl.switchToConfig();
            }
        }

    }

    @Unique
    private static void logErrors(Map<ResourceKey<?>, Exception> map) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        Map<ResourceLocation, Map<ResourceLocation, Exception>> map2 = map.entrySet().stream().collect(Collectors.groupingBy((entry) -> {
            return ((ResourceKey)entry.getKey()).registry();
        }, Collectors.toMap((entry) -> {
            return ((ResourceKey)entry.getKey()).location();
        }, Map.Entry::getValue)));
        map2.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach((entry) -> {
            printWriter.printf("> Errors in registry %s:%n", entry.getKey());
            (entry.getValue()).entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach((entryx) -> {
                printWriter.printf(">> Errors in element %s:%n", entryx.getKey());
                ((Exception)entryx.getValue()).printStackTrace(printWriter);
            });
        });
        printWriter.flush();
        LOGGER.error((String)"Registry loading errors:\n{}", (StringWriter)stringWriter);
    }


}
