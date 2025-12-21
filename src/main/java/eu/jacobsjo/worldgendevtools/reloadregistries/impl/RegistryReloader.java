package eu.jacobsjo.worldgendevtools.reloadregistries.impl;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import eu.jacobsjo.worldgendevtools.reloadregistries.api.ReloadableRegistry;
import eu.jacobsjo.worldgendevtools.reloadregistries.api.UpdatableGeneratorChunkMap;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.*;
import net.minecraft.core.registries.ConcurrentHolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerConnectionListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagLoader;
import net.minecraft.util.Util;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

public class RegistryReloader {
    public static final Logger LOGGER = LogUtils.getLogger();

    private static class ResourceManagerRegistryReloadTask<T> extends RegistryDataLoader.ResourceManagerRegistryLoadTask<T> {

        private ResourceManagerRegistryReloadTask(
                final RegistryDataLoader.RegistryData<T> data,
                final Lifecycle lifecycle,
                final Map<ResourceKey<?>, Exception> loadingErrors,
                final ResourceManager resourceManager,
                RegistryAccess.Frozen reloadedLayer
        ) {
            super(data, lifecycle, loadingErrors, resourceManager);
            Registry<T> registry = reloadedLayer.lookup(data.key()).orElseThrow();
            if (registry instanceof WritableRegistry<T> writableRegistry) {
                this.registry = writableRegistry;
                ((ReloadableRegistry) this.registry).worldgenDevtools$startReload();
                this.concurrentRegistrationGetter = new ConcurrentHolderGetter<>(this.registryWriteLock, this.registry.createRegistrationLookup());
            } else {
                throw new IllegalStateException("Registry is not writable");
            }
        }
    }

    /**
     * Reloades the registries.
     *
     * @param registries  the RegistryAccess from the server
     * @param levels      map of levels to recreate the generatores for
     * @param packsToLoad the packs to load from
     * @return CompletableFuture that completes when the reload is done
     */
    public static CompletableFuture<Object> reloadRegistries(
            LayeredRegistryAccess<RegistryLayer> registries,
            Map<ResourceKey<Level>, ServerLevel> levels,
            ImmutableList<PackResources> packsToLoad
        ){

        final Executor backgroundExecutor = Util.backgroundExecutor();
        LOGGER.info("Reloading registries");

        CloseableResourceManager resources = new MultiPackResourceManager(PackType.SERVER_DATA, packsToLoad);

        // Store old dimensions
        RegistryAccess.Frozen dimensionsContextLayer = registries.getAccessForLoading(RegistryLayer.DIMENSIONS);
        HolderLookup.Provider oldDimensionContextProvider = HolderLookup.Provider.create(dimensionsContextLayer.listRegistries());
        Map<Identifier, JsonElement> generators = new HashMap<>();
        levels.forEach((key, level) -> generators.put(key.identifier(), ChunkGenerator.CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE, oldDimensionContextProvider), level.getChunkSource().getGenerator()).getOrThrow()));

        // Reload Worldgen registries
        LayeredRegistryAccess<RegistryLayer> initialLayers = RegistryLayer.createRegistryAccess();
        List<Registry.PendingTags<?>> staticLayerTags = TagLoader.loadTagsForExistingRegistries(resources, initialLayers.getLayer(RegistryLayer.STATIC));
        RegistryAccess.Frozen worldgenLoadContext = registries.getAccessForLoading(RegistryLayer.WORLDGEN);
        List<HolderLookup.RegistryLookup<?>> worldgenContextRegistries = TagLoader.buildUpdatedLookups(worldgenLoadContext, staticLayerTags);

        RegistryAccess.Frozen worldgenLayerToReload = registries.getLayer(RegistryLayer.WORLDGEN);

        return reloadData(resources, worldgenContextRegistries, RegistryDataLoader.WORLDGEN_REGISTRIES, backgroundExecutor, worldgenLayerToReload)
                .thenComposeAsync(loadedWorldgenRegistries -> {
                    List<HolderLookup.RegistryLookup<?>> dimensionContextRegistries = Stream.concat(
                                    worldgenContextRegistries.stream(), loadedWorldgenRegistries.listRegistries()
                            )
                            .toList();
                    return RegistryDataLoader.load(resources, dimensionContextRegistries, RegistryDataLoader.DIMENSION_REGISTRIES, backgroundExecutor)
                            .thenApply(newDimensionRegistires -> {
                                Registry<LevelStem> newLevelStemRegistry = (Registry<LevelStem>) newDimensionRegistires.registries().findAny().orElseThrow().value();

                                HolderLookup.Provider dimensionContextProvider = HolderLookup.Provider.create(dimensionContextRegistries.stream());

                                reloadDimensions(levels, newLevelStemRegistry, generators, dimensionContextProvider);
                                return null;
                            });
                });
    }

    /// combine dimensions loaded from datapack with already existing dimensions (prioritize new)
    private static void reloadDimensions(
            Map<ResourceKey<Level>, ServerLevel> levels,
            Registry<LevelStem> newLevelStems,
            Map<Identifier, JsonElement> oldGenerators,
            HolderLookup.Provider dimensionContextProvider
        ){
        Stream<Identifier> dimensionKeys = Stream.concat(newLevelStems.registryKeySet().stream().map(ResourceKey::identifier), oldGenerators.keySet().stream()).distinct();

        //for each found dimension, create a generator and set it for the dimension, if one exists. Adding new dimensions isn't supported.
        dimensionKeys.forEach(key -> {
            LOGGER.info("Reloading dimension: {}", key);

            ServerLevel level = levels.get(ResourceKey.create(Registries.DIMENSION, key));
            if (level == null) {
                LOGGER.warn("adding new dimension not supported; trying to add {}", key);
                return;
            }

            Optional<LevelStem> levelStem = newLevelStems.getOptional(key);

            ChunkGenerator chunkGenerator;
            if (levelStem.isPresent()) {
                if (level.dimensionType().minY() != levelStem.get().type().value().minY() || level.dimensionType().height() != levelStem.get().type().value().height()) {
                    throw new IllegalStateException("Can't change world height of dimension " + key + ". Requires reloading the world.");
                }

                level.dimensionTypeRegistration = new FrozenHolder<>(levelStem.get().type());
                chunkGenerator = levelStem.get().generator();
            } else {
                chunkGenerator = ChunkGenerator.CODEC.parse(RegistryOps.create(JsonOps.INSTANCE, dimensionContextProvider), oldGenerators.get(key)).result().orElseThrow();
            }

            ChunkMap chunkMap = level.getChunkSource().chunkMap;
            // Verify this generator change isn't going to cause lots of crashes
            if (chunkMap.generator() instanceof NoiseBasedChunkGenerator oldNoiseGenerator) {
                if (chunkGenerator instanceof NoiseBasedChunkGenerator newNoiseGenerator) {
                    if (!oldNoiseGenerator.generatorSettings().value().noiseSettings().equals(newNoiseGenerator.generatorSettings().value().noiseSettings())) {
                        throw new IllegalStateException("Can't change generator of dimension " + key + ": Uses different generation shapes in noise settings. Requires reloading the world.");
                    }
                }
            } else if (chunkGenerator instanceof NoiseBasedChunkGenerator) {
                throw new IllegalStateException("Can't change generator of dimension " + key + ": should now be NoiseBasedChunkGenerator. Requires reloading the world.");
            }

            ((UpdatableGeneratorChunkMap) chunkMap).worldgenDevtools$setGenerator(chunkGenerator);
        });
    }

    /**
     * Switches all clients to configuration phase, then sends new registries and switches back to gameplay.
     * @param serverConnection the server connection handler
     */
    public static void syncClient(ServerConnectionListener serverConnection) {
        for (Connection connection : serverConnection.getConnections()) {
            PacketListener var5 = connection.getPacketListener();
            if (var5 instanceof ServerGamePacketListenerImpl impl) {
                ServerPlayNetworking.reconfigure(impl);
            }
        }
    }

    public static CompletableFuture<RegistryAccess.Frozen> reloadData(
            final ResourceManager resourceManager,
            final List<HolderLookup.RegistryLookup<?>> contextRegistries,
            final List<RegistryDataLoader.RegistryData<?>> registriesToLoad,
            final Executor executor,
            RegistryAccess.Frozen reloadedLayer
    ) {
        RegistryDataLoader.LoaderFactory loaderFactory = new RegistryDataLoader.LoaderFactory() {
            @Override
            public <T> RegistryDataLoader.RegistryLoadTask<T> create(final RegistryDataLoader.RegistryData<T> data, final Map<ResourceKey<?>, Exception> loadingErrors) {
                return new ResourceManagerRegistryReloadTask<>(data, Lifecycle.stable(), loadingErrors, resourceManager, reloadedLayer);
            }
        };
        return RegistryDataLoader.load(loaderFactory, contextRegistries, registriesToLoad, executor);
    }

}
