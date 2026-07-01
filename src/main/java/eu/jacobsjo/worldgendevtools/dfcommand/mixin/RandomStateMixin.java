package eu.jacobsjo.worldgendevtools.dfcommand.mixin;


import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import eu.jacobsjo.worldgendevtools.dfcommand.api.RandomStateVisitorAccessor;
import net.minecraft.world.level.levelgen.DensityFunction.Visitor;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.RandomState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RandomState.class)
public class RandomStateMixin implements RandomStateVisitorAccessor {
    @Unique
    Visitor visitor;

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/NoiseRouter;mapAll(Lnet/minecraft/world/level/levelgen/DensityFunction$Visitor;)Lnet/minecraft/world/level/levelgen/NoiseRouter;"))
    public NoiseRouter init(NoiseRouter noiseRouter, Visitor visitor, Operation<NoiseRouter> original){
        this.visitor = visitor;
        return original.call(noiseRouter, visitor);
    }

    @Override
    public Visitor worldgenDevtools$getVisitor() {
        return visitor;
    }
}
