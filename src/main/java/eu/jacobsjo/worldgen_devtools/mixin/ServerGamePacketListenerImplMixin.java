package eu.jacobsjo.worldgen_devtools.mixin;


import eu.jacobsjo.worldgen_devtools.api.SwitchToConfigurationCallback;
import net.minecraft.network.protocol.game.ServerboundConfigurationAcknowledgedPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedList;
import java.util.List;


/**
 * Adds a callback into the handler of the {@link ServerboundConfigurationAcknowledgedPacket} packet. Allows sending
 * configuration data as soon as the client entered configuration mode.
 */
@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin implements SwitchToConfigurationCallback {

    @Unique
    private List<SwitchToConfigurationCallback.Callback> callbacks;

    @Override
    public void worldgenDevtools$onSwitchToConfiguration(SwitchToConfigurationCallback.Callback callback) {
        if (this.callbacks == null){
            this.callbacks = new LinkedList<>();
        }

        this.callbacks.add(callback);
    }


    @Inject(method = "handleConfigurationAcknowledged", at = @At("TAIL"))
    public void handleConfigurationAcknowledged(ServerboundConfigurationAcknowledgedPacket serverboundConfigurationAcknowledgedPacket, CallbackInfo ci){
        if (this.callbacks == null){
            return;
        }

        this.callbacks.forEach(Callback::handle);
        this.callbacks.clear();
    }

}
