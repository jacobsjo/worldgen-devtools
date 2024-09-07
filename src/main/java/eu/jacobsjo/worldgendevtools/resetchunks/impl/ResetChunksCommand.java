package eu.jacobsjo.worldgendevtools.resetchunks.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.logging.LogUtils;
import eu.jacobsjo.util.TextUtil;
import eu.jacobsjo.worldgendevtools.resetchunks.api.ResettableChunkMap;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;

public class ResetChunksCommand {
    public static final Logger LOGGER = LogUtils.getLogger();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                (Commands.literal("resetchunks")
                    .requires(commandSourceStack -> commandSourceStack.hasPermission(4)))
                    .executes(commandContext -> resetChunks(commandContext.getSource(), 0))
                    .then((Commands.argument("range", IntegerArgumentType.integer(0, 8))
                    .executes(commandContext -> resetChunks(commandContext.getSource(), IntegerArgumentType.getInteger(commandContext, "range")))))
        );
    }

    private static int resetChunks(CommandSourceStack source, int range){
        int chunkCount = (range * 2 + 1) * (range * 2 + 1);

        ServerLevel level = source.getLevel();
        ChunkMap chunkMap = level.getChunkSource().chunkMap;
        ChunkPos centerChunkPos = new ChunkPos(BlockPos.containing(source.getPosition()));

        // discard entities in affected chunks
        ChunkPos minChunkPos = new ChunkPos(centerChunkPos.x - range, centerChunkPos.z - range);
        ChunkPos maxChunkPos = new ChunkPos(centerChunkPos.x + range, centerChunkPos.z + range);
        AABB aabb = new AABB(minChunkPos.getMinBlockX(), level.getMinY(), minChunkPos.getMinBlockZ(), maxChunkPos.getMaxBlockX(), level.getMaxY(), maxChunkPos.getMaxBlockZ());
        level.getEntitiesOfClass(Entity.class, aabb, entity -> !(entity instanceof Player)).forEach(Entity::discard);

        // reset chunks
        for (int x = -range; x <= range ; x++) {
            for (int z = -range; z <= range; z++) {
                ChunkPos chunkPos = new ChunkPos(centerChunkPos.x + x, centerChunkPos.z + z);
                boolean success = ((ResettableChunkMap) chunkMap).worldgenDevtools$resetChunk(chunkPos);
                if (!success){
                    source.sendFailure(TextUtil.translatable("worldgendevtools.resetchunks.failed", chunkPos));
                    return 0;
                }
            }
        }

        source.sendSuccess(() -> TextUtil.translatable( "worldgendevtools.resetchunks.success", chunkCount), true);
        return chunkCount;
    }
}
