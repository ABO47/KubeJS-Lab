package com.abo47.kubejslab.client.ui.shell;

import java.util.List;

import net.minecraft.world.item.ItemStack;

import com.abo47.kubejslab.client.ui.blocks.*;
import com.abo47.kubejslab.client.ui.contextmenu.*;
import com.abo47.kubejslab.client.ui.items.*;
import com.abo47.kubejslab.client.ui.loot.*;
import com.abo47.kubejslab.client.ui.machines.*;
import com.abo47.kubejslab.client.ui.picker.*;
import com.abo47.kubejslab.client.ui.recipes.*;
import com.abo47.kubejslab.client.ui.theme.UiColors;
import com.abo47.kubejslab.item.model.ItemState;

final class ItemPanelSection {

    private final WorkspacePanel panel;

    ItemPanelSection(WorkspacePanel panel) {
        this.panel = panel;
    }

    WorkspacePanel.EditMode itemMode = WorkspacePanel.EditMode.NEW;
    ItemIndex.ItemEntry itemModifyTarget;
    ItemIndex.ItemEntry itemSelection;

    void selectItem(ItemIndex.ItemEntry entry) {
        if (panel.itemBrowser == null) {
            return;
        }
        panel.itemBrowser.setSelectedItemId(entry.id());
    }

    void enterItemModifyMode(ItemIndex.ItemEntry entry) {
        itemMode = WorkspacePanel.EditMode.MODIFY;
        itemModifyTarget = entry;
        showItemSettings(entry);
    }

    void exitItemModifyMode() {
        if (itemMode != WorkspacePanel.EditMode.MODIFY) return;
        itemMode = WorkspacePanel.EditMode.NEW;
        itemModifyTarget = null;
        refreshItemModeLabel();
    }

    void exitItemModifyModeIfTarget(ItemIndex.ItemEntry entry) {
        if (itemModifyTarget != null && itemModifyTarget.id().equals(entry.id())) {
            exitItemModifyMode();
        }
    }

    void showItemSettings(ItemIndex.ItemEntry entry) {
        itemSelection = entry;
        panel.itemSettings.setFields(panel.itemSettings.fullFields());
        ItemState state = ItemStates.stateOf(entry.id());
        String type = state != null && state.type() != null && !state.type().isBlank()
                ? state.type()
                : ItemIndex.typeOf(entry.id());
        panel.itemSettings.setType(type);
        panel.itemTypeDropdown.setSelected(type);
        if (state != null) {
            panel.itemSettings.applyValues(state.values());
            panel.itemSettings.applyTags(state.tags());
            panel.itemSettings.applyActions(state.actions());
        } else {
            panel.itemSettings.applyValues(ItemIndex.prefillValues(entry.id()));
            panel.itemSettings.applyTags(List.of());
            panel.itemSettings.applyActions(List.of());
        }
        refreshItemModeLabel();
        refreshItemPreview();
    }

    private void refreshItemModeLabel() {
        if (panel.itemModeLabel == null) return;
        panel.itemModeLabel.setColor(itemMode == WorkspacePanel.EditMode.MODIFY ? UiColors.INTERACTIVE : UiColors.TEXT_MUTED);
    }

    void refreshItemPreview() {
        if (panel.itemSettings == null || panel.itemPreview == null) {
            return;
        }
        ItemStack previewStack = itemSelection != null ? itemSelection.stack() : ItemStack.EMPTY;
        panel.itemPreview.setItem(panel.itemSettings.getType(), previewStack);
        panel.itemPreview.setTexture(panel.itemSettings.getTexture());
    }
}
