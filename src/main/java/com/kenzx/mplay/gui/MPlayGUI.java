package com.kenzx.mplay.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public class MPlayGUI extends Screen {

    static Minecraft client;
    private EditBox urlInput;

    public MPlayGUI() {
        super(Component.literal("MPlay Controller"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;

        client = Minecraft.getInstance();
        assert client.player != null;

        // URL textbox
        this.urlInput = new EditBox(this.font, centerX - 100, 60, 200, 20, Component.literal("URL"));
        this.urlInput.setMaxLength(2048);
        this.addRenderableWidget(this.urlInput);

        // Add(play) Song button
        this.addRenderableWidget(Button.builder(Component.literal("Add Song (play)"), b -> {
            String val = urlInput.getValue();
            if (!val.isEmpty()) {
                client.player.connection.sendCommand("music play \"" + val + "\"");
                urlInput.setValue("");
            }
        }).bounds(centerX - 100, 90, 200, 20).build());

        // RESUME button
        this.addRenderableWidget(Button.builder(Component.literal("Resume"), b -> {
            client.player.connection.sendCommand("music resume");
        }).bounds(centerX - 100, 120, 95, 20).build());

        // PAUSE button
        this.addRenderableWidget(Button.builder(Component.literal("Pause"), b -> {
            client.player.connection.sendCommand("music pause");
        }).bounds(centerX + 5, 120, 95, 20).build());

        // STOP button
        this.addRenderableWidget(Button.builder(Component.literal("Stop"), b -> {
            client.player.connection.sendCommand("music stop");
        }).bounds(centerX - 100, 150, 200, 20).build());

        // Volume slider
        VolumeSlider volumeSlider = new VolumeSlider(centerX - 100, 180, 200, 20);
        this.addRenderableWidget(volumeSlider);
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(this.font, "Multimedia Control Panel", this.width / 2, 30, 0x00A3FF);
    }

    // Volume Slider
    private static class VolumeSlider extends AbstractSliderButton {

        private int volume = 50;

        public VolumeSlider(int x, int y, int width, int height) {
            super(x, y, width, height, Component.literal("Volume: 50%"), 0.5D);
        }

        @Override
        protected void updateMessage() {
            this.volume = (int) (this.value * 100);
            this.setMessage(Component.literal("Volume: " + volume + "%"));
        }

        @Override
        protected void applyValue() {
            assert MPlayGUI.client.player != null;
            MPlayGUI.client.player.connection.sendCommand("music volume " + volume);
        }
    }
}