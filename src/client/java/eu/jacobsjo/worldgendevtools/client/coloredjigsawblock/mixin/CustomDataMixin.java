package eu.jacobsjo.worldgendevtools.client.coloredjigsawblock.mixin;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapDecoder;
import eu.jacobsjo.worldgendevtools.client.coloredjigsawblock.impl.JigsawBlockData;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.function.Consumer;

@Mixin(CustomData.class)
public abstract class CustomDataMixin {
    @Shadow @Final private CompoundTag tag;
    @Shadow public abstract <T> DataResult<T> read(MapDecoder<T> mapDecoder);

    @Unique
    private static final ResourceLocation EMPTY_RESOURCE_LOCATION = ResourceLocation.withDefaultNamespace("empty");
    @Unique
    @SuppressWarnings("deprecation")
    private static final ResourceLocation JIGSAW_BLOCK_ID = BlockEntityType.JIGSAW.builtInRegistryHolder().key().location();

    @Inject(method = "addToTooltip", at=@At("TAIL"))
    public void addToTooltip(Item.TooltipContext tooltipContext, Consumer<Component> consumer, TooltipFlag tooltipFlag, DataComponentGetter dataComponentGetter, CallbackInfo ci){
        Optional<String> id = this.tag.getString("id");
        if (id.isEmpty()) return;
        ResourceLocation resourceLocation = ResourceLocation.tryParse(id.get());
        if (JIGSAW_BLOCK_ID.equals(resourceLocation)) {
            DataResult<JigsawBlockData> data = this.read(JigsawBlockData.CODEC);
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
