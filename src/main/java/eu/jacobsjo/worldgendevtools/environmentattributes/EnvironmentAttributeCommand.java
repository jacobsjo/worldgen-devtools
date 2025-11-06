package eu.jacobsjo.worldgendevtools.environmentattributes;

import com.google.gson.JsonElement;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import eu.jacobsjo.util.TextUtil;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.attribute.*;
import net.minecraft.world.phys.Vec3;

public final class EnvironmentAttributeCommand {
    final static SpatialAttributeInterpolator biomeInterpolator = new SpatialAttributeInterpolator();

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, final CommandBuildContext context) {
        commandDispatcher.register(
                Commands.literal("environmentattribute")
                        .then(Commands.literal("get")
                                .then(Commands.argument("environmentAttribute", ResourceArgument.resource(context, Registries.ENVIRONMENT_ATTRIBUTE))
                                        .executes(c -> getEnviromentAttribute(c.getSource(), ResourceArgument.getResource(c, "environmentAttribute", Registries.ENVIRONMENT_ATTRIBUTE)))
                                )
                        )
        );
    }

    private static int getEnviromentAttribute(CommandSourceStack stack, Holder<EnvironmentAttribute<?>> environmentAttribute) {
        biomeInterpolator.clear();
        GaussianSampler.sample(
                stack.getPosition().scale(0.25), stack.getLevel().getBiomeManager()::getNoiseBiomeAtQuart, (d, holder) -> biomeInterpolator.accumulate(d, holder.value().getAttributes())
        );

        DataResult<Tag> value = getAndEncodeAttribute(stack.getLevel().environmentAttributes(), stack.getPosition(), environmentAttribute.value(), biomeInterpolator);
        stack.sendSuccess(() -> Component.translatable("worldgendevtools.environmentattributes.environmentattribute.get.success", environmentAttribute.getRegisteredName(), NbtUtils.toPrettyComponent(value.getOrThrow())), true);
        return 0;
    }

    private static <T> DataResult<Tag> getAndEncodeAttribute(EnvironmentAttributeReader attributeReader, Vec3 pos, EnvironmentAttribute<T> attribute, SpatialAttributeInterpolator biomeInterpolator){
        return attribute.valueCodec().encodeStart(NbtOps.INSTANCE, attributeReader.getValue(attribute, pos, biomeInterpolator));
    }

}
