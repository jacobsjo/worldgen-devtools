package eu.jacobsjo.worldgen_devtools.mixin;

import eu.jacobsjo.worldgen_devtools.HolderStructureStart;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(StructureStart.class)
public class StructureStartMixin implements HolderStructureStart {

    @Mutable @Shadow @Final private Structure structure;
    @Shadow @Final private static Logger LOGGER;
    @Unique
    Holder<Structure> holder;

    @Override
    public void worldgenDevtools$setHolder(Holder.Reference<Structure> holder) {
        LOGGER.info("Set holder to {}", holder.key().location());
        this.holder = holder;
    }

    @Inject(method = "loadStaticStart", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void loadStaticStart(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag, long l, CallbackInfoReturnable<StructureStart> cir, String string){
        Registry<Structure> registry = structurePieceSerializationContext.registryAccess().registryOrThrow(Registries.STRUCTURE);
        ((HolderStructureStart) (Object) cir.getReturnValue()).worldgenDevtools$setHolder(registry.getHolderOrThrow(ResourceKey.create(Registries.STRUCTURE, new ResourceLocation(string))));
    }

    @Inject(method = "getBoundingBox", at = @At(value = "HEAD"))
    public void getBoundingBox(CallbackInfoReturnable<BoundingBox> cir) {
        this.structure = this.holder.value();
    }

    @Inject(method = "placeInChunk", at = @At(value = "HEAD"))
    public void placeInChunk(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox boundingBox, ChunkPos chunkPos, CallbackInfo ci) {
        this.structure = this.holder.value();
    }

    @Inject(method = "createTag", at = @At(value = "HEAD"))
    public void createTag(CallbackInfoReturnable<BoundingBox> cir) {
        if (this.holder == null){
            throw new IllegalStateException("no holder found");
        }
        this.structure = this.holder.value();
    }

    @Inject(method = "getStructure", at = @At(value = "HEAD"))
    public void getStructure(CallbackInfoReturnable<Structure> cir) {
        this.structure = this.holder.value();
    }

}
