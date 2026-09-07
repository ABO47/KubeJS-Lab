package com.abo47.kubejslab.client.ui.shell;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;


import com.abo47.kubejslab.client.ui.blocks.*;
import com.abo47.kubejslab.client.ui.contextmenu.*;
import com.abo47.kubejslab.client.ui.items.*;
import com.abo47.kubejslab.client.ui.loot.*;
import com.abo47.kubejslab.client.ui.machines.*;
import com.abo47.kubejslab.client.ui.picker.*;
import com.abo47.kubejslab.client.ui.recipes.*;
import com.abo47.kubejslab.loot.model.LootFieldValues;
import com.abo47.kubejslab.loot.runtime.LootPrefill;

public final class ScreenSession {
    private ScreenSession() {
    }

    static int lastLeftTab;
    static int lastRightTab;
    static ResourceLocation lastMachineUid;
    static String lastQuery = "";

    public static void applyLootPrefill(ResourceLocation id, String lootType, LootFieldValues values) {
        if (!(Minecraft.getInstance().screen instanceof ScreenContainer gui)) {
            return;
        }
        if (!(gui.modularUI.mainGroup instanceof RootPanel root)) {
            return;
        }
        WorkspacePanel rightPanel = root.getRightPanel();
        if (rightPanel.loot.lootSelection == null || !rightPanel.loot.lootSelection.id().equals(id)) {
            return;
        }
        if (LootStates.stateOf(id) != null) {
            return;
        }
        if (rightPanel.loot.lootMode != WorkspacePanel.EditMode.NEW || rightPanel.poolModal != null) {
            return;
        }
        if (!rightPanel.lootSettings.getLootType().equals(lootType)) {
            return;
        }
        if (!rightPanel.lootSettings.getValues().equals(LootPrefill.blankFor(id))) {
            return;
        }
        rightPanel.lootSettings.setLootType(lootType);
        rightPanel.lootSettings.applyValues(values);
        rightPanel.lootSettings.setFields(List.of());
        rightPanel.loot.refreshLootPreview();
        rightPanel.bulkSaver.markLootClean();
    }
}
