package eu.jacobsjo.worldgendevtools.client.reloadregistries.mixin;

import net.minecraft.client.server.IntegratedPlayerList;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(IntegratedPlayerList.class)
public class IntegratedPlayerListMixin extends PlayerList {

    @Shadow
    private CompoundTag playerData;

    public IntegratedPlayerListMixin(MinecraftServer server, LayeredRegistryAccess<RegistryLayer> registries, PlayerDataStorage playerIo, int maxPlayers) {
        super(server, registries, playerIo, maxPlayers);
    }

    /**
     * Fixes a vanilla bug when sending the singleplay owner back to configuration phase
     */
    @Override
    @NotNull
    public Optional<ValueInput> load(ServerPlayer player, ProblemReporter problemReporter) {
        if (this.getServer().isSingleplayerOwner(player.getGameProfile()) && this.playerData != null) {
            ValueInput valueInput = TagValueInput.create(problemReporter, player.registryAccess(), this.playerData);
            player.load(valueInput);
            return Optional.of(valueInput);
        }
        return super.load(player, problemReporter);
    }
}