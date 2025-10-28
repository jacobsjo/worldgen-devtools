package eu.jacobsjo.worldgendevtools.worldgensettings.mixin;

import eu.jacobsjo.worldgendevtools.worldgensettings.WorldgenSettingsInit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.storage.EntityStorage;
import net.minecraft.world.level.entity.ChunkEntities;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityStorage.class)
public class EntityStorageMixin {

    @Shadow
    @Final
    private ServerLevel level;

    @Inject(method="storeEntities", at=@At("HEAD"), cancellable = true)
    private void save(ChunkEntities<Entity> entities, CallbackInfo ci){
        if (!this.level.getGameRules().get(WorldgenSettingsInit.SAVE_CHUNKS)){
            ci.cancel();
        }
    }
}
