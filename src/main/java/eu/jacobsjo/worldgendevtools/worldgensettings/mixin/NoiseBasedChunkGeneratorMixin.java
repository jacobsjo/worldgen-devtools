package eu.jacobsjo.worldgendevtools.worldgensettings.mixin;


import eu.jacobsjo.worldgendevtools.worldgensettings.WorldgenSettingsInit;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.storage.ServerLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NoiseBasedChunkGenerator.class)
public class NoiseBasedChunkGeneratorMixin {

    /**
     * don't build surface if the maxChunkStatus gamerule forbids it
     */
    @Inject(method="buildSurface(Lnet/minecraft/server/level/WorldGenRegion;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/chunk/ChunkAccess;)V", at=@At("HEAD"), cancellable = true)
    public void buildSurface(WorldGenRegion level, StructureManager structureManager, RandomState random, ChunkAccess chunk, CallbackInfo ci){
        if (!((ServerLevelData) level.getLevelData()).getGameRules().getRule(WorldgenSettingsInit.MAX_CHUNK_STATUS).get().surface){
            ci.cancel();
        }
    }

    /**
     * don't apply carvers if the maxChunkStatus gamerule forbids it
     */
    @Inject(method= "applyCarvers", at=@At("HEAD"), cancellable = true)
    public void applyCarvers(WorldGenRegion level, long seed, RandomState random, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunk, CallbackInfo ci) {
        if (!((ServerLevelData) level.getLevelData()).getGameRules().getRule(WorldgenSettingsInit.MAX_CHUNK_STATUS).get().carvers){
            ci.cancel();
        }
    }

}
