package eu.jacobsjo.worldgendevtools.reloadregistries;

import com.mojang.logging.LogUtils;
import eu.jacobsjo.worldgendevtools.worldgensettings.WorldgenSettingsInit;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

public class ReloadRegistriesInit implements ModInitializer {
    public static final Logger LOGGER = LogUtils.getLogger();

    public static GameRule<Boolean> RELOAD_REGISTIRES = Registry.register(BuiltInRegistries.GAME_RULE, "reload_registries", GameRuleBuilder.forBoolean(false).build());
    public static GameRule<Boolean> SYNC_AFTER_REGISTRY_RELOAD = Registry.register(BuiltInRegistries.GAME_RULE, "sync_after_registry_reload", GameRuleBuilder.forBoolean(true).build());

    public static Identifier PACKET_ID = Identifier.fromNamespaceAndPath("worldgendevtools", "delay");

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.configurationS2C().register(DummyPayload.ID, DummyPayload.CODEC);
    }

    public record DummyPayload() implements CustomPacketPayload {
        public static final DummyPayload INSTANCE = new DummyPayload();
        public static final StreamCodec<FriendlyByteBuf, DummyPayload> CODEC = unit_delay(INSTANCE);
        public static final CustomPacketPayload.Type<DummyPayload> ID = new Type<>(PACKET_ID);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            LOGGER.info("type start");
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException ignored) {
            }
            LOGGER.info("type end");
            return ID;
        }
    }

    static <B, V> StreamCodec<B, V> unit_delay(V object) {
        return new StreamCodec<B, V>() {
            @Override
            public V decode(B o) {
                LOGGER.info("decode start");
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException ignored) {
                }
                LOGGER.info("decode end");
                return object;
            }

            @Override
            public void encode(B o, V object2) {
                LOGGER.info("encode start");
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException ignored) {
                }

                if (!object2.equals(object)) {
                    throw new IllegalStateException("Can't encode '" + object2 + "', expected '" + object + "'");
                }
                LOGGER.info("encode end");
            }
        };
    }
}
