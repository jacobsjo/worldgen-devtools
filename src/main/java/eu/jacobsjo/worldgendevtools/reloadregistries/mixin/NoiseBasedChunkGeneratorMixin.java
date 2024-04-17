package eu.jacobsjo.worldgendevtools.reloadregistries.mixin;


import eu.jacobsjo.worldgendevtools.reloadregistries.impl.FrozenHolder;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NoiseBasedChunkGenerator.class)
public class NoiseBasedChunkGeneratorMixin {
    @Mutable @Shadow @Final private Holder<NoiseGeneratorSettings> settings;

    // Freeze the noisesettings holder, to ensure it doesn't get changed by reload. Reload creates new generator instead.
    @Inject(method = "<init>", at = @At("TAIL"))
    private void freezeHolder(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings, CallbackInfo ci){
        this.settings = new FrozenHolder<>(settings);
    }
}
