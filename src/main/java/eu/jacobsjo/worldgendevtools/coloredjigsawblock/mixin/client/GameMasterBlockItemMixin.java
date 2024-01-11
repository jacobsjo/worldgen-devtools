package eu.jacobsjo.worldgendevtools.coloredjigsawblock.mixin.client;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(GameMasterBlockItem.class)
public class GameMasterBlockItemMixin extends BlockItem {
    private static final ResourceLocation EMPTY_RESOURCE_LOCATION = new ResourceLocation("empty");

    public GameMasterBlockItemMixin(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced){
        if (!stack.getItem().equals(Items.JIGSAW))
            return;

        CompoundTag tag = stack.getTagElement("BlockEntityTag");
        if (tag == null)
            return;

        ResourceLocation name = new ResourceLocation(tag.getString("name"));
        ResourceLocation targetName = new ResourceLocation(tag.getString("target"));
        ResourceLocation targetPool = new ResourceLocation(tag.getString("pool"));

        if (!name.equals(EMPTY_RESOURCE_LOCATION)){
            tooltipComponents.add(Component.translatable("item.minecraft.jigsaw.name_tooltip", name.toString()));
        }

        if (!targetPool.equals(EMPTY_RESOURCE_LOCATION)){
            tooltipComponents.add(Component.translatable("item.minecraft.jigsaw.target_pool_tooltip", targetName.toString()));
        }

        if (!targetName.equals(EMPTY_RESOURCE_LOCATION)){
            tooltipComponents.add(Component.translatable("item.minecraft.jigsaw.target_name_tooltip", targetName.toString()));
        }
    }
}
