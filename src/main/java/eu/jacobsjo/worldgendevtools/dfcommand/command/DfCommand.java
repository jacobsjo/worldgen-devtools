package eu.jacobsjo.worldgendevtools.dfcommand.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import eu.jacobsjo.util.TextUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouterData;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import org.jspecify.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.Optional;

public final class DfCommand{
    private static final DynamicCommandExceptionType ERROR_INVALID_DENSITY_FUNCTION = new DynamicCommandExceptionType((object) -> TextUtil.translatable("worldgendevtools.dfcommand.density_function.invalid", object));
    private static final DynamicCommandExceptionType ERROR_NO_NOISE_ROUTER = new DynamicCommandExceptionType((objcet) -> TextUtil.translatable("worldgendevtools.dfcommand.noise_router.no"));


    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher){
        ArgumentBuilder<CommandSourceStack, LiteralArgumentBuilder<CommandSourceStack>> noiseRouterArgument = Commands.literal("noise_router");

        commandDispatcher.register(
            Commands.literal("getdensity")
                .then(Commands.argument("density_function", ResourceKeyArgument.key(Registries.DENSITY_FUNCTION))
                    .executes((commandContext)-> getDensityFunctionDensity(commandContext.getSource(), getRegistryKeyType(commandContext, "density_function", Registries.DENSITY_FUNCTION, ERROR_INVALID_DENSITY_FUNCTION), BlockPos.containing(commandContext.getSource().getPosition()), commandContext.getSource().getLevel()))
                    .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .executes((commandContext)-> getDensityFunctionDensity(commandContext.getSource(), getRegistryKeyType(commandContext, "density_function", Registries.DENSITY_FUNCTION, ERROR_INVALID_DENSITY_FUNCTION), BlockPosArgument.getLoadedBlockPos(commandContext, "pos"), commandContext.getSource().getLevel())))
                )
        );
    }

    public static int getDensityFunctionDensity(CommandSourceStack commandSourceStack, Holder<DensityFunction> densityFunctionHolder, BlockPos pos, ServerLevel level){
        ChunkGenerator chunkGenerator = level.getChunkSource().getGenerator();

        if (chunkGenerator instanceof NoiseBasedChunkGenerator noiseBasedChunkGenerator) {
            return getDensity(commandSourceStack, densityFunctionHolder.value(), pos, noiseBasedChunkGenerator.generatorSettings().value(), level);
        } else {
            return getDensity(commandSourceStack, densityFunctionHolder.value(), pos, null, level);
        }
    }

    public static int getDensity(CommandSourceStack commandSourceStack, DensityFunction densityFunction, BlockPos pos, @Nullable NoiseGeneratorSettings generatorSettings, ServerLevel level){
        HolderGetter.Provider registryAccess = level.registryAccess();
        RandomState randomState;
        if (generatorSettings != null) {
            randomState = RandomState.create(registryAccess.lookupOrThrow(Registries.NOISE), level.getSeed(), generatorSettings);
        } else {
            randomState = RandomState.create(registryAccess.lookupOrThrow(Registries.NOISE), level.getSeed(), false, 63, NoiseRouterData.none());
        }

        DensitySampler sampler = randomState.getSampler(densityFunction);
        double value = sampler.sampleValue(SamplerContext.EMPTY_UNCACHED, pos.getX(), pos.getY(), pos.getZ());

        DecimalFormat format = new DecimalFormat("0.000");

        commandSourceStack.sendSuccess(() -> TextUtil.translatable("worldgendevtools.dfcommand.result", format.format(value)), true);
        return (int) (value * 1000);
    }

    private static <T> ResourceKey<T> getRegistryType(CommandContext<CommandSourceStack> commandContext, String string, ResourceKey<Registry<T>> resourceKey, DynamicCommandExceptionType dynamicCommandExceptionType) throws CommandSyntaxException {
        ResourceKey<?> resourceKey2 = commandContext.getArgument(string, ResourceKey.class);
        Optional<ResourceKey<T>> optional = resourceKey2.cast(resourceKey);
        return optional.orElseThrow(() -> dynamicCommandExceptionType.create(resourceKey2));
    }

    private static <T> Registry<T> getRegistry(CommandContext<CommandSourceStack> commandContext, ResourceKey<? extends Registry<T>> resourceKey) {
        return commandContext.getSource().getServer().registryAccess().lookupOrThrow(resourceKey);
    }

    @SuppressWarnings("SameParameterValue")
    private static <T> Holder<T> getRegistryKeyType(CommandContext<CommandSourceStack> commandContext, String string, ResourceKey<Registry<T>> resourceKey, DynamicCommandExceptionType dynamicCommandExceptionType) throws CommandSyntaxException {
        ResourceKey<T> resourceKey2 = getRegistryType(commandContext, string, resourceKey, dynamicCommandExceptionType);
        return getRegistry(commandContext, resourceKey).get(resourceKey2).orElseThrow(() -> dynamicCommandExceptionType.create(resourceKey2.identifier().toString()));
    }
}
