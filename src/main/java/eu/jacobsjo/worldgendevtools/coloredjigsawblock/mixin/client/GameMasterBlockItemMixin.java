package eu.jacobsjo.worldgendevtools.coloredjigsawblock.mixin.client;

import com.mojang.serialization.DataResult;
import eu.jacobsjo.worldgendevtools.coloredjigsawblock.impl.JigsawBlockData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(GameMasterBlockItem.class)
public class GameMasterBlockItemMixin extends BlockItem {

    @Unique
    private static final ResourceLocation EMPTY_RESOURCE_LOCATION = new ResourceLocation("empty");

    public GameMasterBlockItemMixin(Block block, Properties properties) {
        super(block, properties);
    }

    /**
     * Overrides appendHoverText to add a tooltip for jigsaw block items that have BlockEntityTag stored.
     */
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced){
        if (!stack.getItem().equals(Items.JIGSAW))
            return;

        TypedDataComponent<CustomData> blockEntityData = stack.getComponents().getTyped(DataComponents.BLOCK_ENTITY_DATA);
        if (blockEntityData == null)
            return;

        DataResult<JigsawBlockData> data = blockEntityData.value().read(JigsawBlockData.CODEC);
        if (data.isError() || data.result().isEmpty())
            return;

        if (!data.result().get().targetPool().equals(EMPTY_RESOURCE_LOCATION)){
            tooltipComponents.add(Component.translatable("item.minecraft.jigsaw.target_pool_tooltip", data.result().get().targetPool().toString()));
        }

        if (!data.result().get().name().equals(EMPTY_RESOURCE_LOCATION)){
            tooltipComponents.add(Component.translatable("item.minecraft.jigsaw.name_tooltip", data.result().get().name().toString()));
        }

        if (!data.result().get().target().equals(EMPTY_RESOURCE_LOCATION)){
            tooltipComponents.add(Component.translatable("item.minecraft.jigsaw.target_name_tooltip", data.result().get().target().toString()));
        }
    }
}
