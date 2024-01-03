package eu.jacobsjo.worldgen_devtools.mixin;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Mixin(ChunkAccess.class)
public class ChunkAccessMixin {

    @Shadow protected volatile boolean unsaved;
    @Shadow @Final private static LongSet EMPTY_REFERENCE_SET;
    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final protected LevelHeightAccessor levelHeightAccessor;
    @Unique
    private Map<ResourceLocation, StructureStart> structureStartsByLocation;
    @Unique
    private Map<ResourceLocation, LongSet> structuresRefencesByLocation;

    @Unique
    private Registry<Structure> structureRegistry;

    @Inject(method="<init>(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/chunk/UpgradeData;Lnet/minecraft/world/level/LevelHeightAccessor;Lnet/minecraft/core/Registry;J[Lnet/minecraft/world/level/chunk/LevelChunkSection;Lnet/minecraft/world/level/levelgen/blending/BlendingData;)V", at = @At("TAIL"))
    private void init(ChunkPos chunkPos, UpgradeData upgradeData, LevelHeightAccessor levelHeightAccessor, Registry<Biome> registry, long l, @Nullable LevelChunkSection[] levelChunkSections, @Nullable BlendingData blendingData, CallbackInfo info){
        this.structureRegistry = ((Level) levelHeightAccessor).registryAccess().registry(Registries.STRUCTURE).orElseThrow();
        this.structureStartsByLocation = Maps.newHashMap();
        this.structuresRefencesByLocation = Maps.newHashMap();
    }



    /**
     * @author eu.jacobsjo.worldgen_devtools
     * @reason Changing structure start saving to save key instead, so registry changes work
     */
    @Nullable @Overwrite
    public StructureStart getStartForStructure(Structure structure) {
        return this.structureStartsByLocation.get(structureRegistry.getKey(structure));
    }

    /**
     * @author eu.jacobsjo.worldgen_devtools
     * @reason Changing structure start saving to save key instead, so registry changes work
     */
    @Overwrite
    public void setStartForStructure(Structure structure, StructureStart structureStart) {
        this.structureStartsByLocation.put(structureRegistry.getKey(structure), structureStart);
        this.unsaved = true;
    }

    /**
     * @author eu.jacobsjo.worldgen_devtools
     * @reason Changing structure start saving to save key instead, so registry changes work
     */
    @Overwrite
    public Map<Structure, StructureStart> getAllStarts() {
        return Collections.unmodifiableMap(this.structureStartsByLocation.entrySet().stream().collect(HashMap::new, (m, e) -> m.put(structureRegistry.get(e.getKey()), e.getValue()), HashMap::putAll));
    }

    /**
     * @author eu.jacobsjo.worldgen_devtools
     * @reason Changing structure start saving to save key instead, so registry changes work
     */
    @Overwrite
    public void setAllStarts(Map<Structure, StructureStart> map) {
        this.structureStartsByLocation.clear();
        map.forEach(this::setStartForStructure);
        this.unsaved = true;
    }

    /**
     * @author eu.jacobsjo.worldgen_devtools
     * @reason Changing structure start saving to save key instead, so registry changes work
     */
    @Overwrite
    public LongSet getReferencesForStructure(Structure structure) {
        return this.structuresRefencesByLocation.getOrDefault(structureRegistry.getKey(structure), EMPTY_REFERENCE_SET);
    }

    /**
     * @author eu.jacobsjo.worldgen_devtools
     * @reason Changing structure start saving to save key instead, so registry changes work
     */
    @Overwrite
    public void addReferenceForStructure(Structure structure, long l) {
        (this.structuresRefencesByLocation.computeIfAbsent(structureRegistry.getKey(structure), (structurex) -> new LongOpenHashSet())).add(l);
        this.unsaved = true;
    }

    /**
     * @author eu.jacobsjo.worldgen_devtools
     * @reason Changing structure start saving to save key instead, so registry changes work
     */
    @Overwrite
    public Map<Structure, LongSet> getAllReferences() {
        return Collections.unmodifiableMap(this.structuresRefencesByLocation.entrySet().stream().collect(HashMap::new, (m, e) -> m.put(structureRegistry.get(e.getKey()), e.getValue()), HashMap::putAll));
    }

    /**
     * @author eu.jacobsjo.worldgen_devtools
     * @reason Changing structure start saving to save key instead, so registry changes work
     */
    @Overwrite
    public void setAllReferences(Map<Structure, LongSet> map) {
        this.structuresRefencesByLocation.clear();
        map.forEach((structure, reference) -> this.structuresRefencesByLocation.put(structureRegistry.getKey(structure), reference));
        this.unsaved = true;
    }
}
