package eu.jacobsjo.worldgen_devtools.reload_registries;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.level.GameRules;

public class ReloadRegistriesInit implements ModInitializer {
    public static GameRules.Key<GameRules.BooleanValue> RELOAD_REGISTIRES;
    public static GameRules.Key<GameRules.BooleanValue> SYNC_AFTER_REGISTRY_RELOAD;

    @Override
    public void onInitialize() {
        RELOAD_REGISTIRES = GameRuleRegistry.register("reloadRegistries", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(false));
        SYNC_AFTER_REGISTRY_RELOAD = GameRuleRegistry.register("syncAfterRegistryReload", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));
    }
}
