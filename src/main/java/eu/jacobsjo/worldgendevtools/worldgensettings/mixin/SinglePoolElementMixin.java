package eu.jacobsjo.worldgendevtools.worldgensettings.mixin;

import com.google.common.collect.ImmutableList;
import eu.jacobsjo.worldgendevtools.worldgensettings.WorldgenSettingsInit;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(SinglePoolElement.class)
public abstract class SinglePoolElementMixin {

    @Unique
    private WorldGenLevel level;

    /**
     * stores the level parameter for use in the getSettings method that is called from this method.
     */
    @Inject(method = "place", at =@At("HEAD"))
    public void place(StructureTemplateManager structureTemplateManager, WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, BlockPos offset, BlockPos pos, Rotation rotation, BoundingBox box, RandomSource random, boolean keepJigsaws, CallbackInfoReturnable<Boolean> cir){
        this.level = level;
    }

    /**
     * If the keepJigsaws gamerule is true, set the keepJigsaws parameter to true, which disables the JigsawReplacementProcessor.
     */
    @ModifyVariable(method = "getSettings", at=@At("HEAD"), ordinal = 0, argsOnly = true)
    public boolean getSettings(boolean keepJigsaws){
        if (level.getLevelData().getGameRules().getBoolean(WorldgenSettingsInit.KEEP_JIGSAWS)){
            return true;
        }
        return keepJigsaws;
    }

    /**
     * If the applyProcessorLists gamerule is false, don't return any items from the stored processorList
     */
    @Redirect(method = "getSettings", at= @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureProcessorList;list()Ljava/util/List;"))
    public List<StructureProcessor> getProcessorList(StructureProcessorList processorList) {
        if (!level.getLevelData().getGameRules().getBoolean(WorldgenSettingsInit.APPLY_PROCESSOR_LISTS)) {
            return ImmutableList.of();
        }
        return processorList.list();
    }

    /**
     * if the applyGrafivtyProcessor gamerule is false, don't apply any processors that come from the projection. This is currently only used for the gravity processor.
     */
    @Redirect(method = "getSettings", at= @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool$Projection;getProcessors()Lcom/google/common/collect/ImmutableList;"))
    public ImmutableList<StructureProcessor> getProcessors(StructureTemplatePool.Projection projection){
        if (!level.getLevelData().getGameRules().getBoolean(WorldgenSettingsInit.APPLY_GRAVITY_PROCESSOR)) {
            return ImmutableList.of();
        }
        return projection.getProcessors();
    }

}
