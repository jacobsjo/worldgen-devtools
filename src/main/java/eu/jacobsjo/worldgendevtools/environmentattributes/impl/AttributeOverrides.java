package eu.jacobsjo.worldgendevtools.environmentattributes.impl;

import com.mojang.serialization.Codec;
import eu.jacobsjo.worldgendevtools.environmentattributes.EnvironmentAttributesInit;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Util;
import net.minecraft.world.attribute.*;
import net.minecraft.world.level.Level;

import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public record AttributeOverrides (Map<EnvironmentAttribute<?>, Object> overrides) {
    public static final Codec<AttributeOverrides> CODEC = Codec.lazyInitialized(
            () -> Codec.<EnvironmentAttribute<?>, Object>dispatchedMap(EnvironmentAttributes.CODEC, Util.memoize(AttributeOverrides::createValueCodec))
                    .xmap(AttributeOverrides::new, AttributeOverrides::overrides)
    );

    public AttributeOverrides (final Map<EnvironmentAttribute<?>, Object> overrides) {
        this.overrides = new Reference2ObjectOpenHashMap<>(overrides);
    }

    private static <Value> Codec<Value> createValueCodec(EnvironmentAttribute<Value> environmentAttribute){
        return environmentAttribute.valueCodec();
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, AttributeOverrides> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public AttributeOverrides(){
        this(new Reference2ObjectOpenHashMap<>());
    }

    public AttributeOverrides(AttributeOverrides old){
        this(old.overrides);
    }

    public static void addLayers(EnvironmentAttributeSystem.Builder builder, Level level) {
        BuiltInRegistries.ENVIRONMENT_ATTRIBUTE.entrySet().forEach(entry -> addLayer(builder, level, entry.getValue()));
    }

    private static <Value> void addLayer(EnvironmentAttributeSystem.Builder builder, Level level, EnvironmentAttribute<Value> attribute){
        builder.addTimeBasedLayer(attribute, new AttributeOverride<>(attribute, level));
    }

    public void addOverride(EnvironmentAttribute<?> attribute, Object value){
        overrides.put(attribute, value);
    }

    public void removeOverride(EnvironmentAttribute<?> attribute){
        overrides.remove(attribute);
    }

    private static class AttributeOverride<Value> implements EnvironmentAttributeLayer.TimeBased<Value> {
        EnvironmentAttribute<Value> attribute;
        Level level;
        public AttributeOverride(EnvironmentAttribute<Value> attribute, Level level) {
            this.attribute = attribute;
            this.level = level;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Value applyTimeBased(Value object, int i) {
            AttributeOverrides overrides = this.level.getAttached(EnvironmentAttributesInit.ENVIRONMENT_ATTRIBUTE_OVERRIDES);
            if (overrides != null && overrides.overrides.containsKey(attribute)) {
                return (Value) overrides.overrides.get(this.attribute);
            }
            return object;
        }
    }
}