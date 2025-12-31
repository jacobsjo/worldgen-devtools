package eu.jacobsjo.worldgendevtools.reloadregistries;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.gamerules.GameRule;

public class ReloadRegistriesInit implements ModInitializer {
    public static GameRule<Boolean> RELOAD_REGISTIRES = Registry.register(BuiltInRegistries.GAME_RULE, "reload_registries", GameRuleBuilder.forBoolean(false).build());
    public static GameRule<Boolean> SYNC_AFTER_REGISTRY_RELOAD = Registry.register(BuiltInRegistries.GAME_RULE, "sync_after_registry_reload", GameRuleBuilder.forBoolean(true).build());

    @Override
    public void onInitialize() {

    }

}
