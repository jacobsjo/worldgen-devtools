package eu.jacobsjo.worldgendevtools.dfcommand.command;

import com.mojang.brigadier.CommandDispatcher;
import eu.jacobsjo.util.TextUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;

import java.text.DecimalFormat;

public final class DfCommand{

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher){
        commandDispatcher.register(
            Commands.literal("getdensity")
                .then(Commands.argument("density_function", ResourceArgument.resource(Registries.DENSITY_FUNCTION))
                    .executes((commandContext)-> getDensityFunctionDensity(commandContext.getSource(), ResourceArgument.getResource(commandContext, "density_function", Registries.DENSITY_FUNCTION), BlockPos.containing(commandContext.getSource().getPosition()), commandContext.getSource().getLevel()))
                    .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .executes((commandContext)-> getDensityFunctionDensity(commandContext.getSource(), ResourceArgument.getResource(commandContext, "density_function", Registries.DENSITY_FUNCTION), BlockPosArgument.getLoadedBlockPos(commandContext, "pos"), commandContext.getSource().getLevel())))
                )
        );
    }

    public static int getDensityFunctionDensity(CommandSourceStack commandSourceStack, Holder<DensityFunction> densityFunctionHolder, BlockPos pos, ServerLevel level){
        ChunkGenerator chunkGenerator = level.getChunkSource().getGenerator();
        return getDensity(commandSourceStack, densityFunctionHolder.value(), pos, chunkGenerator, level);
    }

    public static int getDensity(CommandSourceStack commandSourceStack, DensityFunction densityFunction, BlockPos pos, ChunkGenerator generator, ServerLevel level){
        HolderGetter.Provider registryAccess = level.registryAccess();
        RandomState randomState = RandomState.createForGenerator(registryAccess.lookupOrThrow(Registries.NOISE), level.getSeed(), generator, level);

        DensitySampler sampler = randomState.getSampler(densityFunction);
        double value = sampler.sampleValue(SamplerContext.EMPTY_UNCACHED, pos.getX(), pos.getY(), pos.getZ());

        DecimalFormat format = new DecimalFormat("0.000");

        commandSourceStack.sendSuccess(() -> TextUtil.translatable("worldgendevtools.dfcommand.result", format.format(value)), true);
        return (int) (value * 1000);
    }
}
