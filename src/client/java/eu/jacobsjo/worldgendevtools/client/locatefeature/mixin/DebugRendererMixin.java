package eu.jacobsjo.worldgendevtools.client.locatefeature.mixin;

import eu.jacobsjo.worldgendevtools.client.locatefeature.impl.LocationRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.debug.DebugRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static eu.jacobsjo.worldgendevtools.client.locatefeature.LocateFeatureClientInit.FEATURE_POSITIONS_RENDERER;

@Mixin(DebugRenderer.class)
public class DebugRendererMixin {
    @Shadow
    @Final
    private List<DebugRenderer.SimpleDebugRenderer> opaqueRenderers;

    @Inject(method = "refreshRendererList", at = @At("TAIL"))
    public void refreshRendererList(CallbackInfo ci) {
        if (Minecraft.getInstance().debugEntries.isCurrentlyEnabled(FEATURE_POSITIONS_RENDERER)){
            this.opaqueRenderers.add(new LocationRenderer());
        }
    }
}
