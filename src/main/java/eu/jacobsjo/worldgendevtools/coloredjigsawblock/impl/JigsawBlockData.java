package eu.jacobsjo.worldgendevtools.coloredjigsawblock.impl;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record JigsawBlockData(ResourceLocation name, ResourceLocation target, ResourceLocation targetPool) {
    public final static MapCodec<JigsawBlockData> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    ResourceLocation.CODEC.fieldOf("name").forGetter(JigsawBlockData::name),
                    ResourceLocation.CODEC.fieldOf("target").forGetter(JigsawBlockData::name),
                    ResourceLocation.CODEC.fieldOf("pool").forGetter(JigsawBlockData::name)
            ).apply(instance, JigsawBlockData::new)
    );
}
