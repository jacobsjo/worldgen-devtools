package eu.jacobsjo.worldgendevtools.worldgensettings;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.serialization.Codec;
import eu.jacobsjo.util.TextUtil;
import eu.jacobsjo.worldgendevtools.worldgensettings.api.GenerationOptions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.resources.Identifier;
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
    @Deprecated public static GameRule<GenerationOptions> MAX_CHUNK_STATUS = Registry.register(BuiltInRegistries.GAME_RULE, "max_chunk_status", GameRuleBuilder.forEnum(GenerationOptions.ALL).build());
    public static GameRule<Boolean> APPLY_PROCESSOR_LISTS = Registry.register(BuiltInRegistries.GAME_RULE, "apply_processor_lists", GameRuleBuilder.forBoolean(true).build());
    @Deprecated public static GameRule<Boolean> KEEP_JIGSAWS = Registry.register(BuiltInRegistries.GAME_RULE, "keep_jigsaws", GameRuleBuilder.forBoolean(true).build());
    public static GameRule<Boolean> APPLY_GRAVITY_PROCESSOR = Registry.register(BuiltInRegistries.GAME_RULE, "apply_gravity_processor", GameRuleBuilder.forBoolean(true).build());
    @Deprecated public static GameRule<Boolean> SAVE_CHUNKS = Registry.register(BuiltInRegistries.GAME_RULE, "save_chunks", GameRuleBuilder.forBoolean(true).build());

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

    private void showWarning(Player player){
        if (player instanceof ServerPlayer serverPlayer){
            serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(NO_SAVE_WARNING));
        }
    }

}
