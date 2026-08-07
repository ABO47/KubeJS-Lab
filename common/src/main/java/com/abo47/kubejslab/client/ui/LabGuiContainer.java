package com.abo47.kubejslab.client.ui;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.modular.ModularUIGuiContainer;


public final class LabGuiContainer extends ModularUIGuiContainer {
    public LabGuiContainer(ModularUI modularUI, int windowId) {
        super(modularUI, windowId);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
