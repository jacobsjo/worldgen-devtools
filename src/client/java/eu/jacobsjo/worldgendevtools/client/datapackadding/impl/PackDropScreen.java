package eu.jacobsjo.worldgendevtools.client.datapackadding.impl;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class PackDropScreen extends Screen {

    private static final int BUTTON_WIDTH = 100;
    private static final int BUTTON_SPACING = 10;
    private static final Component TITLE = Component.translatable("worldgendevtools.datapackadding.gui.title");
    private static final Component CANCEL_BUTTON = Component.translatable("worldgendevtools.datapackadding.gui.cancel");
    private static final Component COPY_BUTTON = Component.translatable("worldgendevtools.datapackadding.gui.copy");
    private static final Component SYMLINK_BUTTON = Component.translatable("worldgendevtools.datapackadding.gui.symlink");
    private final Component datapacks;
    private final Consumer<Selection> callback;
    private MultiLineLabel display;

    public PackDropScreen(Component datapacks, Consumer<Selection> callback) {
        super(TITLE);
        this.datapacks = datapacks;
        this.callback = callback;
    }

    @Override
    @NotNull
    public Component getNarrationMessage() {
        return CommonComponents.joinForNarration(super.getNarrationMessage(), this.datapacks);
    }

    @Override
    protected void init() {
        super.init();
        this.display = MultiLineLabel.create(this.font, this.datapacks, this.width - 50);
        int messageHeight = Mth.clamp(this.messageTop() + this.messageHeight() + 20, this.height / 6 + 96, this.height - 24);
        this.addButtons(messageHeight);
    }

    protected void addButtons(int y) {
        this.addRenderableWidget(Button.builder(COPY_BUTTON, button -> this.callback.accept(Selection.COPY)).bounds(this.width / 2 - BUTTON_WIDTH / 2 - BUTTON_SPACING - BUTTON_WIDTH, y, BUTTON_WIDTH, 20).build());
        this.addRenderableWidget(Button.builder(SYMLINK_BUTTON, button -> this.callback.accept(Selection.SYMLINK)).bounds(this.width / 2 - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, 20).build());
        this.addRenderableWidget(Button.builder(CANCEL_BUTTON, button -> this.callback.accept(Selection.CANCEL)).bounds(this.width / 2 + BUTTON_WIDTH / 2 + BUTTON_SPACING, y, BUTTON_WIDTH, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, this.titleTop(), -1);
        this.display.visitLines(TextAlignment.CENTER, this.width / 2, this.messageTop(), 9, guiGraphics.textRenderer());
    }

    private int titleTop() {
        int i = (this.height - this.messageHeight()) / 2;
        return Mth.clamp(i - 20 - 9, 10, 80);
    }

    private int messageTop() {
        return this.titleTop() + 20;
    }

    private int messageHeight() {
        return this.display.getLineCount() * 9;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (keyEvent.key() == 256) {
            this.callback.accept(Selection.CANCEL);
            return true;
        } else {
            return super.keyPressed(keyEvent);
        }
    }

    public enum Selection {
        CANCEL,
        COPY,
        SYMLINK
    }
}
