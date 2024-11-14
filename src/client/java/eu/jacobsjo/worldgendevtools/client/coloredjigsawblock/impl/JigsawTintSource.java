package eu.jacobsjo.worldgendevtools.client.coloredjigsawblock.impl;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public record JigsawTintSource(int defaultColor) implements ItemTintSource {
    private static final ResourceLocation EMPTY_RESOURCE_LOCATION = ResourceLocation.withDefaultNamespace("empty");

    public static final MapCodec<JigsawTintSource> MAP_CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(ExtraCodecs.RGB_COLOR_CODEC.fieldOf("default").forGetter(JigsawTintSource::defaultColor)).apply(instance, JigsawTintSource::new)
    );

    @Override
    public int calculate(ItemStack stack) {
        TypedDataComponent<CustomData> blockEntityData = stack.getComponents().getTyped(DataComponents.BLOCK_ENTITY_DATA);
        if (blockEntityData == null)
            return this.defaultColor;

        DataResult<JigsawBlockData> dataResult = blockEntityData.value().read(JigsawBlockData.CODEC);
        if (dataResult.isError() || dataResult.result().isEmpty())
            return this.defaultColor;

        JigsawBlockData data = dataResult.result().get();

        ResourceLocation location = data.name().equals(EMPTY_RESOURCE_LOCATION) ? data.target() : data.name();

        if (location.equals(EMPTY_RESOURCE_LOCATION)) {
            return this.defaultColor;
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

