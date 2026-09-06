package com.abo47.kubejslab.client.ui.shell;

import com.abo47.kubejslab.loot.model.LootPoolValues;
import com.abo47.kubejslab.loot.runtime.LootScriptWriter;
import com.abo47.kubejslab.recipe.model.RecipeIngredient;

final class BulkSaver {
    private final WorkspacePanel panel;

    BulkSaver(WorkspacePanel panel) {
        this.panel = panel;
    }

    int saveAll() {
        int saved = 0;
        if (canSaveRecipe()) {
            panel.saver.saveRecipe();
            saved++;
        }
        if (canSaveItem()) {
            panel.itemSaver.saveItem();
            saved++;
        }
        if (canSaveBlock()) {
            panel.blockSaver.saveBlock();
            saved++;
        }
        if (canSaveLoot()) {
            panel.lootSaver.saveLoot();
            saved++;
        }
        if (saved == 0) {
            saveCurrentWithFeedback();
        }
        return saved;
    }

    private void saveCurrentWithFeedback() {
        int tab = panel.getSelectedTabIndex();
        switch (tab) {
            case 1 -> panel.itemSaver.saveItem();
            case 2 -> panel.blockSaver.saveBlock();
            case 3 -> panel.lootSaver.saveLoot();
            default -> panel.saver.saveRecipe();
        }
    }

    private boolean canSaveRecipe() {
        if (panel.machineLayout == null) {
            return false;
        }
        for (RecipeIngredient input : panel.machineLayout.getInputs()) {
            if (input != null && !input.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private boolean canSaveItem() {
        if (panel.itemSettings == null) {
            return false;
        }
        if (panel.items != null && panel.items.itemMode == WorkspacePanel.EditMode.MODIFY
                && panel.items.itemModifyTarget != null) {
            return true;
        }
        String type = panel.itemSettings.getType();
        String name = panel.itemSettings.getValues().displayName();
        return type != null && !type.isBlank() && name != null && !name.isBlank();
    }

    private boolean canSaveBlock() {
        if (panel.blockSettings == null) {
            return false;
        }
        if (panel.blocks != null && panel.blocks.blockMode == WorkspacePanel.EditMode.MODIFY
                && panel.blocks.blockModifyTarget != null) {
            return true;
        }
        String type = panel.blockSettings.getType();
        String name = panel.blockSettings.getValues().displayName();
        return type != null && !type.isBlank() && name != null && !name.isBlank();
    }

    private boolean canSaveLoot() {
        if (panel.lootSettings == null) {
            return false;
        }
        if (panel.loot != null && panel.loot.lootMode == WorkspacePanel.EditMode.MODIFY
                && panel.loot.lootModifyTarget != null) {
            return true;
        }
        String target = panel.lootSettings.getValues().targetId();
        if (target == null || target.isBlank()) {
            return false;
        }
        for (LootPoolValues pool : panel.lootSettings.getValues().pools()) {
            if (pool != null && LootScriptWriter.writesPool(pool)) {
                return true;
            }
        }
        return false;
    }
}
