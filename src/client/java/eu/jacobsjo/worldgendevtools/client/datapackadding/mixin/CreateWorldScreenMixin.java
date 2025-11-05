package eu.jacobsjo.worldgendevtools.client.datapackadding.mixin;


import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

@Mixin(CreateWorldScreen.class)
@Environment(EnvType.CLIENT)
public class CreateWorldScreenMixin {
    @Redirect(method = "copyBetweenDirs", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;copyBetweenDirs(Ljava/nio/file/Path;Ljava/nio/file/Path;Ljava/nio/file/Path;)V"))
    private static void copyBetweenDirs(Path fromDirectory, Path toDirectory, Path filePath) throws IOException {
        Path path = fromDirectory.relativize(filePath);
        Path path2 = toDirectory.resolve(path);
        Files.copy(filePath, path2, LinkOption.NOFOLLOW_LINKS); // make sure to copy symlinks
    }
}
