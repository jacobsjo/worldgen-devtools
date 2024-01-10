package eu.jacobsjo.worldgen_devtools.worldgen_settings.mixin;

import eu.jacobsjo.worldgen_devtools.worldgen_settings.WorldgenSettingsInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(StructureTemplate.class)
public abstract class StructureTemplateMixin {
    @Redirect(method = "processBlockInfos", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureProcessor;processBlock(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate$StructureBlockInfo;Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate$StructureBlockInfo;Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructurePlaceSettings;)Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate$StructureBlockInfo;"))
    private static StructureTemplate.StructureBlockInfo processBlockInfos(StructureProcessor instance, LevelReader level, BlockPos offset, BlockPos pos, StructureTemplate.StructureBlockInfo blockInfo, StructureTemplate.StructureBlockInfo relativeBlockInfo, StructurePlaceSettings settings){
        GameRules gameRules = ((ServerLevelAccessor)level).getLevelData().getGameRules();

        if (instance instanceof GravityProcessor){
            if (!gameRules.getBoolean(WorldgenSettingsInit.APPLY_GRAVITY_PROCESSOR)) return relativeBlockInfo;
        } else if (instance instanceof JigsawReplacementProcessor){
            if (!gameRules.getBoolean(WorldgenSettingsInit.APPLY_JIGSAW_REPLACEMENT_PROCESSOR)) return relativeBlockInfo;
        } else {
            if (!gameRules.getBoolean(WorldgenSettingsInit.APPLY_PROCESSORS)) return relativeBlockInfo;
        }

        return instance.processBlock(level, offset, pos, blockInfo, relativeBlockInfo, settings);
    }
}
