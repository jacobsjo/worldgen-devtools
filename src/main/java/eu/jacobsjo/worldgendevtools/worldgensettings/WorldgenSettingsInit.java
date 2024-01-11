package eu.jacobsjo.worldgendevtools.worldgensettings;

import eu.jacobsjo.worldgendevtools.worldgensettings.api.GenerationOptions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.gamerule.v1.rule.EnumRule;
import net.minecraft.world.level.GameRules;

public class WorldgenSettingsInit implements ModInitializer {
    public static GameRules.Key<EnumRule<GenerationOptions>> MAX_CHUNK_STATUS;
    public static GameRules.Key<GameRules.BooleanValue> APPLY_PROCESSOR_LISTS;
    public static GameRules.Key<GameRules.BooleanValue> KEEP_JIGSAWS;
    public static GameRules.Key<GameRules.BooleanValue> APPLY_GRAVITY_PROCESSOR;

    @Override
    public void onInitialize() {
        MAX_CHUNK_STATUS = GameRuleRegistry.register("maxChunkStatus", GameRules.Category.MISC, GameRuleFactory.createEnumRule(GenerationOptions.ALL));
        APPLY_PROCESSOR_LISTS = GameRuleRegistry.register("applyProcessorLists", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));
        KEEP_JIGSAWS = GameRuleRegistry.register("keepJigsaws", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(false));
        APPLY_GRAVITY_PROCESSOR = GameRuleRegistry.register("applyGravityProcessor", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));
    }

}
