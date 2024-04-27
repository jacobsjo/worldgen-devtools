package eu.jacobsjo.worldgendevtools.client.datapackadding.impl;

import com.mojang.logging.LogUtils;
import eu.jacobsjo.worldgendevtools.client.datapackadding.api.ExtendablePathAllowList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.validation.DirectoryValidator;
import net.minecraft.world.level.validation.PathAllowList;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.slf4j.Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class SymlinkUtil {
    static final Logger LOGGER = LogUtils.getLogger();
    public static final SystemToast.SystemToastId ADD_SYMLINK_TOAST = new SystemToast.SystemToastId();

    public static void symlinkPacks(Minecraft minecraft, List<Path> packs, Path outDir) {
        MutableBoolean mutableBoolean = new MutableBoolean();
        packs.forEach(path -> {
            try {
                Files.createSymbolicLink(outDir.resolve(path.getFileName()), path);
            } catch (IOException | UnsupportedOperationException e) {
                LOGGER.warn("Failed to symlink datapack file from {} to {}", path, outDir);
                mutableBoolean.setTrue();
            }
        });
        if (mutableBoolean.isTrue()) {
            SystemToast.onPackCopyFailure(minecraft, outDir.toString());
        }
    }

    public static void addAllowedSymlink(Minecraft minecraft, Path path) {
        LOGGER.info("Adding allowed symlink to {}", path);
        DirectoryValidator datapackValidator = minecraft.directoryValidator();
        if (!(datapackValidator.symlinkTargetAllowList instanceof PathAllowList)){
            datapackValidator.symlinkTargetAllowList = new PathAllowList(new ArrayList<>());
        }

        ((ExtendablePathAllowList) minecraft.directoryValidator().symlinkTargetAllowList).worldgenDevtools$addEntry(new PathAllowList.ConfigEntry(PathAllowList.EntryType.FILESYSTEM, "glob:" + path.toString()));
        Path allowedSymlinkPath = minecraft.gameDirectory.toPath().resolve("allowed_symlinks.txt");
        try (FileWriter fw = new FileWriter(allowedSymlinkPath.toString(), true)){
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(String.format("%n[glob]%s", path.toAbsolutePath()));
            bw.close();
        } catch (IOException e){
            LOGGER.error("Failed to save allowed symlink", e);
        }

        SystemToast.add(minecraft.getToasts(), ADD_SYMLINK_TOAST, Component.translatable("worldgendevtools.datapackadding.gui.symlink.add_allowed"), Component.literal(path.toString()));
    }
}
