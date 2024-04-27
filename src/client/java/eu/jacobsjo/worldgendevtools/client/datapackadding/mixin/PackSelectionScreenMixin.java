package eu.jacobsjo.worldgendevtools.client.datapackadding.mixin;


import com.llamalad7.mixinextras.sugar.Local;
import eu.jacobsjo.worldgendevtools.client.datapackadding.impl.PackDropScreen;
import eu.jacobsjo.worldgendevtools.client.datapackadding.impl.SymlinkUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(PackSelectionScreen.class)
@Environment(EnvType.CLIENT)
public abstract class PackSelectionScreenMixin extends Screen {

    @Shadow
    private static Stream<String> extractPackNames(Collection<Path> paths) {
        return null;
    }

    @Shadow protected abstract void method_29676(List<Path> par1, boolean par2);

    @Shadow @Final private static Logger LOGGER;

    @Shadow
    protected static void copyPacks(Minecraft minecraft, List<Path> packs, Path outDir) {
    }

    @Unique private boolean shouldSymlink = false;

    protected PackSelectionScreenMixin(Component title) {
        super(title);
    }

    /**
     * @author eu.jacobsjo.worldgendevtools
     * @reason setting different screen bacially changes whole method anyway, the inner lambda is still called
     */
    @Overwrite
    public void onFilesDrop(List<Path> packs) {
        String datapacks = extractPackNames(packs).collect(Collectors.joining(", "));
        this.minecraft.setScreen(new PackDropScreen(Component.literal(datapacks), result -> {
            shouldSymlink = result == PackDropScreen.Selection.SYMLINK;
            method_29676(packs, result == PackDropScreen.Selection.COPY || result == PackDropScreen.Selection.SYMLINK);
        }));
    }

    @Inject(method = "method_29676", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/packs/PackSelectionScreen;copyPacks(Lnet/minecraft/client/Minecraft;Ljava/util/List;Ljava/nio/file/Path;)V"))
    public void beforeCopyPacks(List<Path> packs, boolean bl, CallbackInfo ci, @Local(ordinal = 1) List<Path> list2) {
        if (shouldSymlink) {
            list2.stream()
                    .filter(path -> !minecraft.directoryValidator().symlinkTargetAllowList.matches(path))
                    .forEach(path -> SymlinkUtil.addAllowedSymlink(minecraft, path));
        }
    }

    @Redirect(method = "method_29676", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/packs/PackSelectionScreen;copyPacks(Lnet/minecraft/client/Minecraft;Ljava/util/List;Ljava/nio/file/Path;)V"))
    public void callCopyPacks(Minecraft minecraft, List<Path> packs, Path outDir){
        if (shouldSymlink){
            SymlinkUtil.symlinkPacks(minecraft, packs, outDir);
        } else {
            copyPacks(minecraft, packs, outDir);
        }
    }
}
