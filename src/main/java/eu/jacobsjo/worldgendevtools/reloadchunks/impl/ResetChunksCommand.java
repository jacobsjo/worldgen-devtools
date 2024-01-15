package eu.jacobsjo.worldgendevtools.reloadchunks.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.datafixers.util.Unit;
import com.mojang.logging.LogUtils;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.Heightmap;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class ResetChunksCommand {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                (Commands.literal("resetchunks")
                    .requires(commandSourceStack -> commandSourceStack.hasPermission(4)))
                    .executes(commandContext -> resetChunks(commandContext.getSource(), 0))
                    .then((Commands.argument("range", IntegerArgumentType.integer(0, 5))
                    .executes(commandContext -> resetChunks(commandContext.getSource(), IntegerArgumentType.getInteger(commandContext, "range")))))
        );
    }



    private static int resetChunks(CommandSourceStack source, int range) {
        ServerLevel level = source.getLevel();

        ServerChunkCache chunkCache = level.getChunkSource();
        ChunkPos centerChunkPos = new ChunkPos(BlockPos.containing(source.getPosition()));

        long startMills = System.currentTimeMillis();

        long setToAirStartMills = System.currentTimeMillis();
        for (int z = -range; z <= range; ++z) {
            for (int x = -range; x <= range; ++x) {
                ChunkPos chunkPos = new ChunkPos(centerChunkPos.x + x, centerChunkPos.z + z);
                LevelChunk levelChunk = chunkCache.getChunk(chunkPos.x, chunkPos.z, false);
                if (levelChunk == null) continue;
                for (BlockPos blockPos : BlockPos.betweenClosed(chunkPos.getMinBlockX(), level.getMinBuildHeight(), chunkPos.getMinBlockZ(), chunkPos.getMaxBlockX(), level.getMaxBuildHeight() - 1, chunkPos.getMaxBlockZ())) {
                    level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 16);
                }
            }
        }
        LOGGER.info("Set chunks to air took " + (System.currentTimeMillis() - setToAirStartMills) + " ms");

        ProcessorMailbox<Runnable> mailbox = ProcessorMailbox.create(Util.backgroundExecutor(), "worldgen-resetchunks");

        for (ChunkStatus chunkStatus : ImmutableList.of(ChunkStatus.BIOMES, ChunkStatus.NOISE, ChunkStatus.SURFACE, ChunkStatus.CARVERS, ChunkStatus.FEATURES, ChunkStatus.INITIALIZE_LIGHT)) {
            long statusStartMillis = System.currentTimeMillis();

            CompletableFuture<Unit> future = CompletableFuture.supplyAsync(() -> Unit.INSTANCE, mailbox::tell);
            for (int z = centerChunkPos.z - range; z <= centerChunkPos.z + range; ++z) {
                for (int x = centerChunkPos.x - range; x <= centerChunkPos.x + range; ++x) {
                    ChunkPos chunkPos = new ChunkPos(x, z);
                    LevelChunk chunk = chunkCache.getChunk(x, z, false);
                    if (chunk == null) continue;
                    ArrayList<ChunkAccess> cache = Lists.newArrayList();
                    int chunkRange = Math.max(1, chunkStatus.getRange());
                    for (int u = chunkPos.z - chunkRange; u <= chunkPos.z + chunkRange; ++u) {
                        for (int v = chunkPos.x - chunkRange; v <= chunkPos.x + chunkRange; ++v) {
                            ChunkAccess chunkAccess = chunkCache.getChunk(v, u, chunkStatus.getParent(), true);
                            ChunkAccess protoChunk = chunkAccess instanceof ImposterProtoChunk ? new ImposterProtoChunk(((ImposterProtoChunk)chunkAccess).getWrapped(), true) : (chunkAccess instanceof LevelChunk ? new ImposterProtoChunk((LevelChunk)chunkAccess, true) : chunkAccess);
                            cache.add(protoChunk);
                        }
                    }
                    future = future.thenComposeAsync(unit -> chunkStatus.generate(mailbox::tell, level, chunkCache.getGenerator(), level.getStructureManager(), chunkCache.getLightEngine(), chunkAccess -> {
                        throw new UnsupportedOperationException("Not creating full chunks here");
                    }, cache).thenApply(either -> {
                        if (chunkStatus == ChunkStatus.NOISE) {
                            either.left().ifPresent(chunkAccess -> Heightmap.primeHeightmaps(chunkAccess, ChunkStatus.POST_FEATURES));
                        }
                        return Unit.INSTANCE;
                    }), mailbox::tell);
                }
            }
            CompletableFuture<Unit> finalCompletableFuture = future;
            source.getServer().managedBlock(finalCompletableFuture::isDone);
            LOGGER.info(chunkStatus + " took " + (System.currentTimeMillis() - statusStartMillis) + " ms");
        }
        long w = System.currentTimeMillis();
        for (int z = centerChunkPos.z - range; z <= centerChunkPos.z + range; ++z) {
            for (int x = centerChunkPos.x - range; x <= centerChunkPos.x + range; ++x) {
                ChunkPos chunkPos4 = new ChunkPos(x, z);
                LevelChunk levelChunk3 = chunkCache.getChunk(x, z, false);
                if (levelChunk3 == null) continue;
                for (BlockPos blockPos2 : BlockPos.betweenClosed(chunkPos4.getMinBlockX(), level.getMinBuildHeight(), chunkPos4.getMinBlockZ(), chunkPos4.getMaxBlockX(), level.getMaxBuildHeight() - 1, chunkPos4.getMaxBlockZ())) {
                    chunkCache.blockChanged(blockPos2);
                }
            }
        }
        LOGGER.info("blockChanged took " + (System.currentTimeMillis() - w) + " ms");
        long q = System.currentTimeMillis() - startMills;
        int p = (range * 2 + 1) * (range * 2 + 1);
        source.sendSuccess(() -> Component.literal(String.format(Locale.ROOT, "%d chunks have been reset. This took %d ms for %d chunks, or %02f ms per chunk", p, q, p, Float.valueOf((float)q / (float)p))), true);
        return 1;
    }

    /*
    private static int resetChunks(CommandSourceStack source, int range){


        ChunkMap chunkMap = source.getLevel().getChunkSource().chunkMap;

        ChunkPos centerChunkPos = new ChunkPos(BlockPos.containing(source.getPosition()));


        if (!((ForceUnloadableChunkMap) chunkMap).worldgenDevtools$regenerateChunks(centerChunkPos, range)) {
            source.sendFailure(Component.literal("Failed to reset chunks!"));
        };

        return 0;
    }*/
}
