package eu.jacobsjo.worldgendevtools.externalprofiling.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import eu.jacobsjo.worldgendevtools.externalprofiling.api.FeatureGenerationEvent;
import jdk.jfr.Event;
import net.minecraft.util.profiling.jfr.JfrProfiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.stream.Stream;

@Mixin(JfrProfiler.class)
public class JfrProfilerMixin {
    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Ljava/util/List;of(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/util/List;"))
    private static List<Class<? extends Event>> addEvent(List<Class<? extends Event>> original){
        return Stream.concat(original.stream(), Stream.of(FeatureGenerationEvent.class)).toList();
    }
}
