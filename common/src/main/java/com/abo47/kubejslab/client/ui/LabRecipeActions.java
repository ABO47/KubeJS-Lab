package com.abo47.kubejslab.client.ui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.resources.language.I18n;

import com.abo47.kubejslab.client.ui.base.LabGuiKeys;
import com.abo47.kubejslab.client.ui.contextmenu.LabActionTone;
import com.abo47.kubejslab.client.ui.contextmenu.LabContextAction;
import com.abo47.kubejslab.client.ui.recipes.LabRecipeIndex;
import com.abo47.kubejslab.client.ui.recipes.LabRecipeStates;
import com.abo47.kubejslab.recipe.model.LabRecipeEditAction;
import com.abo47.kubejslab.recipe.model.LabRecipeStatus;


final class LabRecipeActions {
    private LabRecipeActions() {
    }

    static List<LabContextAction> forEntry(LabScreen.LabPanelWidget rightPanel, LabRecipeIndex.LabRecipeEntry entry) {
        LabRecipeStatus status = LabRecipeStates.statusOf(entry.id());
        boolean custom = entry.kubejs();
        List<LabContextAction> actions = new ArrayList<>();
        if (status == LabRecipeStatus.NORMAL) {
            actions.add(modify(rightPanel, entry));
            actions.add(disable(rightPanel, entry));
            if (custom) actions.add(delete(rightPanel, entry));
        } else if (status == LabRecipeStatus.MODIFIED) {
            actions.add(modify(rightPanel, entry));
            actions.add(reset(rightPanel, entry));
            actions.add(disable(rightPanel, entry));
            if (custom) actions.add(delete(rightPanel, entry));
        } else {
            actions.add(new LabContextAction(I18n.get(LabGuiKeys.LAB_RECIPE_ENABLE), "eye", LabActionTone.SUCCESS,
                    () -> send(rightPanel, LabRecipeEditAction.ENABLE, entry)));
            if (custom) actions.add(delete(rightPanel, entry));
        }
        return actions;
    }

    private static LabContextAction modify(LabScreen.LabPanelWidget rightPanel, LabRecipeIndex.LabRecipeEntry entry) {
        return new LabContextAction(I18n.get(LabGuiKeys.LAB_RECIPE_MODIFY), "editor", LabActionTone.PRIMARY,
                () -> rightPanel.enterModifyMode(entry));
    }

    private static LabContextAction disable(LabScreen.LabPanelWidget rightPanel, LabRecipeIndex.LabRecipeEntry entry) {
        return new LabContextAction(I18n.get(LabGuiKeys.LAB_RECIPE_DISABLE), "eye-off", LabActionTone.WARNING,
                () -> send(rightPanel, LabRecipeEditAction.DISABLE, entry));
    }

    private static LabContextAction reset(LabScreen.LabPanelWidget rightPanel, LabRecipeIndex.LabRecipeEntry entry) {
        return new LabContextAction(I18n.get(LabGuiKeys.LAB_RECIPE_RESET), "repeat", LabActionTone.SUCCESS,
                () -> {
                    send(rightPanel, LabRecipeEditAction.RESET, entry);
                    rightPanel.exitModifyMode();
                });
    }

    private static LabContextAction delete(LabScreen.LabPanelWidget rightPanel, LabRecipeIndex.LabRecipeEntry entry) {
        return new LabContextAction(I18n.get(LabGuiKeys.LAB_RECIPE_DELETE), "delete", LabActionTone.DANGER,
                () -> {
                    send(rightPanel, LabRecipeEditAction.DELETE, entry);
                    rightPanel.exitModifyModeIfTarget(entry);
                });
    }

    private static void send(LabScreen.LabPanelWidget rightPanel, LabRecipeEditAction action,
            LabRecipeIndex.LabRecipeEntry entry) {
        rightPanel.saver.sendRecipeEdit(action, entry.id(),
                LabScreen.emptyPayload(entry, rightPanel.getSelectedMachineUid()));
    }
}