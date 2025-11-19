package eu.jacobsjo.worldgendevtools.environmentattributes.mixin;

import eu.jacobsjo.worldgendevtools.environmentattributes.impl.AttributeOverrides;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnvironmentAttributeSystem.class)
public class EnvironmentAttributeSystemMixin {

    @Inject(method = "addDefaultLayers", at = @At("TAIL"))
    private static void addDefaultLayers(EnvironmentAttributeSystem.Builder builder, Level level, CallbackInfo ci){
        AttributeOverrides.addLayers(builder);
    }
}
