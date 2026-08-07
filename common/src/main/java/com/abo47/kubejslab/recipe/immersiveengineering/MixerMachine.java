package com.abo47.kubejslab.recipe.immersiveengineering;

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import blusunrize.immersiveengineering.api.crafting.MixerRecipe;

import com.abo47.kubejslab.recipe.model.LabIngredient;
import com.abo47.kubejslab.recipe.model.LabRecipeField;
import com.abo47.kubejslab.recipe.model.LabRecipeFieldValues;
import com.abo47.kubejslab.recipe.model.LabRecipeOutput;

import net.minecraft.world.item.crafting.Recipe;

public class MixerMachine extends ImmersiveEngineeringMachine {
    public MixerMachine() {
        super("mixer", LabRecipeField.ENERGY);
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
        json.add("inputs", itemInputs);
        if (fluidInput != null) {
            json.add("fluid", fluidInput);
        }
        for (LabRecipeOutput output : outputs) {
            if (output instanceof LabRecipeOutput.Fluid) {
                json.add("result", readOutput(output));
            }
        }
        json.addProperty("energy", Math.max(0, values.energy()));
        return json;
    }

    @Override
    public LabRecipeFieldValues prefill(LabRecipeFieldValues current, Recipe<?> original) {
        if (original instanceof MixerRecipe mixer) {
            return new LabRecipeFieldValues(current.shapeless(), current.experience(), current.cookingTime(),
                    current.count(), current.processingTime(), current.heatRequirement(), current.keepHeldItem(),
                    current.acceptMirrored(), current.gridWidth(), current.gridHeight(), current.outputCount(),
                    mixer.getTotalProcessEnergy(), current.creosoteAmount(), current.mold(), current.blueprintCategory(),
                    current.clocheRenderType(), current.clocheRenderBlock());
        }
        return current;
    }
}