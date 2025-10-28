package eu.jacobsjo.worldgendevtools.worldgensettings;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.serialization.Codec;
import eu.jacobsjo.util.TextUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.minecraft.world.level.gamerules.GameRuleTypeVisitor;

public class WorldgenSettingsInit implements ModInitializer {
    //@Deprecated public static GameRules.Key<EnumRule<GenerationOptions>> MAX_CHUNK_STATUS = GameRuleRegistry.register("maxChunkStatus", GameRuleCategory.MISC, GameRuleFactory.createEnumRule(GenerationOptions.ALL));
    public static GameRule<Boolean> APPLY_PROCESSOR_LISTS = registrBoolean("apply_processor_lists", GameRuleCategory.MISC, true);;
    @Deprecated public static GameRule<Boolean> KEEP_JIGSAWS = registrBoolean("keep_jigsaws", GameRuleCategory.MISC, false);
    public static GameRule<Boolean> APPLY_GRAVITY_PROCESSOR = registrBoolean("apply_gravity_processor", GameRuleCategory.MISC, true);
    @Deprecated public static GameRule<Boolean> SAVE_CHUNKS = registrBoolean("save_chunks", GameRuleCategory.MISC, true);

    public static final Component NO_SAVE_WARNING = TextUtil.translatable("worldgendevtools.worldgensettings.no_save_warning").withStyle(ChatFormatting.RED);

    @Override
    public void onInitialize() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (!((ServerLevel) world).getGameRules().get(SAVE_CHUNKS)){
                showWarning(player);
            }
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!player.isSpectator() && world instanceof ServerLevel && !((ServerLevel) world).getGameRules().get(SAVE_CHUNKS)){
                showWarning(player);
            }
            return InteractionResult.PASS;
        });
    }

    public static GameRule<Boolean> registrBoolean(String id, GameRuleCategory category, boolean default_value){
        GameRule<Boolean> gameRule = new GameRule<>(category, GameRuleType.BOOL, BoolArgumentType.bool(), GameRuleTypeVisitor::visitBoolean, Codec.BOOL, b -> b ? 1 : 0, default_value, FeatureFlagSet.of());
        return Registry.register(BuiltInRegistries.GAME_RULE, ResourceLocation.withDefaultNamespace(id), gameRule); // Using minecraft namespace here as other namespaces are horrible UX
    }

    private void showWarning(Player player){
        if (player instanceof ServerPlayer serverPlayer){
            serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(NO_SAVE_WARNING));
        }
    }

}
