package eu.jacobsjo.worldgendevtools.reloadregistries.impl;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import eu.jacobsjo.worldgendevtools.reloadregistries.api.ReloadableRegistry;
import eu.jacobsjo.worldgendevtools.reloadregistries.api.SwitchToConfigurationCallback;
import eu.jacobsjo.worldgendevtools.reloadregistries.api.UpdatableGeneratorChunkMap;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.network.ServerConnectionListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.config.JoinWorldTask;
import net.minecraft.server.network.config.SynchronizeRegistriesTask;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RegistryReloader {
    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Reloades the registries.
     * @param registries the RegistryAccess from the server
     * @param levels map of levels to recreate the generatores for
     * @param resources datapack resources
     */
    public static void reloadRegistries(LayeredRegistryAccess<RegistryLayer> registries, Map<ResourceKey<Level>, ServerLevel> levels, ImmutableList<PackResources> resources){
        CloseableResourceManager resourceManager = new MultiPackResourceManager(PackType.SERVER_DATA, resources);

        // Store old dimensions
        RegistryAccess.Frozen dimensionsContextLayer = registries.getAccessForLoading(RegistryLayer.DIMENSIONS);
        RegistryAccess.Frozen dimensionsNewLayer = registries.getLayer(RegistryLayer.DIMENSIONS);
        RegistryOps.RegistryInfoLookup dimensionsLookup = getRegistrtyInfoLookup(dimensionsContextLayer, dimensionsNewLayer, false);

        Map<ResourceLocation, JsonElement> generators = new HashMap<>();
        levels.forEach((key, level) -> generators.put(key.location(), ChunkGenerator.CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE, dimensionsLookup), level.getChunkSource().getGenerator()).getOrThrow()));


        // Reload Worldgen registries
        Map<ResourceKey<?>, Exception> exceptionMap = new HashMap<>();

        RegistryAccess.Frozen worldgenContextLayer = registries.getAccessForLoading(RegistryLayer.WORLDGEN);
        RegistryAccess.Frozen worldgenNewLayer = registries.getLayer(RegistryLayer.WORLDGEN);
        RegistryOps.RegistryInfoLookup worldgenLookup = getRegistrtyInfoLookup(worldgenContextLayer, worldgenNewLayer, true);

        RegistryDataLoader.WORLDGEN_REGISTRIES.forEach((RegistryDataLoader.RegistryData<?> data) -> loadData(worldgenLookup, resourceManager, data, worldgenNewLayer, exceptionMap));

        if (!exceptionMap.isEmpty()) {
            logErrors(exceptionMap);
            throw new IllegalStateException("Failed to load registries due to above errors");
        }

        // Reload Dimension registry
        MappedRegistry<LevelStem> levelStemRegistry = new MappedRegistry<>(Registries.LEVEL_STEM, Lifecycle.stable());
        RegistryDataLoader.loadContentsFromManager(resourceManager, dimensionsLookup, levelStemRegistry, LevelStem.CODEC, exceptionMap);

        // combine dimensions loaded from datapack with already existing dimensions (prioritize new)
        Stream<ResourceLocation> dimensionKeys = Stream.concat(levelStemRegistry.keySet().stream(), generators.keySet().stream()).distinct();

        //for each found dimension, create a generator and set it for the dimension, if one exists. Adding new dimensions isn't supported.
        dimensionKeys.forEach(key -> levelStemRegistry.getOptional(key).map(LevelStem::generator).or(() -> {
            JsonElement json = generators.get(key);
            if (json == null) return Optional.empty();
            return ChunkGenerator.CODEC.parse(RegistryOps.create(JsonOps.INSTANCE, worldgenLookup), json).result();
        }).ifPresent(chunkGenerator -> {
            ServerLevel level = levels.get(ResourceKey.create(Registries.DIMENSION, key));
            if (level == null){
                LOGGER.warn("adding new dimension not supported; trying to add {}", key);
                return;
            }
            ChunkMap chunkMap = level.getChunkSource().chunkMap;
            ((UpdatableGeneratorChunkMap) chunkMap).worldgenDevtools$setGenerator(chunkGenerator);
        }));
    }

    /**
     * Switches all clients to configuration phase, then sends new registries and switches back to gameplay.
     * @param serverConnection the server connection handler
     */
    public static void syncClient(ServerConnectionListener serverConnection) {
        for (Connection connection : serverConnection.getConnections()) {
            PacketListener var5 = connection.getPacketListener();
            if (var5 instanceof ServerGamePacketListenerImpl impl) {

                ((SwitchToConfigurationCallback) impl).worldgenDevtools$onSwitchToConfiguration(() -> {
                    PacketListener listener = connection.getPacketListener();
                    if (listener instanceof ServerConfigurationPacketListenerImpl impl2) {
                        LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess = serverConnection.getServer().registries();

                        List<KnownPack> list = serverConnection.getServer().getResourceManager().listPacks().flatMap((packResources) -> {
                            return packResources.location().knownPackInfo().stream();
                        }).toList();

                        impl2.synchronizeRegistriesTask = new SynchronizeRegistriesTask(list, layeredRegistryAccess);
                        impl2.configurationTasks.add(impl2.synchronizeRegistriesTask);
                        impl2.configurationTasks.add(new JoinWorldTask());
                        impl2.startNextTask();
                    }
                });
                impl.switchToConfig();
            }
        }
    }

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

        RegistryDataLoader.loadContentsFromManager(resourceManager, registryInfoLookup, (WritableRegistry<T>) registry.get(), data.elementCodec(), exceptionMap);
    }

    private static <T> RegistryOps.RegistryInfo<T> createInfoForNewRegistry(WritableRegistry<T> writableRegistry) {
        return new RegistryOps.RegistryInfo<>(writableRegistry.asLookup(), writableRegistry.createRegistrationLookup(), writableRegistry.registryLifecycle());
    }

    private static <T> RegistryOps.RegistryInfo<T> createInfoForContextRegistry(Registry<T> registry) {
        return new RegistryOps.RegistryInfo<>(registry.asLookup(), registry.asTagAddingLookup(), registry.registryLifecycle());
    }

    private static RegistryOps.RegistryInfoLookup getRegistrtyInfoLookup(RegistryAccess.Frozen contextLayer, RegistryAccess.Frozen newLayer, boolean reset){

        final Map<ResourceKey<? extends Registry<?>>, RegistryOps.RegistryInfo<?>> lookupMap = new HashMap<>();
        contextLayer.registries().forEach((registryEntry) -> lookupMap.put(registryEntry.key(), createInfoForContextRegistry(registryEntry.value())));

        newLayer.registries().forEach((registryEntry) -> {
            assert registryEntry.value() instanceof MappedRegistry;
            MappedRegistry<?> registry = (MappedRegistry<?>) registryEntry.value();
            if (reset) {
                ((ReloadableRegistry) registry).worldgenDevtools$startReload();
                lookupMap.put(registryEntry.key(), createInfoForNewRegistry(registry));
            } else {
                lookupMap.put(registryEntry.key(), createInfoForContextRegistry(registryEntry.value()));
            }
        });

        return new RegistryOps.RegistryInfoLookup() {
            public <E> @NotNull Optional<RegistryOps.RegistryInfo<E>> lookup(ResourceKey<? extends Registry<? extends E>> resourceKey) {
                return Optional.ofNullable((RegistryOps.RegistryInfo<E>) lookupMap.get(resourceKey));
            }
        };
    }


    private static void logErrors(Map<ResourceKey<?>, Exception> map) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        Map<ResourceLocation, Map<ResourceLocation, Exception>> map2 = map.entrySet().stream().collect(Collectors.groupingBy((entry) -> (entry.getKey()).registry(), Collectors.toMap((entry) -> (entry.getKey()).location(), Map.Entry::getValue)));
        map2.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach((entry) -> {
            printWriter.printf("> Errors in registry %s:%n", entry.getKey());
            (entry.getValue()).entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach((entryx) -> {
                printWriter.printf(">> Errors in element %s:%n", entryx.getKey());
                (entryx.getValue()).printStackTrace(printWriter);
            });
        });
        printWriter.flush();
        LOGGER.error("Registry loading errors:\n{}", stringWriter);
    }


}
