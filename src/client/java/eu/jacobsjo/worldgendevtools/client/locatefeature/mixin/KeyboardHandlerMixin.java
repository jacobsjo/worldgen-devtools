package eu.jacobsjo.worldgendevtools.client.locatefeature.mixin;

import eu.jacobsjo.worldgendevtools.client.locatefeature.impl.LocationRenderer;
import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.event.KeyEvent;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
    @Shadow protected abstract void debugFeedbackTranslated(String message, Object... args);

    @Inject(method = "handleDebugKeys", at = @At("TAIL"), cancellable = true)
    public void handleDebugKeys(int key, CallbackInfoReturnable<Boolean> cir){
        if (key == KeyEvent.VK_F){
            boolean enabled = LocationRenderer.toggle();
            this.debugFeedbackTranslated(enabled ? "worldgendevtools.locatefeature.renderer.on" : "worldgendevtools.locatefeature.renderer.off");
            cir.setReturnValue(true);
        }
    }
}
