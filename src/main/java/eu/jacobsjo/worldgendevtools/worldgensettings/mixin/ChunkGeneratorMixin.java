package eu.jacobsjo.worldgendevtools.worldgensettings.mixin;


import eu.jacobsjo.worldgendevtools.worldgensettings.WorldgenSettingsInit;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkGenerator.class)
public class ChunkGeneratorMixin {
    /**
     * don't apply biome decoration (features) if the maxChunkStatus gamerule forbids it
     */
    @Inject(method= "applyBiomeDecoration", at=@At("HEAD"), cancellable = true)
    public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager, CallbackInfo ci) {
        if (!level.getLevelData().getGameRules().getRule(WorldgenSettingsInit.MAX_CHUNK_STATUS).get().features){
            ci.cancel();
        };
    }

}
