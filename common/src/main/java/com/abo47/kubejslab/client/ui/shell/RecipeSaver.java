package com.abo47.kubejslab.client.ui.shell;

import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.ItemStack;

import com.abo47.kubejslab.KubeJSLab;
import com.abo47.kubejslab.client.ui.machines.MachineCatalog;
import com.abo47.kubejslab.client.ui.machines.MachineView;
import com.abo47.kubejslab.client.ui.recipes.RecipeIndex;
import com.abo47.kubejslab.client.ui.recipes.RecipeStates;
import com.abo47.kubejslab.network.NetworkRegistry;
import com.abo47.kubejslab.network.recipe.C2SRecipeEditPacket;
import com.abo47.kubejslab.recipe.MachineRegistry;
import com.abo47.kubejslab.recipe.RecipeHandler;
import com.abo47.kubejslab.recipe.model.RecipeEditAction;
import com.abo47.kubejslab.recipe.model.RecipeFieldValues;
import com.abo47.kubejslab.recipe.model.RecipeIngredient;
import com.abo47.kubejslab.recipe.model.RecipeOutput;
import com.abo47.kubejslab.recipe.model.RecipePayload;


final class RecipeSaver {
    private final WorkspacePanel panel;

    RecipeSaver(WorkspacePanel panel) {
        this.panel = panel;
    }

    void saveRecipe() {
        KubeJSLab.LOGGER.info("[RecipeSaver] saveRecipe: mode={}, modifyTarget={}", panel.recipes.mode,
                panel.recipes.modifyTarget == null ? null : panel.recipes.modifyTarget.id());
        boolean overriding = panel.recipes.mode == WorkspacePanel.EditMode.MODIFY && panel.recipes.modifyTarget != null;
        if (!overriding) {
            saveNewRecipe();
            return;
        }
        ResourceLocation uid = resolveModifyUid(panel.recipes.modifyTarget);
        if (uid == null) {
            saveGenericOverride();
            return;
        }
        RecipeHandler support = MachineRegistry.get(uid);
        if (support == null) {
            return;
        }
        List<RecipeIngredient> inputs = panel.machineLayout.getInputs();
        if (!hasInput(inputs)) {
            KubeJSLab.LOGGER.info("[RecipeSaver] saveRecipe: no inputs, aborting");
            return;
        }
        List<RecipeOutput> outputs = panel.machineLayout.getOutputs();
        Recipe<?> original = RecipeIndex.recipeById(panel.recipes.modifyTarget.id());
        if (outputs.isEmpty() && (original == null || !support.allowsEmptyResult(original))) {
            KubeJSLab.LOGGER.info("[RecipeSaver] saveRecipe: no outputs, aborting");
            return;
        }
        KubeJSLab.LOGGER.info("[RecipeSaver] OVERRIDE {}: inputs={}, outputs={}, values={}", uid, inputs.size(),
                outputs.size(), panel.settingsWidget.getValues());
        sendRecipeEdit(RecipeEditAction.OVERRIDE, panel.recipes.modifyTarget.id(),
                new RecipePayload(uid, inputs, outputs,
                        outputName(outputs), panel.settingsWidget.getValues()));
    }

    private void saveNewRecipe() {
        MachineView machine = panel.machineDropdown.getSelectedMachine();
        if (machine == null) {
            return;
        }
        RecipeHandler support = MachineRegistry.get(machine.recipeTypeUid());
        if (support == null) {
            return;
        }
        List<RecipeIngredient> inputs = panel.machineLayout.getInputs();
        if (!hasInput(inputs)) {
            KubeJSLab.LOGGER.info("[RecipeSaver] saveNewRecipe: no inputs, aborting");
            return;
        }
        List<RecipeOutput> outputs = panel.machineLayout.getOutputs();
        if (outputs.isEmpty()) {
            KubeJSLab.LOGGER.info("[RecipeSaver] saveNewRecipe: no outputs, aborting");
            return;
        }
        KubeJSLab.LOGGER.info("[RecipeSaver] SAVE_NEW {}: inputs={}, outputs={}, values={}",
                machine.recipeTypeUid(), inputs.size(), outputs.size(), panel.settingsWidget.getValues());
        sendRecipeEdit(RecipeEditAction.SAVE_NEW, null,
                new RecipePayload(machine.recipeTypeUid(), inputs, outputs,
                        outputName(outputs), panel.settingsWidget.getValues()));
    }

    private void saveGenericOverride() {
        List<RecipeIngredient> inputs = panel.machineLayout.getInputs();
        if (!hasInput(inputs)) {
            return;
        }
        List<RecipeOutput> outputs = panel.machineLayout.getOutputs();
        if (outputs.isEmpty()) {
            return;
        }
        KubeJSLab.LOGGER.info("[RecipeSaver] saveGenericOverride: inputs={}, outputs={}", inputs.size(), outputs.size());
        sendRecipeEdit(RecipeEditAction.OVERRIDE, panel.recipes.modifyTarget.id(),
                new RecipePayload(null, inputs, outputs,
                        outputName(outputs), RecipeFieldValues.defaults()));
    }

    private static boolean hasInput(List<RecipeIngredient> inputs) {
        for (RecipeIngredient input : inputs) {
            if (!input.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static String outputName(List<RecipeOutput> outputs) {
        ItemStack item = RecipeOutput.firstItem(outputs);
        if (!item.isEmpty()) {
            return item.getHoverName().getString();
        }
        for (RecipeOutput output : outputs) {
            if (output instanceof RecipeOutput.Fluid fluid && !fluid.fluid().isEmpty()) {
                return fluid.fluid().getDisplayName().getString();
            }
        }
        return "";
    }

    ResourceLocation resolveModifyUid(RecipeIndex.RecipeEntry entry) {
        ResourceLocation uid = RecipeStates.machineUidOf(entry.id());
        if (uid != null && MachineRegistry.supports(uid)) {
            return uid;
        }
        MachineView machine = MachineCatalog.machineFor(entry.id());
        if (machine != null && machine.supported()) {
            return machine.recipeTypeUid();
        }
        return null;
    }

    void sendRecipeEdit(RecipeEditAction action, ResourceLocation targetId, RecipePayload payload) {
        NetworkRegistry.sendRecipeEdit(new C2SRecipeEditPacket(action, targetId, payload));
    }
}