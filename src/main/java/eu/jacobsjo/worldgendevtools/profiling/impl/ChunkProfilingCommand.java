package eu.jacobsjo.worldgendevtools.profiling.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import eu.jacobsjo.worldgendevtools.profiling.ProfilingInit;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ChunkProfilingCommand {

    public static int HEADER_COLOR = 0x80FF80;
    public static int KEY_COLOR = 0x8080FF;
    public static int NUMBER_COLOR = 0xFFFFFF;
    public static int TEXT_COLOR = 0xA0A0A0;


    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register(
                Commands.literal("chunkprofiling")
                        .executes((commandContext) -> getProfiling(commandContext.getSource(), 0))
                        .then((Commands.argument("range", IntegerArgumentType.integer(0, 8))
                                .executes(commandContext -> getProfiling(commandContext.getSource(), IntegerArgumentType.getInteger(commandContext, "range")))))

        );
    }

    public static int getProfiling(CommandSourceStack source, int range){
        ChunkPos centerChunkPos = new ChunkPos(BlockPos.containing(source.getPosition()));

        List<ChunkgenProfilingInformation> informations = new ArrayList<>();

        for (int x = -range; x <= range; x++) {
            for (int z = -range; z <= range; z++) {
                ChunkAccess chunk = source.getLevel().getChunk(centerChunkPos.x + x, centerChunkPos.z + z);
                ChunkgenProfilingInformation info = chunk.getAttached(ProfilingInit.PROFILING_ATTACHMENT);
                if (info != null) {
                    informations.add(info);
                }
            }
        }

        if (informations.size() == 0) {
            source.sendFailure(Component.translatable ("worldgendevtools.profiling.command.no_information"));
            return 0;
        } else {
            ChunkgenProfilingInformation sum = ChunkgenProfilingInformation.sum(informations);

            MutableComponent message = Component.literal("");

            int chunkCount = informations.size();
            if (chunkCount == 1) {
                message.append(Component.translatable("worldgendevtools.profiling.command.header.one", centerChunkPos.toString()).withColor(HEADER_COLOR));
            } else {
                message.append(Component.translatable("worldgendevtools.profiling.command.header", chunkCount).withColor(HEADER_COLOR));
            }

            BuiltInRegistries.CHUNK_STATUS
                    .entrySet()
                    .stream()
                    .filter(e -> !e.getValue().equals(ChunkStatus.EMPTY))
                    .sorted(Comparator.comparingInt(e -> e.getValue().getIndex()))
                    .map(e -> e.getKey().location())
                    .forEach(loc -> {
                        Duration duration = sum.getStatusDuration(loc);
                        Duration average = duration.dividedBy(informations.size());
                        message.append("\n");
                        message.append(Component.translatable("worldgendevtools.profiling.command.entry.key",
                                loc.getPath()
                        ).withColor(KEY_COLOR));
                        if (chunkCount == 1){
                            message.append(Component.translatable("worldgendevtools.profiling.command.entry.value.single",
                                    Component.literal(String.format("%.2f", duration.toNanos() * 1e-6)).withColor(NUMBER_COLOR)
                            ).withColor(TEXT_COLOR));
                        } else {
                            message.append(Component.translatable("worldgendevtools.profiling.command.entry.value",
                                    Component.literal(String.format("%.2f", average.toNanos() * 1e-6)).withColor(NUMBER_COLOR),
                                    Component.literal(String.format("%.2f", duration.toNanos() * 1e-6)).withColor(NUMBER_COLOR)
                            ).withColor(TEXT_COLOR));
                        }
                    });

            source.sendSuccess(() -> message, true);
        }

        return 1;
    }
}