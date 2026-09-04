package com.abo47.kubejslab.client.ui.shell;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.resources.language.I18n;

import com.abo47.kubejslab.client.ui.contextmenu.ActionTone;
import com.abo47.kubejslab.client.ui.contextmenu.ContextAction;
import com.abo47.kubejslab.client.ui.recipes.RecipeIndex;
import com.abo47.kubejslab.client.ui.recipes.RecipeKeys;
import com.abo47.kubejslab.client.ui.recipes.RecipeStates;
import com.abo47.kubejslab.recipe.model.RecipeEditAction;
import com.abo47.kubejslab.recipe.model.RecipeStatus;


final class RecipeActions {
    private RecipeActions() {
    }

    static List<ContextAction> forEntry(WorkspacePanel rightPanel, RecipeIndex.RecipeEntry entry) {
        RecipeStatus status = RecipeStates.statusOf(entry.id());
        boolean custom = entry.kubejs();
        List<ContextAction> actions = new ArrayList<>();
        if (status == RecipeStatus.NORMAL) {
            actions.add(modify(rightPanel, entry));
            actions.add(disable(rightPanel, entry));
            if (custom) actions.add(delete(rightPanel, entry));
        } else if (status == RecipeStatus.MODIFIED) {
            actions.add(modify(rightPanel, entry));
            actions.add(reset(rightPanel, entry));
            actions.add(disable(rightPanel, entry));
            if (custom) actions.add(delete(rightPanel, entry));
        } else {
            actions.add(new ContextAction(I18n.get(RecipeKeys.RECIPE_ENABLE), "eye", ActionTone.SUCCESS,
                    () -> send(rightPanel, RecipeEditAction.ENABLE, entry)));
            if (custom) actions.add(delete(rightPanel, entry));
        }
        return actions;
    }

    private static ContextAction modify(WorkspacePanel rightPanel, RecipeIndex.RecipeEntry entry) {
        return new ContextAction(I18n.get(RecipeKeys.RECIPE_MODIFY), "editor", ActionTone.PRIMARY,
                () -> rightPanel.enterModifyMode(entry));
    }

    private static ContextAction disable(WorkspacePanel rightPanel, RecipeIndex.RecipeEntry entry) {
        return new ContextAction(I18n.get(RecipeKeys.RECIPE_DISABLE), "eye-off", ActionTone.WARNING,
                () -> send(rightPanel, RecipeEditAction.DISABLE, entry));
    }

    private static ContextAction reset(WorkspacePanel rightPanel, RecipeIndex.RecipeEntry entry) {
        return new ContextAction(I18n.get(RecipeKeys.RECIPE_RESET), "repeat", ActionTone.SUCCESS,
                () -> {
                    send(rightPanel, RecipeEditAction.RESET, entry);
                    rightPanel.exitModifyMode();
                });
    }

    private static ContextAction delete(WorkspacePanel rightPanel, RecipeIndex.RecipeEntry entry) {
        return new ContextAction(I18n.get(RecipeKeys.RECIPE_DELETE), "delete", ActionTone.DANGER,
                () -> {
                    send(rightPanel, RecipeEditAction.DELETE, entry);
                    rightPanel.exitModifyModeIfTarget(entry);
                });
    }

    private static void send(WorkspacePanel rightPanel, RecipeEditAction action,
            RecipeIndex.RecipeEntry entry) {
        rightPanel.saver.sendRecipeEdit(action, entry.id(),
                ScreenFactory.emptyPayload(entry, rightPanel.getSelectedMachineUid()));
    }
}