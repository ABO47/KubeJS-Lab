package com.abo47.kubejslab.recipe.vanilla;

import java.util.List;

import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;

import com.abo47.kubejslab.recipe.LabRecipeMachine;
import com.abo47.kubejslab.recipe.model.LabIngredient;
import com.abo47.kubejslab.recipe.model.LabRecipeField;
import com.abo47.kubejslab.recipe.model.LabRecipeFieldValues;
import com.abo47.kubejslab.recipe.model.LabRecipeJson;
import com.abo47.kubejslab.recipe.model.LabRecipeOutput;

import com.google.gson.JsonObject;


public abstract class CookingBase implements LabRecipeMachine {
    @Override
    public List<LabRecipeField> fields() {
        return List.of(LabRecipeField.EXPERIENCE, LabRecipeField.COOKING_TIME);
    }

    @Override
    public JsonObject buildJson(String jsonType, List<LabIngredient> inputs, List<LabRecipeOutput> outputs,
            LabRecipeFieldValues values) {
        if (inputs.isEmpty() || LabRecipeOutput.firstItem(outputs).isEmpty()) {
            return null;
        }
        JsonObject json = new JsonObject();
        json.addProperty("type", jsonType);
        json.add("ingredient", LabRecipeJson.ingredientJson(inputs.get(0)));
        json.add("result", LabRecipeJson.itemWithCount(LabRecipeOutput.firstItem(outputs)));
        json.addProperty("experience", values.experience());
        json.addProperty("cookingtime", values.cookingTime());
        return json;
    }

    @Override
    public LabRecipeFieldValues prefill(LabRecipeFieldValues current, Recipe<?> original) {
        if (original instanceof AbstractCookingRecipe cooking) {
            return new LabRecipeFieldValues(current.shapeless(), cooking.getExperience(),
                    cooking.getCookingTime(), current.count(), current.processingTime(), current.heatRequirement(),
                    current.keepHeldItem(), current.acceptMirrored(), current.gridWidth(), current.gridHeight());
        }
        return current;
    }
}
