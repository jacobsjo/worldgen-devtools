package eu.jacobsjo.worldgendevtools.locatefeature.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.logging.LogUtils;
import eu.jacobsjo.util.TextUtil;
import eu.jacobsjo.worldgendevtools.locatefeature.LocateFeatureInit;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.slf4j.Logger;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@SuppressWarnings("UnstableApiUsage")
public class LocateFeature {
    private static final int MAX_RANGE = 10;
    private static final Logger LOGGER = LogUtils.getLogger();

    public static LiteralArgumentBuilder<CommandSourceStack> addSubcommand(LiteralArgumentBuilder<CommandSourceStack> command){
        return command.then(
            Commands.literal("feature")
                .then(
                    Commands.argument("configured_feature", ResourceKeyArgument.key(Registries.CONFIGURED_FEATURE))
                        .executes(
                            commandContext -> locateFeature(
                                commandContext.getSource(),  ResourceKeyArgument.getConfiguredFeature(commandContext, "configured_feature").key()
                            )
                        )
                )
        );

    }
    public static int locateFeature(CommandSourceStack source, ResourceKey<ConfiguredFeature<?, ?>> feature ){
        try {
            BlockPos sourcePos = BlockPos.containing(source.getPosition());
            ChunkAccess chunk = source.getLevel().getChunk(sourcePos);
            FeaturePositions featurePositions = chunk.getAttached(LocateFeatureInit.FEATURE_POSITION_ATTACHMENT);
            if (featurePositions == null) {
                source.sendFailure(TextUtil.translatable("worldgendevtools.locatefeature.command.failure.no_attachment"));
                return 0;
            }

            List<BlockPos> positions = featurePositions.getPositions(feature);

            if (positions.isEmpty()) {
                return locateNearbyFeature(source, feature, sourcePos, chunk.getPos());
            } else {
                return showListResult(source, feature.location(), positions, sourcePos);
            }
        } catch (Exception e){
            LOGGER.error("Exception running command" ,e);
            throw e;
        }
    }

    public static int locateNearbyFeature(CommandSourceStack source, ResourceKey<ConfiguredFeature<?, ?>> feature, BlockPos sourcePos, ChunkPos centerChunkPos ){
        for (int range = 1 ; range <= MAX_RANGE ; range++){
            Stream<BlockPos> foundPositions = Stream.empty();
            for (int x = -range ; x <= range ; x++){
                for (int z = -range ; z <= range ; z++) {
                    if (Math.abs(x) < range && Math.abs(z) < range) continue;

                    ChunkAccess chunk = source.getLevel().getChunk(centerChunkPos.x + x, centerChunkPos.z + z, ChunkStatus.EMPTY);
                    FeaturePositions featurePositions = chunk.getAttached(LocateFeatureInit.FEATURE_POSITION_ATTACHMENT);
                    if (featurePositions == null) continue;

                    foundPositions = Stream.concat(foundPositions, featurePositions.getPositions(feature).stream());

                }
            }
            Optional<BlockPos> minPos = foundPositions.min(Comparator.comparingDouble(pos -> pos.distSqr(sourcePos)));
            if (minPos.isPresent()){
                return showLocateResult(source, feature.location(), minPos.get(), sourcePos);
            }
        }
        source.sendFailure(TextUtil.translatable("worldgendevtools.locatefeature.command.failure.not_nearby", feature.location().toString()));
        return -1;
    }

    private static int showListResult(CommandSourceStack source, ResourceLocation location, List<BlockPos> positions, BlockPos sourcePos){

        List<BlockPos> sortedPositions = positions.stream().sorted(Comparator.comparingDouble(pos -> pos.distSqr(sourcePos))).limit(5).toList();

        Component positionsComponent = ComponentUtils.formatList(
                sortedPositions.stream().map(pos -> ComponentUtils.wrapInSquareBrackets(Component.translatable("chat.coordinates", pos.getX(), pos.getY(), pos.getZ()))
                        .withStyle(
                                style -> style.withColor(ChatFormatting.GREEN)
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + pos.getX() + " " + pos.getY() + " " + pos.getZ()))
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.coordinates.tooltip")))
                        )).toList(),
                Component.literal(", ")
        );

        String translationKey = (positions.size() == 1 ? "single" : (positions.size() <= 5) ? "multiple" : "many");

        source.sendSuccess(() -> TextUtil.translatable("worldgendevtools.locatefeature.command.success." + translationKey, location.toString(), positions.size(), positionsComponent, positions.size() - 5), true);

        return (int) Math.round(Math.sqrt(sortedPositions.getFirst().distSqr(sourcePos)));
    }

    private static int showLocateResult(
            CommandSourceStack source,
            ResourceLocation location,
            BlockPos position,
            BlockPos sourcePosition
    ) {
        int dist = Mth.floor(Mth.sqrt((float)sourcePosition.distSqr(position)));
        Component component = ComponentUtils.wrapInSquareBrackets(Component.translatable("chat.coordinates", position.getX(), position.getY(), position.getZ()))
                .withStyle(
                        style -> style.withColor(ChatFormatting.GREEN)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + position.getX() + " " + position.getY() + " " + position.getZ()))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.coordinates.tooltip")))
                );
        source.sendSuccess(() -> TextUtil.translatable("worldgendevtools.locatefeature.command.success.nearby", location.toString(), component, dist), false);
        return dist;
    }
}
