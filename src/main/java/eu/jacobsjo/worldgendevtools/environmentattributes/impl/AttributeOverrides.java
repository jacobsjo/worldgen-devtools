package eu.jacobsjo.worldgendevtools.environmentattributes.impl;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import eu.jacobsjo.worldgendevtools.locatefeature.impl.FeaturePositions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Util;
import net.minecraft.world.attribute.*;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class AttributeOverrides {
    public static final Codec<AttributeOverrides> CODEC = Codec.lazyInitialized(
            () -> Codec.dispatchedMap(EnvironmentAttributes.CODEC, Util.memoize(EnvironmentAttributeMap.Entry::createCodec))
                    .xmap(EnvironmentAttributeMap::new, environmentAttributeMap -> environmentAttributeMap.entries)
    );


    public static final StreamCodec<RegistryFriendlyByteBuf, AttributeOverrides> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries()

            StreamCodec.composite(
            FeaturePositions.PositionsOfFeature.STREAM_CODEC.apply(ByteBufCodecs.list()), FeaturePositions::getList,
            FeaturePositions::new
    );

    private final Map<EnvironmentAttribute<?>, Object> overrides = new HashMap<>();
//    public static final Logger LOGGER = LogUtils.getLogger();

    public void addLayers(EnvironmentAttributeSystem.Builder builder) {
        BuiltInRegistries.ENVIRONMENT_ATTRIBUTE.entrySet().forEach(entry -> builder.addTimeBasedLayer(entry.getValue(), new AttributeOverride(entry.getValue())));
    }

    public void addOverride(EnvironmentAttribute<?> attribute, Object value){
        overrides.put(attribute, value);
    }

    public void removeOverride(EnvironmentAttribute<?> attribute){
        overrides.remove(attribute);
    }

    public void clearOverrides(){
        overrides.clear();
    }

    private class AttributeOverride<Value> implements EnvironmentAttributeLayer.TimeBased<Value> {
        EnvironmentAttribute<Value> attribute;
        public AttributeOverride(EnvironmentAttribute<Value> attribute) {
            this.attribute = attribute;
        }

        @Override
        public Value applyTimeBased(Value object, int i) {
            if (AttributeOverrides.this.overrides.containsKey(attribute)) {
                return (Value) AttributeOverrides.this.overrides.get(this.attribute);
            }
            return object;
        }
    }
}