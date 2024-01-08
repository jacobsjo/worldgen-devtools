package eu.jacobsjo.worldgen_devtools.reload_registries.mixin;

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
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Vanilla uses {@link ChunkAccess#structureStarts} and {@link ChunkAccess#structuresRefences} to map structures to
 * structure starts and references. This mixin changes it to use {@link #structureStartsByLocation} and
 * {@link #structuresRefencesByLocation} which use {@link ResourceLocation}s instead. This change is only done on the
 * logical server.
 */
@Mixin(ChunkAccess.class)
public class ChunkAccessMixin {

    @Shadow protected volatile boolean unsaved;
    @Shadow @Final private static LongSet EMPTY_REFERENCE_SET;

    @Unique
    private Map<ResourceLocation, StructureStart> structureStartsByLocation;
    @Unique
    private Map<ResourceLocation, LongSet> structuresRefencesByLocation;
    @Unique
    private Registry<Structure> structureRegistry;

    @Inject(method="<init>(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/chunk/UpgradeData;Lnet/minecraft/world/level/LevelHeightAccessor;Lnet/minecraft/core/Registry;J[Lnet/minecraft/world/level/chunk/LevelChunkSection;Lnet/minecraft/world/level/levelgen/blending/BlendingData;)V", at = @At("TAIL"))
    private void init(ChunkPos chunkPos, UpgradeData upgradeData, LevelHeightAccessor levelHeightAccessor, Registry<Biome> registry, long l, @Nullable LevelChunkSection[] levelChunkSections, @Nullable BlendingData blendingData, CallbackInfo info){
        if (!((Level) levelHeightAccessor).isClientSide()) {
            this.structureRegistry = ((Level) levelHeightAccessor).registryAccess().registry(Registries.STRUCTURE).orElseThrow();
            this.structureStartsByLocation = Maps.newHashMap();
            this.structuresRefencesByLocation = Maps.newHashMap();
        }
    }

    @Inject(method = "getStartForStructure", at=@At("HEAD"), cancellable = true)
    public void getStartForStructure(Structure structure, CallbackInfoReturnable<StructureStart> cir) {
        if (structureRegistry == null) return;
        cir.setReturnValue(this.structureStartsByLocation.get(structureRegistry.getKey(structure)));
        cir.cancel();
    }

    @Inject(method = "setStartForStructure", at=@At("HEAD"), cancellable = true)
    public void setStartForStructure(Structure structure, StructureStart structureStart, CallbackInfo ci) {
        if (structureRegistry == null) return;
        this.structureStartsByLocation.put(structureRegistry.getKey(structure), structureStart);
        this.unsaved = true;
        ci.cancel();
    }

    @Inject(method = "getAllStarts", at=@At("HEAD"), cancellable = true)
    public void getAllStarts(CallbackInfoReturnable<Map<Structure, StructureStart>> cir) {
        if (structureRegistry == null) return;
        cir.setReturnValue(Collections.unmodifiableMap(this.structureStartsByLocation.entrySet().stream().collect(HashMap::new, (m, e) -> m.put(structureRegistry.get(e.getKey()), e.getValue()), HashMap::putAll)));
        cir.cancel();
    }

    @Inject(method = "setAllStarts", at=@At("HEAD"), cancellable = true)
    public void setAllStarts(Map<Structure, StructureStart> structureStarts, CallbackInfo ci) {
        if (structureRegistry == null) return;
        this.structureStartsByLocation.clear();
        structureStarts.forEach((structure, structureStart) -> this.structureStartsByLocation.put(structureRegistry.getKey(structure), structureStart));
        this.unsaved = true;
        ci.cancel();
    }

    @Inject(method = "getReferencesForStructure", at=@At("HEAD"), cancellable = true)
    public void getReferencesForStructure(Structure structure, CallbackInfoReturnable<LongSet> cir) {
        if (structureRegistry == null) return;
        cir.setReturnValue(this.structuresRefencesByLocation.getOrDefault(structureRegistry.getKey(structure), EMPTY_REFERENCE_SET));
        cir.cancel();
    }

    @Inject(method = "addReferenceForStructure", at=@At("HEAD"), cancellable = true)
    public void addReferenceForStructure(Structure structure, long reference, CallbackInfo ci) {
        if (structureRegistry == null) return;
        (this.structuresRefencesByLocation.computeIfAbsent(structureRegistry.getKey(structure), (structurex) -> new LongOpenHashSet())).add(reference);
        this.unsaved = true;
        ci.cancel();
    }

    @Inject(method = "getAllReferences", at=@At("HEAD"), cancellable = true)
    public void getAllReferences(CallbackInfoReturnable<Map<Structure, LongSet>> cir) {
        if (structureRegistry == null) return;
        cir.setReturnValue(Collections.unmodifiableMap(this.structuresRefencesByLocation.entrySet().stream().collect(HashMap::new, (m, e) -> m.put(structureRegistry.get(e.getKey()), e.getValue()), HashMap::putAll)));
        cir.cancel();
    }

    @Inject(method = "setAllReferences", at=@At("HEAD"), cancellable = true)
    public void setAllReferences(Map<Structure, LongSet> structureReferencesMap, CallbackInfo ci) {
        if (structureRegistry == null) return;
        this.structuresRefencesByLocation.clear();
        structureReferencesMap.forEach((structure, reference) -> this.structuresRefencesByLocation.put(structureRegistry.getKey(structure), reference));
        this.unsaved = true;
        ci.cancel();
    }
}
