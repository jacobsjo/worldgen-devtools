package eu.jacobsjo.worldgendevtools.reloadregistries;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameRules;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

public class ReloadRegistriesInit implements ModInitializer {
    public static final Logger LOGGER = LogUtils.getLogger();

    public static GameRules.Key<GameRules.BooleanValue> RELOAD_REGISTIRES;
    public static GameRules.Key<GameRules.BooleanValue> SYNC_AFTER_REGISTRY_RELOAD;

    public static ResourceLocation PACKET_ID = ResourceLocation.fromNamespaceAndPath("worldgendevtools", "delay");

    @Override
    public void onInitialize() {
        RELOAD_REGISTIRES = GameRuleRegistry.register("reloadRegistries", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(false));
        SYNC_AFTER_REGISTRY_RELOAD = GameRuleRegistry.register("syncAfterRegistryReload", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));

        PayloadTypeRegistry.configurationS2C().register(DummyPayload.ID, DummyPayload.CODEC);
    }


    public record DummyPayload() implements CustomPacketPayload {
        public static final DummyPayload INSTANCE = new DummyPayload();
        public static final StreamCodec<FriendlyByteBuf, DummyPayload> CODEC = unit_delay(INSTANCE);
        public static final CustomPacketPayload.Type<DummyPayload> ID = new Type<>(PACKET_ID);

        @Override @NotNull
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
