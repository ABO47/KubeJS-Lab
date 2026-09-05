package com.abo47.kubejslab.client.ui.shell;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.modular.ModularUIGuiContainer;


public final class ScreenContainer extends ModularUIGuiContainer {
    public ScreenContainer(ModularUI modularUI, int windowId) {
        super(modularUI, windowId);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
