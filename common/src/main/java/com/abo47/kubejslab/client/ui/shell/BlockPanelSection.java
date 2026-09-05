package com.abo47.kubejslab.client.ui.shell;

import java.util.List;

import net.minecraft.world.item.ItemStack;

import com.abo47.kubejslab.block.model.BlockState;
import com.abo47.kubejslab.client.ui.blocks.*;
import com.abo47.kubejslab.client.ui.contextmenu.*;
import com.abo47.kubejslab.client.ui.items.*;
import com.abo47.kubejslab.client.ui.loot.*;
import com.abo47.kubejslab.client.ui.machines.*;
import com.abo47.kubejslab.client.ui.picker.*;
import com.abo47.kubejslab.client.ui.recipes.*;
import com.abo47.kubejslab.client.ui.theme.UiColors;

final class BlockPanelSection {

    private final WorkspacePanel panel;

    BlockPanelSection(WorkspacePanel panel) {
        this.panel = panel;
    }

    WorkspacePanel.EditMode blockMode = WorkspacePanel.EditMode.NEW;
    BlockIndex.BlockEntry blockModifyTarget;
    BlockIndex.BlockEntry blockSelection;

    void selectBlock(BlockIndex.BlockEntry entry) {
        if (panel.blockBrowser == null) {
            return;
        }
        panel.blockBrowser.setSelectedBlockId(entry.id());
    }

    void enterBlockModifyMode(BlockIndex.BlockEntry entry) {
        blockMode = WorkspacePanel.EditMode.MODIFY;
        blockModifyTarget = entry;
        showBlockSettings(entry);
    }

    void exitBlockModifyMode() {
        if (blockMode != WorkspacePanel.EditMode.MODIFY) return;
        blockMode = WorkspacePanel.EditMode.NEW;
        blockModifyTarget = null;
        refreshBlockModeLabel();
    }

    void showBlockSettings(BlockIndex.BlockEntry entry) {
        blockSelection = entry;
        panel.blockSettings.setBuiltInOnly(!entry.kubejs());
        panel.blockSettings.setFields(panel.blockSettings.fullFields());
        BlockState state = BlockStates.stateOf(entry.id());
        String type = state != null && state.type() != null && !state.type().isBlank()
                ? state.type()
                : BlockIndex.typeOf(entry.id());
        panel.blockSettings.setType(type);
        panel.blockTypeDropdown.setSelected(type);
        if (state != null) {
            panel.blockSettings.applyValues(state.values());
            panel.blockSettings.applyTags(state.tags());
            panel.blockSettings.applyActions(state.actions());
        } else {
            panel.blockSettings.applyValues(BlockIndex.prefillValues(entry.id()));
            panel.blockSettings.applyTags(List.of());
            panel.blockSettings.applyActions(List.of());
        }
        refreshBlockModeLabel();
        refreshBlockPreview();
    }

    private void refreshBlockModeLabel() {
        if (panel.blockModeLabel == null) return;
        panel.blockModeLabel.setColor(blockMode == WorkspacePanel.EditMode.MODIFY ? UiColors.INTERACTIVE : UiColors.TEXT_MUTED);
    }

    void refreshBlockPreview() {
        if (panel.blockSettings == null || panel.blockPreview == null) {
            return;
        }
        ItemStack previewStack = blockSelection != null ? blockSelection.stack() : ItemStack.EMPTY;
        panel.blockPreview.setBlock(panel.blockSettings.getType(), previewStack);
        panel.blockPreview.setTexture(panel.blockSettings.getAllTexture());
    }
}
