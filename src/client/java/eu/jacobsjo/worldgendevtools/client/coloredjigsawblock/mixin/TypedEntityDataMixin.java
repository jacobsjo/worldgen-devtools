package eu.jacobsjo.worldgendevtools.client.coloredjigsawblock.mixin;

import com.mojang.serialization.DataResult;
import eu.jacobsjo.worldgendevtools.client.coloredjigsawblock.impl.JigsawBlockData;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(TypedEntityData.class)
public abstract class TypedEntityDataMixin<IdType> {

    @Shadow @Final private IdType type;
    @Shadow @Final private CompoundTag tag;
    @Unique
    private static final ResourceLocation EMPTY_RESOURCE_LOCATION = ResourceLocation.withDefaultNamespace("empty");

    @Inject(method = "addToTooltip", at=@At("TAIL"))
    public void addToTooltip(Item.TooltipContext tooltipContext, Consumer<Component> consumer, TooltipFlag tooltipFlag, DataComponentGetter dataComponentGetter, CallbackInfo ci){
        if (this.type == BlockEntityType.JIGSAW) {
            DataResult<JigsawBlockData> data = JigsawBlockData.CODEC.decode(NbtOps.INSTANCE, NbtOps.INSTANCE.getMap(this.tag).getOrThrow());

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
