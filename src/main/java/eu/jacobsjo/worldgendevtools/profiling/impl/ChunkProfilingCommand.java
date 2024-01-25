package eu.jacobsjo.worldgendevtools.profiling.impl;

import com.mojang.brigadier.CommandDispatcher;
import eu.jacobsjo.worldgendevtools.profiling.ProfilingInit;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.chunk.ChunkAccess;

public class ChunkProfilingCommand {
    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register(
                Commands.literal("chunkprofiling")
                        .executes((commandContext) -> getProfiling(commandContext.getSource()))
        );
    }

    public static int getProfiling(CommandSourceStack source){
        ChunkAccess chunk = source.getLevel().getChunk(BlockPos.containing(source.getPosition()));

        ChunkgenProfilingInformation information = chunk.getAttached(ProfilingInit.PROFILING_ATTACHMENT);
        if (information == null){
            source.sendFailure(Component.literal("No generation profiling saved for chunk"));
            return 0;
        }

        source.sendSuccess(() -> Component.literal(information.toString()), true);
        return 1;
    }
}