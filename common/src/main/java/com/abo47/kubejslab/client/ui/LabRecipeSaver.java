package com.abo47.kubejslab.client.ui;

import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.ItemStack;

import com.abo47.kubejslab.client.ui.machines.LabMachine;
import com.abo47.kubejslab.client.ui.machines.LabMachineCatalog;
import com.abo47.kubejslab.client.ui.recipes.LabRecipeIndex;
import com.abo47.kubejslab.client.ui.recipes.LabRecipeStates;
import com.abo47.kubejslab.KubeJSLab;
import com.abo47.kubejslab.network.ModNetwork;
import com.abo47.kubejslab.network.recipe.C2SRecipeEditPacket;
import com.abo47.kubejslab.recipe.LabRecipeMachine;
import com.abo47.kubejslab.recipe.LabRecipeMachines;
import com.abo47.kubejslab.recipe.model.LabIngredient;
import com.abo47.kubejslab.recipe.model.LabRecipeEditAction;
import com.abo47.kubejslab.recipe.model.LabRecipeFieldValues;
import com.abo47.kubejslab.recipe.model.LabRecipeOutput;
import com.abo47.kubejslab.recipe.model.LabRecipePayload;


final class LabRecipeSaver {
    private final LabScreen.LabPanelWidget panel;

    LabRecipeSaver(LabScreen.LabPanelWidget panel) {
        this.panel = panel;
    }

    void saveRecipe() {
        KubeJSLab.LOGGER.info("[LabScreen] saveRecipe: mode={}, modifyTarget={}", panel.mode,
                panel.modifyTarget == null ? null : panel.modifyTarget.id());
        boolean overriding = panel.mode == LabScreen.LabPanelWidget.EditMode.MODIFY && panel.modifyTarget != null;
        if (!overriding) {
            saveNewRecipe();
            return;
        }
        ResourceLocation uid = resolveModifyUid(panel.modifyTarget);
        if (uid == null) {
            saveGenericOverride();
            return;
        }
        LabRecipeMachine support = LabRecipeMachines.get(uid);
        if (support == null) {
            return;
        }
        List<LabIngredient> inputs = panel.machineLayout.getInputs();
        if (!hasInput(inputs)) {
            KubeJSLab.LOGGER.info("[LabScreen] saveRecipe: no inputs, aborting");
            return;
        }
        List<LabRecipeOutput> outputs = panel.machineLayout.getOutputs();
        Recipe<?> original = LabRecipeIndex.recipeById(panel.modifyTarget.id());
        if (outputs.isEmpty() && (original == null || !support.allowsEmptyResult(original))) {
            KubeJSLab.LOGGER.info("[LabScreen] saveRecipe: no outputs, aborting");
            return;
        }
        KubeJSLab.LOGGER.info("[LabScreen] OVERRIDE {}: inputs={}, outputs={}, values={}", uid, inputs.size(),
                outputs.size(), panel.settingsWidget.getValues());
        sendRecipeEdit(LabRecipeEditAction.OVERRIDE, panel.modifyTarget.id(),
                new LabRecipePayload(uid, inputs, outputs,
                        outputName(outputs), panel.settingsWidget.getValues()));
    }

    private void saveNewRecipe() {
        LabMachine machine = panel.machineDropdown.getSelectedMachine();
        if (machine == null) {
            return;
        }
        LabRecipeMachine support = LabRecipeMachines.get(machine.recipeTypeUid());
        if (support == null) {
            return;
        }
        List<LabIngredient> inputs = panel.machineLayout.getInputs();
        if (!hasInput(inputs)) {
            KubeJSLab.LOGGER.info("[LabSaver] saveNewRecipe: no inputs, aborting");
            return;
        }
        List<LabRecipeOutput> outputs = panel.machineLayout.getOutputs();
        if (outputs.isEmpty()) {
            KubeJSLab.LOGGER.info("[LabSaver] saveNewRecipe: no outputs, aborting");
            return;
        }
        KubeJSLab.LOGGER.info("[LabSaver] SAVE_NEW {}: inputs={}, outputs={}, values={}",
                machine.recipeTypeUid(), inputs.size(), outputs.size(), panel.settingsWidget.getValues());
        sendRecipeEdit(LabRecipeEditAction.SAVE_NEW, null,
                new LabRecipePayload(machine.recipeTypeUid(), inputs, outputs,
                        outputName(outputs), panel.settingsWidget.getValues()));
    }

    private void saveGenericOverride() {
        List<LabIngredient> inputs = panel.machineLayout.getInputs();
        if (!hasInput(inputs)) {
            return;
        }
        List<LabRecipeOutput> outputs = panel.machineLayout.getOutputs();
        if (outputs.isEmpty()) {
            return;
        }
        KubeJSLab.LOGGER.info("[LabSaver] saveGenericOverride: inputs={}, outputs={}", inputs.size(), outputs.size());
        sendRecipeEdit(LabRecipeEditAction.OVERRIDE, panel.modifyTarget.id(),
                new LabRecipePayload(null, inputs, outputs,
                        outputName(outputs), LabRecipeFieldValues.defaults()));
    }

    private static boolean hasInput(List<LabIngredient> inputs) {
        for (LabIngredient input : inputs) {
            if (!input.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static String outputName(List<LabRecipeOutput> outputs) {
        ItemStack item = LabRecipeOutput.firstItem(outputs);
        if (!item.isEmpty()) {
            return item.getHoverName().getString();
        }
        for (LabRecipeOutput output : outputs) {
            if (output instanceof LabRecipeOutput.Fluid fluid && !fluid.fluid().isEmpty()) {
                return fluid.fluid().getDisplayName().getString();
            }
        }
        return "";
    }

    ResourceLocation resolveModifyUid(LabRecipeIndex.LabRecipeEntry entry) {
        ResourceLocation uid = LabRecipeStates.machineUidOf(entry.id());
        if (uid != null && LabRecipeMachines.supports(uid)) {
            return uid;
        }
        LabMachine machine = LabMachineCatalog.machineFor(entry.id());
        if (machine != null && machine.supported()) {
            return machine.recipeTypeUid();
        }
        return null;
    }

    void sendRecipeEdit(LabRecipeEditAction action, ResourceLocation targetId, LabRecipePayload payload) {
        ModNetwork.sendRecipeEdit(new C2SRecipeEditPacket(action, targetId, payload));
    }
}