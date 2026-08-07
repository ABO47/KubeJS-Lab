package com.abo47.kubejslab.recipe.immersiveengineering;

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import com.abo47.kubejslab.recipe.model.LabIngredient;
import com.abo47.kubejslab.recipe.model.LabRecipeFieldValues;
import com.abo47.kubejslab.recipe.model.LabRecipeOutput;

import net.minecraft.world.item.crafting.Recipe;

public class BottlingMachine extends ImmersiveEngineeringMachine {
    public BottlingMachine() {
        super("bottling_machine");
    }

    @Override
    public boolean supportsOutputCount() {
        return true;
    }

    @Override
    public JsonObject buildJson(String type, List<LabIngredient> inputs, List<LabRecipeOutput> outputs,
            LabRecipeFieldValues values) {
        JsonObject json = new JsonObject();
        json.addProperty("type", type);
        JsonArray itemInputs = new JsonArray();
        JsonObject fluidInput = null;
        for (LabIngredient input : inputs) {
            if (input instanceof LabIngredient.Fluid) {
                fluidInput = fluidTagInput(input);
            } else {
                itemInputs.add(ingredientWithSize(input));
            }
        }
        if (itemInputs.size() == 1) {
            json.add("input", itemInputs.get(0));
        } else if (itemInputs.size() > 1) {
            json.add("inputs", itemInputs);
        }
        if (fluidInput != null) {
            json.add("fluid", fluidInput);
        }
        JsonArray results = new JsonArray();
        for (LabRecipeOutput output : outputs) {
            results.add(readOutput(output));
        }
        json.add("results", results);
        return json;
    }

    @Override
    public LabRecipeFieldValues prefill(LabRecipeFieldValues current, Recipe<?> original) {
        return current;
    }
}