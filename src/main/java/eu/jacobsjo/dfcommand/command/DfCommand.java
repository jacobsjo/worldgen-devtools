package eu.jacobsjo.dfcommand.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import eu.jacobsjo.dfcommand.RandomState;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

public final class DfCommand{

    private static final DynamicCommandExceptionType ERROR_INVALID_DENSITY_FUNCTION = new DynamicCommandExceptionType((object) -> {
        return Component.translatable("commands.jacobsjo.getdensity.density_function.invalid", object);
    });

    private static final DynamicCommandExceptionType ERROR_INVALID_NOISE_ROUTER = new DynamicCommandExceptionType((object) -> {
        return Component.translatable("commands.jacobsjo.getdensity.noise_router.invalid", object);
    });

    private static final DynamicCommandExceptionType ERROR_NO_NOISE_ROUTER = new DynamicCommandExceptionType((objcet) -> {
        return Component.translatable("commands.jacobsjo.getdensity.noise_router.no");
    });

    private static final Collection<String> NOISE_ROUTER_VALUES = Arrays.asList(
            "barrier",
            "fluid_level_floodedness",
            "fluid_level_spread",
            "lava",
            "temperature",
            "vegetation",
            "continents",
            "erosion",
            "depth",
            "ridges",
            "initial_density_without_jaggedness",
            "final_density",
            "vein_toggle",
            "vein_ridged",
            "vein_gap"
    );


    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher){
        ArgumentBuilder<CommandSourceStack, LiteralArgumentBuilder<CommandSourceStack>> noiseRouterArgument = Commands.literal("noise_router");

        for (String value : NOISE_ROUTER_VALUES){
            noiseRouterArgument = noiseRouterArgument.then(Commands.literal(value)
                    .executes((commandContext)->{
                        return getDensity(commandContext.getSource(), value, new BlockPos(commandContext.getSource().getPosition()), commandContext.getSource().getLevel());
                    })
                    .then(Commands.argument("pos", BlockPosArgument.blockPos())
                            .executes((commandContext)-> {
                                return getDensity(commandContext.getSource(), value, BlockPosArgument.getLoadedBlockPos(commandContext, "pos"), commandContext.getSource().getLevel());
                            })
                    ));
        }

        commandDispatcher.register(
            Commands.literal("getdensity")
                    .then(Commands.literal("density_function")
                        .then(Commands.argument("density_function", ResourceKeyArgument.key(Registry.DENSITY_FUNCTION_REGISTRY))
                            .executes((commandContext)->{
                                return getDensity(commandContext.getSource(), getRegistryKeyType(commandContext, "density_function", Registry.DENSITY_FUNCTION_REGISTRY, ERROR_INVALID_DENSITY_FUNCTION), new BlockPos(commandContext.getSource().getPosition()), commandContext.getSource().getLevel());
                            })
                            .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes((commandContext)->{
                                    return getDensity(commandContext.getSource(), getRegistryKeyType(commandContext, "density_function", Registry.DENSITY_FUNCTION_REGISTRY, ERROR_INVALID_DENSITY_FUNCTION), BlockPosArgument.getLoadedBlockPos(commandContext, "pos"), commandContext.getSource().getLevel());
                                }))
                        )
                    )
                    .then(noiseRouterArgument)
        );
    }

    public static int getDensity(CommandSourceStack commandSourceStack, String routerDensityFunction, BlockPos pos, ServerLevel level) throws CommandSyntaxException {
        ChunkGenerator chunkGenerator = level.getChunkSource().getGenerator();
        RandomState randomState;
        if (chunkGenerator instanceof NoiseBasedChunkGenerator noiseBasedChunkGenerator) {
            NoiseRouter router = noiseBasedChunkGenerator.generatorSettings().value().noiseRouter();
            DensityFunction densityFunction = switch (routerDensityFunction) {
                case "barrier" -> router.barrierNoise();
                case "fluid_level_floodedness" -> router.fluidLevelFloodednessNoise();
                case "fluid_level_spread" -> router.fluidLevelSpreadNoise();
                case "lava" -> router.lavaNoise();
                case "temperature" -> router.temperature();
                case "vegetation" -> router.vegetation();
                case "continents" -> router.continents();
                case "erosion" -> router.erosion();
                case "depth" -> router.depth();
                case "ridges" -> router.ridges();
                case "initial_density_without_jaggedness" -> router.initialDensityWithoutJaggedness();
                case "final_density" -> router.finalDensity();
                case "vein_toggle" -> router.veinToggle();
                case "vein_ridged" -> router.veinRidged();
                case "vein_gap" -> router.veinGap();
                default -> throw ERROR_INVALID_NOISE_ROUTER.create(routerDensityFunction);
            };

            return getDensity(commandSourceStack, densityFunction, pos, noiseBasedChunkGenerator.generatorSettings().value(), level);
        } else {
            throw ERROR_NO_NOISE_ROUTER.create(routerDensityFunction);
        }
    }

    public static int getDensity(CommandSourceStack commandSourceStack, Holder<DensityFunction> densityFunctionHolder, BlockPos pos, ServerLevel level){
        ChunkGenerator chunkGenerator = level.getChunkSource().getGenerator();

        if (chunkGenerator instanceof NoiseBasedChunkGenerator noiseBasedChunkGenerator) {
            return getDensity(commandSourceStack, densityFunctionHolder.value(), pos, noiseBasedChunkGenerator.generatorSettings().value(), level);
        } else {
            return getDensity(commandSourceStack, densityFunctionHolder.value(), pos, NoiseGeneratorSettings.dummy(), level);
        }
    }

    public static int getDensity(CommandSourceStack commandSourceStack, DensityFunction densityFunction, BlockPos pos, NoiseGeneratorSettings generatorSettings, ServerLevel level){
        RandomState randomState = RandomState.create(generatorSettings, level.registryAccess().registryOrThrow(Registry.NOISE_REGISTRY), level.getSeed());

        double value = densityFunction.mapAll(randomState.getVisitor()).compute( new DensityFunction.SinglePointContext(pos.getX(), pos.getY(), pos.getZ()));

        DecimalFormat format = new DecimalFormat("0.000");

        commandSourceStack.sendSuccess(Component.translatable("commands.jacobsjo.getdensity.result", format.format(value)), true);
        return (int) (value * 1000);
    }

    private static <T> ResourceKey<T> getRegistryType(CommandContext<CommandSourceStack> commandContext, String string, ResourceKey<Registry<T>> resourceKey, DynamicCommandExceptionType dynamicCommandExceptionType) throws CommandSyntaxException {
        ResourceKey<?> resourceKey2 = commandContext.getArgument(string, ResourceKey.class);
        Optional<ResourceKey<T>> optional = resourceKey2.cast(resourceKey);
        return optional.orElseThrow(() -> {
            return dynamicCommandExceptionType.create(resourceKey2);
        });
    }

    private static <T> Registry<T> getRegistry(CommandContext<CommandSourceStack> commandContext, ResourceKey<? extends Registry<T>> resourceKey) {
        return ((CommandSourceStack)commandContext.getSource()).getServer().registryAccess().registryOrThrow(resourceKey);
    }

    private static <T> Holder<T> getRegistryKeyType(CommandContext<CommandSourceStack> commandContext, String string, ResourceKey<Registry<T>> resourceKey, DynamicCommandExceptionType dynamicCommandExceptionType) throws CommandSyntaxException {
        ResourceKey<T> resourceKey2 = getRegistryType(commandContext, string, resourceKey, dynamicCommandExceptionType);
        return getRegistry(commandContext, resourceKey).getHolder(resourceKey2).orElseThrow(() -> {
            return dynamicCommandExceptionType.create(resourceKey2.location());
        });
    }
}
