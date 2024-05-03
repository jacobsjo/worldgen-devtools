package eu.jacobsjo.worldgendevtools.worldgensettings;

import eu.jacobsjo.util.TextUtil;
import eu.jacobsjo.worldgendevtools.worldgensettings.api.GenerationOptions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.gamerule.v1.rule.EnumRule;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;

public class WorldgenSettingsInit implements ModInitializer {
    public static GameRules.Key<EnumRule<GenerationOptions>> MAX_CHUNK_STATUS;
    public static GameRules.Key<GameRules.BooleanValue> APPLY_PROCESSOR_LISTS;
    public static GameRules.Key<GameRules.BooleanValue> KEEP_JIGSAWS;
    public static GameRules.Key<GameRules.BooleanValue> APPLY_GRAVITY_PROCESSOR;
    public static GameRules.Key<GameRules.BooleanValue> SAVE_CHUNKS;

    public static final Component NO_SAVE_WARNING = TextUtil.translatable("worldgendevtools.worldgensettings.no_save_warning").withStyle(ChatFormatting.RED);

    @Override
    public void onInitialize() {
        MAX_CHUNK_STATUS = GameRuleRegistry.register("maxChunkStatus", GameRules.Category.MISC, GameRuleFactory.createEnumRule(GenerationOptions.ALL));
        APPLY_PROCESSOR_LISTS = GameRuleRegistry.register("applyProcessorLists", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));
        KEEP_JIGSAWS = GameRuleRegistry.register("keepJigsaws", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(false));
        APPLY_GRAVITY_PROCESSOR = GameRuleRegistry.register("applyGravityProcessor", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));
        SAVE_CHUNKS = GameRuleRegistry.register("saveChunks", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));

        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (!world.getGameRules().getRule(SAVE_CHUNKS).get()){
                showWarning(player);
            }
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!player.isSpectator() && !world.getGameRules().getRule(SAVE_CHUNKS).get()){
                showWarning(player);
            }
            return InteractionResult.PASS;
        });
    }

    private void showWarning(Player player){
        if (player instanceof ServerPlayer serverPlayer){
            serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(NO_SAVE_WARNING));
        }
    }

}
