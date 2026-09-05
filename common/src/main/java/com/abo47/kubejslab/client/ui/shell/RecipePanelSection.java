package com.abo47.kubejslab.client.ui.shell;

import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

import com.abo47.kubejslab.client.ui.blocks.*;
import com.abo47.kubejslab.client.ui.contextmenu.*;
import com.abo47.kubejslab.client.ui.items.*;
import com.abo47.kubejslab.client.ui.loot.*;
import com.abo47.kubejslab.client.ui.machines.*;
import com.abo47.kubejslab.client.ui.picker.*;
import com.abo47.kubejslab.client.ui.recipes.*;
import com.abo47.kubejslab.client.ui.theme.UiColors;
import com.abo47.kubejslab.recipe.MachineRegistry;
import com.abo47.kubejslab.recipe.RecipeHandler;
import com.abo47.kubejslab.recipe.model.RecipeOutput;

final class RecipePanelSection {

    private final WorkspacePanel panel;

    RecipePanelSection(WorkspacePanel panel) {
        this.panel = panel;
    }

    WorkspacePanel.EditMode mode = WorkspacePanel.EditMode.NEW;
    RecipeIndex.RecipeEntry modifyTarget;

    void selectRecipe(RecipeIndex.RecipeEntry entry) {
        if (panel.recipeBrowser == null) {
            return;
        }
        panel.recipeBrowser.setSelectedRecipeId(entry.id());
    }

    void enterModifyMode(RecipeIndex.RecipeEntry entry) {
        mode = WorkspacePanel.EditMode.MODIFY;
        modifyTarget = entry;
        refreshModeLabel();
        ResourceLocation uid = panel.saver.resolveModifyUid(entry);
        if (uid != null) {
            panel.machineDropdown.selectMachineByUid(uid);
        }
        showRecipe(entry);
        if (uid == null) {
            panel.settingsWidget.setFields(List.of());
        }
    }

    void exitModifyMode() {
        if (mode != WorkspacePanel.EditMode.MODIFY) return;
        mode = WorkspacePanel.EditMode.NEW;
        modifyTarget = null;
        refreshModeLabel();
    }

    void exitModifyModeIfTarget(RecipeIndex.RecipeEntry entry) {
        if (modifyTarget != null && modifyTarget.id().equals(entry.id())) {
            exitModifyMode();
        }
    }

    private void refreshModeLabel() {
        if (panel.modeLabel == null) return;
        panel.modeLabel.setColor(mode == WorkspacePanel.EditMode.MODIFY ? UiColors.INTERACTIVE : UiColors.TEXT_MUTED);
    }

    void showRecipe(RecipeIndex.RecipeEntry entry) {
        panel.machineLayout.showRecipe(entry);
        MachineView machine = panel.machineDropdown.getSelectedMachine();
        if (machine == null) {
            return;
        }
        RecipeHandler support = MachineRegistry.get(machine.recipeTypeUid());
        if (support != null) {
            Recipe<?> original = RecipeIndex.recipeById(entry.id());
            panel.settingsWidget.applyValues(support.prefill(panel.settingsWidget.getValues(), original));
            panel.machineLayout.setGridSize(panel.settingsWidget.gridWidthValue(), panel.settingsWidget.gridHeightValue());
            if (support.supportsFluidOutputAmount()) {
                int amount = 0;
                for (RecipeOutput output : panel.machineLayout.getOutputs()) {
                    if (output instanceof RecipeOutput.Fluid fluid) {
                        amount = (int) fluid.fluid().getAmount();
                        break;
                    }
                }
                panel.settingsWidget.setFluidOutputAmount(amount);
            }
        }
    }
}
