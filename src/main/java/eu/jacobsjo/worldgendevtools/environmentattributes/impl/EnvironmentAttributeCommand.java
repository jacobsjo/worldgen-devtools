package eu.jacobsjo.worldgendevtools.environmentattributes.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.NbtTagArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.attribute.*;
import net.minecraft.world.phys.Vec3;

public final class EnvironmentAttributeCommand {
    final static SpatialAttributeInterpolator biomeInterpolator = new SpatialAttributeInterpolator();

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, final CommandBuildContext context) {
        commandDispatcher.register(
                Commands.literal("environmentattribute")
                        .then(Commands.literal("get")
                                .then(Commands.argument("environmentAttribute", ResourceArgument.resource(context, Registries.ENVIRONMENT_ATTRIBUTE))
                                        .executes(c -> getEnviromentAttribute(
                                                c.getSource(),
                                                ResourceArgument.getResource(c, "environmentAttribute", Registries.ENVIRONMENT_ATTRIBUTE)
                                        ))
                                )
                        )
                        .then(Commands.literal("set")
                                .then(Commands.argument("environmentAttribute", ResourceArgument.resource(context, Registries.ENVIRONMENT_ATTRIBUTE))
                                        .then(Commands.argument("value", NbtTagArgument.nbtTag())
                                                .executes(c -> setEnviromentAttribute(
                                                        c.getSource(),
                                                        ResourceArgument.getResource(c, "environmentAttribute", Registries.ENVIRONMENT_ATTRIBUTE),
                                                        NbtTagArgument.getNbtTag(c, "value")
                                                ))
                                        )
                                )
                        )
                        .then(Commands.literal("reset")
                                .then(Commands.argument("environmentAttribute", ResourceArgument.resource(context, Registries.ENVIRONMENT_ATTRIBUTE))
                                        .executes(c -> resetEnviromentAttribute(
                                                c.getSource(),
                                                ResourceArgument.getResource(c, "environmentAttribute", Registries.ENVIRONMENT_ATTRIBUTE)
                                        ))
                                )
                        )
                        .then(Commands.literal("resetall")
                                .executes(c -> resetAllEnviromentAttribute(c.getSource()))
                        )
                );
    }

    private static int getEnviromentAttribute(CommandSourceStack stack, Holder<EnvironmentAttribute<?>> environmentAttribute) {
        biomeInterpolator.clear();
        GaussianSampler.sample(
                stack.getPosition().scale(0.25), stack.getLevel().getBiomeManager()::getNoiseBiomeAtQuart, (d, holder) -> biomeInterpolator.accumulate(d, holder.value().getAttributes())
        );

        DataResult<Tag> value = getAndEncodeAttribute(stack.getLevel().environmentAttributes(), stack.getPosition(), environmentAttribute.value());
        stack.sendSuccess(() -> Component.translatable("worldgendevtools.environmentattributes.environmentattribute.get.success", environmentAttribute.getRegisteredName(), NbtUtils.toPrettyComponent(value.getOrThrow())), true);
        return 1;
    }

    private static <T> DataResult<Tag> getAndEncodeAttribute(EnvironmentAttributeReader attributeReader, Vec3 pos, EnvironmentAttribute<T> attribute){
        return attribute.valueCodec().encodeStart(NbtOps.INSTANCE, attributeReader.getValue(attribute, pos, EnvironmentAttributeCommand.biomeInterpolator));
    }

    private static int setEnviromentAttribute(CommandSourceStack stack, Holder<EnvironmentAttribute<?>> environmentAttribute, Tag value){
        DataResult<? extends Pair<?, Tag>> decoded = environmentAttribute.value().valueCodec().decode(NbtOps.INSTANCE, value);
        if (decoded.isError()) {
            stack.sendFailure(Component.translatable("worldgendevtools.environmentattributes.environmentattribute.set.failure", environmentAttribute.getRegisteredName(), decoded.error().get().message()));
            return 0;
        }
        AttributeOverrides.addOverride(environmentAttribute.value(), environmentAttribute.value().valueCodec().decode(NbtOps.INSTANCE, value).getOrThrow().getFirst());
        stack.sendSuccess(() -> Component.translatable("worldgendevtools.environmentattributes.environmentattribute.set.success", environmentAttribute.getRegisteredName()), true);
        return 1;
    }

    private static int resetEnviromentAttribute(CommandSourceStack stack, Holder<EnvironmentAttribute<?>> environmentAttribute){
        AttributeOverrides.removeOverride(environmentAttribute.value());
        stack.sendSuccess(() -> Component.translatable("worldgendevtools.environmentattributes.environmentattribute.reset.success", environmentAttribute.getRegisteredName()), true);
        return 1;
    }

    private static int resetAllEnviromentAttribute(CommandSourceStack stack){
        AttributeOverrides.clearOverrides();
        stack.sendSuccess(() -> Component.translatable("worldgendevtools.environmentattributes.environmentattribute.resetall.success"), true);
        return 1;
    }

}
