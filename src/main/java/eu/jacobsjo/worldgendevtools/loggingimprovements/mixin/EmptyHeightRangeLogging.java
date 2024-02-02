package eu.jacobsjo.worldgendevtools.loggingimprovements.mixin;


import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.BiasedToBottomHeight;
import net.minecraft.world.level.levelgen.heightproviders.TrapezoidHeight;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.heightproviders.VeryBiasedToBottomHeight;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin({BiasedToBottomHeight.class, VeryBiasedToBottomHeight.class, TrapezoidHeight.class, UniformHeight.class})
public class EmptyHeightRangeLogging {
    @Unique
    private static final Logger MIXIN_LOGGER = LoggerFactory.getLogger("worldgendevtools");

    @Inject(
            method = "sample",
            at = @At(value = "INVOKE",
                    target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;)V",
                    remap = false,
                    shift = At.Shift.AFTER
            )
    )
    private void logFeature(RandomSource random, WorldGenerationContext context, CallbackInfoReturnable<Integer> cir){
        if (context instanceof PlacementContext placementContext){
            Optional<PlacedFeature> topFeature = placementContext.topFeature();
            Registry<PlacedFeature> registry = placementContext.getLevel().registryAccess().registry(Registries.PLACED_FEATURE).orElseThrow();
            if (topFeature.isPresent()){
                ResourceLocation key = registry.getKey(topFeature.get());
                if (key != null) {
                    MIXIN_LOGGER.warn("Offending placed feature: {}", key);
                }
            }
        }
    }
}
