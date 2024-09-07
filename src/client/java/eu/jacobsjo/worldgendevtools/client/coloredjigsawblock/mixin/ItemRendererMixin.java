package eu.jacobsjo.worldgendevtools.client.coloredjigsawblock.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(value= EnvType.CLIENT)
@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
    @Shadow @Final private BlockEntityWithoutLevelRenderer blockEntityRenderer;

    /**
     * Hack to allow the Jigsaw items to render both the normal model and the registered DynamicItemRenderer.
     */
    @Inject(method = "renderItem", at = @At("HEAD"))
    public void renderSimpleItemModel(ItemStack itemStack, ItemDisplayContext itemDisplayContext, PoseStack poseStack, MultiBufferSource multiBufferSource, int combinedLight, int combinedOverlay, BakedModel bakedModel, boolean bl, CallbackInfo ci){
        if (itemStack.getItem().equals(Items.JIGSAW)){
            this.blockEntityRenderer.renderByItem(itemStack, itemDisplayContext, poseStack, multiBufferSource, combinedLight, combinedOverlay);
        }
    }

}
