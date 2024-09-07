package eu.jacobsjo.worldgendevtools.reloadregistries.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import eu.jacobsjo.worldgendevtools.reloadregistries.api.HolderStructureStart;
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
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * In vanilla, {@link StructureStart} uses direct references to {@link Structure}s. This is problematic when reloading the
 * registries. Therefore, this mixin changes it to use a {@link Holder}< Structure > instead. The {@link #worldgenDevtools$setHolder}
 * method needs to be called after creation of the structure start.
 */
@Mixin(StructureStart.class)
public class StructureStartMixin implements HolderStructureStart {

    @Mutable @Shadow @Final private Structure structure;
    @Unique
    Holder<Structure> holder;

    @Override
    public void worldgenDevtools$setHolder(Holder.Reference<Structure> holder) {
        this.holder = holder;
    }

    /**
     * calls {@link #worldgenDevtools$setHolder} after creation from loading.
     */
    @Inject(method = "loadStaticStart", at = @At("RETURN"))
    private static void loadStaticStart(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag, long l, CallbackInfoReturnable<StructureStart> cir, @Local String string){
        Registry<Structure> registry = structurePieceSerializationContext.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        ((HolderStructureStart) (Object) cir.getReturnValue()).worldgenDevtools$setHolder(registry.getOrThrow(ResourceKey.create(Registries.STRUCTURE, ResourceLocation.parse(string))));
    }

    /**
     * Sets {@link #structure} from holder imediately before usage.
     */
    @Inject(method = "getBoundingBox", at = @At(value = "HEAD"))
    public void getBoundingBox(CallbackInfoReturnable<BoundingBox> cir) {
        this.structure = this.holder.value();
    }

    /**
     * Sets {@link #structure} from holder imediately before usage.
     */
    @Inject(method = "placeInChunk", at = @At(value = "HEAD"))
    public void placeInChunk(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox boundingBox, ChunkPos chunkPos, CallbackInfo ci) {
        this.structure = this.holder.value();
    }

    /**
     * Sets {@link #structure} from holder imediately before usage.
     */
    @Inject(method = "createTag", at = @At(value = "HEAD"))
    public void createTag(CallbackInfoReturnable<BoundingBox> cir) {
        if (this.holder == null){
            throw new IllegalStateException("no holder found");
        }
        this.structure = this.holder.value();
    }

    /**
     * Sets {@link #structure} from holder imediately before usage.
     */
    @Inject(method = "getStructure", at = @At(value = "HEAD"))
    public void getStructure(CallbackInfoReturnable<Structure> cir) {
        this.structure = this.holder.value();
    }

}
