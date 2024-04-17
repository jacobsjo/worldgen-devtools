package eu.jacobsjo.worldgendevtools.reloadregistries.impl;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class FrozenHolder<T> implements Holder<T> {
    Holder<T> wrapping;
    T value = null;

    public FrozenHolder(Holder<T> wrapping){
        this.wrapping = wrapping;
    }

    @Override
    public T value() {
        if (value == null){
            value = wrapping.value();
        }
        return value;
    }

    @Override
    public boolean isBound() {
        return value != null || wrapping.isBound();
    }

    @Override
    public boolean is(ResourceLocation location) {
        return wrapping.is(location);
    }

    @Override
    public boolean is(ResourceKey<T> resourceKey) {
        return wrapping.is(resourceKey);
    }

    @Override
    public boolean is(Predicate<ResourceKey<T>> predicate) {
        return wrapping.is(predicate);
    }

    @Override
    public boolean is(TagKey<T> tagKey) {
        return wrapping.is(tagKey);
    }

    @Override @Deprecated
    public boolean is(Holder<T> holder) {
        return wrapping.is(holder);
    }

    @Override
    public Stream<TagKey<T>> tags() {
        return this.wrapping.tags();
    }

    @Override
    public Either<ResourceKey<T>, T> unwrap() {
        return this.wrapping.unwrap();
    }

    @Override
    public Optional<ResourceKey<T>> unwrapKey() {
        return this.wrapping.unwrapKey();
    }

    @Override
    public Kind kind() {
        return this.wrapping.kind();
    }

    @Override
    public boolean canSerializeIn(HolderOwner<T> owner) {
        return this.wrapping.canSerializeIn(owner);
    }
}
