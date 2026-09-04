package com.abo47.kubejslab.client.ui.shell;

import java.util.List;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.client.ui.blocks.*;
import com.abo47.kubejslab.client.ui.contextmenu.*;
import com.abo47.kubejslab.client.ui.items.*;
import com.abo47.kubejslab.client.ui.loot.*;
import com.abo47.kubejslab.client.ui.machines.*;
import com.abo47.kubejslab.client.ui.picker.*;
import com.abo47.kubejslab.client.ui.recipes.*;
import com.abo47.kubejslab.client.ui.theme.UiColors;
import com.abo47.kubejslab.loot.model.LootFieldValues;
import com.abo47.kubejslab.loot.model.LootState;
import com.abo47.kubejslab.loot.runtime.LootPrefill;
import com.abo47.kubejslab.network.NetworkRegistry;
import com.abo47.kubejslab.network.loot.C2SLootPrefillPacket;

final class LootPanelSection {

    private final WorkspacePanel panel;

    LootPanelSection(WorkspacePanel panel) {
        this.panel = panel;
    }

    WorkspacePanel.EditMode lootMode = WorkspacePanel.EditMode.NEW;
    ResourceLocation lootModifyTarget;
    LootIndex.LootEntry lootSelection;

    void selectLoot(LootIndex.LootEntry entry) {
        if (panel.lootBrowser == null) {
            return;
        }
        panel.lootBrowser.setSelectedLootId(entry.id());
    }

    void enterLootModifyMode(LootIndex.LootEntry entry) {
        lootMode = WorkspacePanel.EditMode.MODIFY;
        lootModifyTarget = entry.id();
        showLootSettings(entry);
    }

    void exitLootModifyMode() {
        if (lootMode != WorkspacePanel.EditMode.MODIFY) return;
        lootMode = WorkspacePanel.EditMode.NEW;
        lootModifyTarget = null;
        refreshLootModeLabel();
    }

    void showLootSettings(LootIndex.LootEntry entry) {
        lootSelection = entry;
        LootState state = LootStates.stateOf(entry.id());
        String lootType = state != null && state.lootType() != null && !state.lootType().isBlank()
                ? state.lootType()
                : entry.lootType();
        panel.lootSettings.setLootType(lootType);
        panel.lootTypeDropdown.setSelected("all");
        if (state != null) {
            panel.lootSettings.applyValues(state.values());
        } else {
            panel.lootSettings.applyValues(LootPrefill.blankFor(entry.id()));
            NetworkRegistry.sendLootPrefill(new C2SLootPrefillPacket(entry.id(), lootType));
        }
        panel.lootSettings.setFields(List.of());
        refreshLootModeLabel();
        refreshLootPreview();
    }

    private void refreshLootModeLabel() {
        if (panel.lootModeLabel == null) return;
        panel.lootModeLabel.setColor(lootMode == WorkspacePanel.EditMode.MODIFY ? UiColors.INTERACTIVE : UiColors.TEXT_MUTED);
    }

    void refreshLootPreview() {
        if (panel.lootPreview == null) {
            return;
        }
        ResourceLocation id = lootSelection == null ? null : lootSelection.id();
        String type = lootSelection == null ? null : lootSelection.lootType();
        LootFieldValues values = panel.lootSettings == null ? null : panel.lootSettings.getValues();
        if (id == null && values != null && !values.targetId().isBlank()) {
            id = ResourceLocation.tryParse(values.targetId());
        }
        if ((type == null || type.isBlank()) && panel.lootSettings != null) {
            type = panel.lootSettings.getLootType();
        }
        panel.lootPreview.setEntry(id, type, values);
    }
}
