package eu.jacobsjo.worldgendevtools.client.reloadregistries.mixin;

import net.minecraft.client.server.IntegratedPlayerList;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.notifications.NotificationService;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(IntegratedPlayerList.class)
public class IntegratedPlayerListMixin extends PlayerList {

    @Shadow
    private @Nullable CompoundTag playerData;

    public IntegratedPlayerListMixin(MinecraftServer server, LayeredRegistryAccess<RegistryLayer> registries, PlayerDataStorage playerIo, NotificationService notificationService) {
        super(server, registries, playerIo, notificationService);
    }

    /**
     * Fixes a vanilla bug when sending the singleplay owner back to configuration phase
     */
    @Override
    public Optional<CompoundTag> loadPlayerData(NameAndId nameAndId) {
        if (this.getServer().isSingleplayerOwner(nameAndId) && this.playerData != null) {
            return Optional.of(this.playerData);
        }
        return super.loadPlayerData(nameAndId);
    }
}