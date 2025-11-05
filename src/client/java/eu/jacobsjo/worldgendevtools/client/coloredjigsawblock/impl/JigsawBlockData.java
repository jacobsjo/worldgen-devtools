package eu.jacobsjo.worldgendevtools.client.coloredjigsawblock.impl;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record JigsawBlockData(Identifier name, Identifier target, Identifier targetPool) {
    public final static MapCodec<JigsawBlockData> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    Identifier.CODEC.fieldOf("name").forGetter(JigsawBlockData::name),
                    Identifier.CODEC.fieldOf("target").forGetter(JigsawBlockData::name),
                    Identifier.CODEC.fieldOf("pool").forGetter(JigsawBlockData::name)
            ).apply(instance, JigsawBlockData::new)
    );
}
