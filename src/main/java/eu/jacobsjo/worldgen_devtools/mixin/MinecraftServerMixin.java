package eu.jacobsjo.worldgen_devtools.mixin;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
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
import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {


    @Unique
    private static final Set<ResourceKey<?>> RELOADED_REGISTRIES = new HashSet<>(List.of(
            Registries.CONFIGURED_CARVER,   // WORKS!
            Registries.CONFIGURED_FEATURE,  // WORKS!
            Registries.PLACED_FEATURE,      // WORKS!
            Registries.DIMENSION_TYPE,      // WORKS, not synconized with client
            Registries.NOISE,               // WORKS!
            Registries.DENSITY_FUNCTION,    // WORKS (probably)
            Registries.NOISE_SETTINGS,      // WORKS
            Registries.STRUCTURE_SET,       // WORKS, but not with /resetchunks, and not new sets?
            Registries.PROCESSOR_LIST,      // WORKS!
            Registries.TEMPLATE_POOL,       // WORKS, but not with /resetchunks
            Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST, //Whatever
            Registries.BIOME,               // WORKS, effects not syncronized with client
            Registries.STRUCTURE            // WORKS! (/locate is slightly broken...)

            //Presets
            //Registries.WORLD_PRESET,
            //Registries.FLAT_LEVEL_GENERATOR_PRESET,

            //Non-worldgen
            //Registries.TRIM_PATTERN,
            //Registries.TRIM_MATERIAL,
            //Registries.DAMAGE_TYPE
            //Registries.CHAT_TYPE,
    ));

    @Shadow
    private LayeredRegistryAccess<RegistryLayer> registries;

    @Shadow @Final private static Logger LOGGER;

    @Shadow public abstract ServerConnectionListener getConnection();

    @Shadow @Final private Map<ResourceKey<Level>, ServerLevel> levels;

    @Inject (method = "method_29437", at = @At("HEAD"))
    private void thenCompose(RegistryAccess.Frozen frozen, ImmutableList<PackResources> immutableList, CallbackInfoReturnable<CompletionStage<?>> cir) {
        CloseableResourceManager resourceManager = new MultiPackResourceManager(PackType.SERVER_DATA, immutableList);

        Map<ResourceKey<Level>, DataResult<JsonElement>> generators = new HashMap<>();
        this.levels.forEach((key, level) -> generators.put(key, ChunkGenerator.CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE, getRegistrtyInfoLookup()), level.getChunkSource().getGenerator())));


        // Worldgen registries
        Map<ResourceKey<?>, Exception> exceptionMap = new HashMap<>();

        RegistryAccess.Frozen beforeLayer = this.registries.getAccessForLoading(RegistryLayer.WORLDGEN);
        RegistryAccess.Frozen layer = this.registries.getLayer(RegistryLayer.WORLDGEN);

        final Map<ResourceKey<? extends Registry<?>>, RegistryOps.RegistryInfo<?>> lookupMap = new HashMap<>();
        beforeLayer.registries().forEach((registryEntry) -> {
            lookupMap.put(registryEntry.key(), createInfoForContextRegistry(registryEntry.value()));
        });

        layer.registries().forEach((registryEntry) -> {
            assert registryEntry.value() instanceof MappedRegistry;
            MappedRegistry<?> registry = (MappedRegistry<?>) registryEntry.value();

            if (RELOADED_REGISTRIES.stream().anyMatch(d -> d.equals(registryEntry.key()))) {
                ((RegistryResetter) registry).reset();
                lookupMap.put(registryEntry.key(), createInfoForNewRegistry(registry));
            } else {
                lookupMap.put(registryEntry.key(), createInfoForContextRegistry(registryEntry.value()));
            }
        });
        RegistryOps.RegistryInfoLookup registryInfoLookup = new RegistryOps.RegistryInfoLookup() {
            public <E> Optional<RegistryOps.RegistryInfo<E>> lookup(ResourceKey<? extends Registry<? extends E>> resourceKey) {
                return Optional.ofNullable((RegistryOps.RegistryInfo<E>) lookupMap.get(resourceKey));
            }
        };

        RegistryDataLoader.WORLDGEN_REGISTRIES.forEach((RegistryDataLoader.RegistryData<?> data) -> {
            if (RELOADED_REGISTRIES.stream().anyMatch(d -> d.equals(data.key()))) {
                loadData(registryInfoLookup, resourceManager, data, layer, exceptionMap);
            }

        });

        if (!exceptionMap.isEmpty()) {
            logErrors(exceptionMap);
            exceptionMap.values().forEach(e -> LOGGER.error(e.getMessage()));
            throw new IllegalStateException("Failed to load registries due to above errors");
        }

        // Dimension registry
        //TODO this doesn't work: dimensions aren't reloaded and saving breaks on server stop "java.lang.IllegalStateException: Overworld settings missing"
        /*
        reloadRegistryLayer(
                resourceManager, this.registries, RegistryLayer.DIMENSIONS, RegistryDataLoader.DIMENSION_REGISTRIES
        );*/


        generators.forEach((key, json) -> {
            DataResult<ChunkGenerator> dataResult2 = json.flatMap((jsonElement) -> {
                return ChunkGenerator.CODEC.parse(RegistryOps.create(JsonOps.INSTANCE, getRegistrtyInfoLookup()), jsonElement);
            });
            dataResult2.result().ifPresent((chunkGenerator) -> {
                ChunkMap chunkMap = this.levels.get(key).getChunkSource().chunkMap;
                ((UpdatableGeneratorChunkMap) chunkMap).worldgenDevtools$setGenerator(chunkGenerator);
            });

        } );

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
    private RegistryOps.RegistryInfoLookup getRegistrtyInfoLookup(){
        final Map<ResourceKey<? extends Registry<?>>, RegistryOps.RegistryInfo<?>> lookupMap = new HashMap<>();
        this.registries.getAccessForLoading(RegistryLayer.DIMENSIONS).registries().forEach((registryEntry) -> {
            lookupMap.put(registryEntry.key(), createInfoForContextRegistry(registryEntry.value()));
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
