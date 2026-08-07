package com.abo47.kubejslab.recipe.immersiveengineering;

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import blusunrize.immersiveengineering.api.crafting.AlloyRecipe;

import com.abo47.kubejslab.recipe.model.LabIngredient;
import com.abo47.kubejslab.recipe.model.LabRecipeField;
import com.abo47.kubejslab.recipe.model.LabRecipeFieldValues;
import com.abo47.kubejslab.recipe.model.LabRecipeOutput;

import net.minecraft.world.item.crafting.Recipe;

public class AlloyMachine extends ImmersiveEngineeringMachine {
    public AlloyMachine() {
        super("alloy", LabRecipeField.PROCESSING_TIME);
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
                    current.acceptMirrored(), current.gridWidth(), current.gridHeight(), current.outputCount(),
                    current.energy(), current.creosoteAmount(), current.mold(), current.blueprintCategory(),
                    current.clocheRenderType(), current.clocheRenderBlock());
        }
        return current;
    }
}