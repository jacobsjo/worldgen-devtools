package eu.jacobsjo.worldgendevtools.client.coloredjigsawblock.mixin;

import com.mojang.serialization.DataResult;
import eu.jacobsjo.worldgendevtools.client.coloredjigsawblock.impl.JigsawBlockData;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements DataComponentHolder {
    @Shadow public abstract boolean is(Item item);

    @Unique
    private static final ResourceLocation EMPTY_RESOURCE_LOCATION = ResourceLocation.withDefaultNamespace("empty");
    @Unique
    @SuppressWarnings("deprecation")
    private static final ResourceLocation JIGSAW_BLOCK_ID = BlockEntityType.JIGSAW.builtInRegistryHolder().key().location();

    @Inject(method = "addDetailsToTooltip", at=@At("TAIL"))
    public void addDetailsToTooltip(Item.TooltipContext tooltipContext, TooltipDisplay tooltipDisplay, Player player, TooltipFlag tooltipFlag, Consumer<Component> consumer, CallbackInfo ci){
        if (this.is(Items.JIGSAW) && tooltipDisplay.shows(DataComponents.BLOCK_ENTITY_DATA)) {
            CustomData customData = this.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY);
            DataResult<JigsawBlockData> data = customData.read(JigsawBlockData.CODEC);

            if (data.isError() || data.result().isEmpty())
                return;

            if (!data.result().get().targetPool().equals(EMPTY_RESOURCE_LOCATION)) {
                consumer.accept(Component.translatable("worldgendevtools.coloredjigsawblock.target_pool_tooltip", data.result().get().targetPool().toString()));
            }

            if (!data.result().get().name().equals(EMPTY_RESOURCE_LOCATION)) {
                consumer.accept(Component.translatable("worldgendevtools.coloredjigsawblock.name_tooltip", data.result().get().name().toString()));
            }

            if (!data.result().get().target().equals(EMPTY_RESOURCE_LOCATION)) {
                consumer.accept(Component.translatable("worldgendevtools.coloredjigsawblock.target_name_tooltip", data.result().get().target().toString()));
            }
        }
    }
}
