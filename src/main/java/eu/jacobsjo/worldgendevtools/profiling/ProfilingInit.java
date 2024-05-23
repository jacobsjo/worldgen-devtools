package eu.jacobsjo.worldgendevtools.profiling;

import eu.jacobsjo.worldgendevtools.profiling.impl.ChunkProfilingCommand;
import eu.jacobsjo.worldgendevtools.profiling.impl.ChunkgenProfilingInformation;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("UnstableApiUsage")
public class ProfilingInit implements ModInitializer {
    public static final AttachmentType<ChunkgenProfilingInformation> PROFILING_ATTACHMENT = AttachmentRegistry.<ChunkgenProfilingInformation>builder()
            .initializer(ChunkgenProfilingInformation::new)
            .persistent(ChunkgenProfilingInformation.CODEC)
            .buildAndRegister(ResourceLocation.fromNamespaceAndPath("worldgendevtools", "profiling"));

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> ChunkProfilingCommand.register(dispatcher));

    }
}
