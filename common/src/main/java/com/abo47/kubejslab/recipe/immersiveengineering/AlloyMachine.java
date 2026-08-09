package com.abo47.kubejslab.recipe.immersiveengineering;

import java.util.List;

import net.minecraft.world.item.crafting.Recipe;

import com.abo47.kubejslab.recipe.model.LabIngredient;
import com.abo47.kubejslab.recipe.model.LabRecipeField;
import com.abo47.kubejslab.recipe.model.LabRecipeFieldValues;
import com.abo47.kubejslab.recipe.model.LabRecipeOutput;
import com.abo47.kubejslab.recipe.model.LabSlotDescriptor;
import com.abo47.kubejslab.recipe.model.LabSlotKind;

import blusunrize.immersiveengineering.api.crafting.AlloyRecipe;
import com.google.gson.JsonObject;


public class AlloyMachine extends ImmersiveEngineeringMachine {
    public AlloyMachine() {
        super("alloy", LabRecipeField.PROCESSING_TIME);
    }

    @Override
    public List<LabSlotDescriptor> inputSlots() {
        return List.of(
                new LabSlotDescriptor(true, LabSlotKind.ITEM, 0, 0, false),
                new LabSlotDescriptor(true, LabSlotKind.ITEM, 1, 0, false));
    }

    @Override
    public List<LabSlotDescriptor> outputSlots() {
        return List.of(new LabSlotDescriptor(false, LabSlotKind.ITEM, 0, 0, false));
    }

    @Override
    public JsonObject buildJson(String type, List<LabIngredient> inputs, List<LabRecipeOutput> outputs,
            LabRecipeFieldValues values) {
        JsonObject json = new JsonObject();
        json.addProperty("type", type);
        if (!inputs.isEmpty()) {
            json.add("input0", ingredientWithSize(inputs.get(0)));
        }
        if (inputs.size() > 1) {
            json.add("input1", ingredientWithSize(inputs.get(1)));
        }
        LabRecipeOutput result = outputs.isEmpty() ? null : outputs.get(0);
        if (result != null) {
            json.add("result", readOutput(result));
        }
        json.addProperty("time", Math.max(1, values.processingTime()));
        return json;
    }

    @Override
    public LabRecipeFieldValues prefill(LabRecipeFieldValues current, Recipe<?> original) {
        if (original instanceof AlloyRecipe alloy) {
            return new LabRecipeFieldValues(current.shapeless(), current.experience(), current.cookingTime(),
                    current.count(), alloy.time, current.heatRequirement(), current.keepHeldItem(),
                    current.acceptMirrored(), current.gridWidth(), current.gridHeight(),
                    current.energy(), current.creosoteAmount(), current.mold(), current.blueprintCategory(),
                    current.clocheRenderType(), current.clocheRenderBlock());
        }
        return current;
    }
}