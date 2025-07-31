package eu.jacobsjo.worldgendevtools.client.coloredjigsawblock.impl;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public record JigsawTintSource(int defaultColor) implements ItemTintSource {
    private static final ResourceLocation EMPTY_RESOURCE_LOCATION = ResourceLocation.withDefaultNamespace("empty");

    public static final MapCodec<JigsawTintSource> MAP_CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(ExtraCodecs.RGB_COLOR_CODEC.fieldOf("default").forGetter(JigsawTintSource::defaultColor)).apply(instance, JigsawTintSource::new)
    );

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity) {
        TypedEntityData<BlockEntityType<?>> blockEntityData = stack.getComponents().get(DataComponents.BLOCK_ENTITY_DATA);
        if (blockEntityData == null)
            return 0xFF000000 | this.defaultColor;

        @SuppressWarnings("deprecation")
        DataResult<MapLike<Tag>> dataResult = NbtOps.INSTANCE.getMap(blockEntityData.getUnsafe());
        if (dataResult.isError() || dataResult.result().isEmpty())
            return 0xFF000000 | this.defaultColor;

        DataResult<JigsawBlockData> jigsawDataResult = JigsawBlockData.CODEC.decode(NbtOps.INSTANCE, dataResult.getOrThrow());
        if (jigsawDataResult.isError() || jigsawDataResult.result().isEmpty())
            return 0xFF000000 | this.defaultColor;

        JigsawBlockData data = jigsawDataResult.result().get();

        ResourceLocation location = data.name().equals(EMPTY_RESOURCE_LOCATION) ? data.target() : data.name();

        if (location.equals(EMPTY_RESOURCE_LOCATION)) {
            return 0xFF000000 | this.defaultColor;
        }

        int hash = location.toString().hashCode();
        int r = hash & 0xFF;
        int g = hash >> 8 & 0xFF;
        int b = hash >> 16 & 0xFF;

        return 0xFF000000 | r << 16 | g << 8 | b;
    }

    @Override
    @NotNull
    public MapCodec<JigsawTintSource> type() {
        return MAP_CODEC;
    }
}

