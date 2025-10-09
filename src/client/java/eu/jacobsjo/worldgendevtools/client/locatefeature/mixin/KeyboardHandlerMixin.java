package eu.jacobsjo.worldgendevtools.client.locatefeature.mixin;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import static eu.jacobsjo.worldgendevtools.client.locatefeature.LocateFeatureClientInit.FEATURE_POSITIONS_RENDERER;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
    @Shadow protected abstract void debugFeedbackTranslated(String message, Object... args);

    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "handleDebugKeys", at = @At("TAIL"), cancellable = true)
    public void handleDebugKeys(KeyEvent keyEvent, CallbackInfoReturnable<Boolean> cir){
        if (keyEvent.key() == java.awt.event.KeyEvent.VK_F){
            boolean enabled = this.minecraft.debugEntries.toggleStatus(FEATURE_POSITIONS_RENDERER);
            this.debugFeedbackTranslated(enabled ? "worldgendevtools.locatefeature.renderer.on" : "worldgendevtools.locatefeature.renderer.off");
            cir.setReturnValue(true);
        }
    }
}
