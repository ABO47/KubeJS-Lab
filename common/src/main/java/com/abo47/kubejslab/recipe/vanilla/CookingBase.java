package com.abo47.kubejslab.recipe.vanilla;

import java.util.List;

import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;

import com.abo47.kubejslab.recipe.RecipeHandler;
import com.abo47.kubejslab.recipe.model.RecipeField;
import com.abo47.kubejslab.recipe.model.RecipeFieldValues;
import com.abo47.kubejslab.recipe.model.RecipeIngredient;
import com.abo47.kubejslab.recipe.model.RecipeJson;
import com.abo47.kubejslab.recipe.model.RecipeOutput;
import com.abo47.kubejslab.recipe.model.SlotDescriptor;
import com.abo47.kubejslab.recipe.model.SlotLayouts;

import com.google.gson.JsonObject;


public abstract class CookingBase implements RecipeHandler {
    @Override
    public List<RecipeField> fields() {
        return List.of(RecipeField.EXPERIENCE, RecipeField.COOKING_TIME);
    }

    @Override
    public List<SlotDescriptor> inputSlots() {
        return SlotLayouts.oneInput();
    }

    @Override
    public List<SlotDescriptor> outputSlots() {
        return SlotLayouts.oneOutput();
    }

    @Override
    public JsonObject buildJson(String jsonType, List<RecipeIngredient> inputs, List<RecipeOutput> outputs,
            RecipeFieldValues values) {
        if (inputs.isEmpty() || RecipeOutput.firstItem(outputs).isEmpty()) {
            return null;
        }
        JsonObject json = new JsonObject();
        json.addProperty("type", jsonType);
        json.add("ingredient", RecipeJson.ingredientJson(inputs.get(0)));
        json.add("result", RecipeJson.itemWithCount(RecipeOutput.firstItem(outputs)));
        json.addProperty("experience", values.experience());
        json.addProperty("cookingtime", values.cookingTime());
        return json;
    }

    @Override
    public RecipeFieldValues prefill(RecipeFieldValues current, Recipe<?> original) {
        if (original instanceof AbstractCookingRecipe cooking) {
            return new RecipeFieldValues(current.shapeless(), cooking.getExperience(),
                    cooking.getCookingTime(), current.count(), current.processingTime(), current.heatRequirement(),
                    current.keepHeldItem(), current.acceptMirrored(), current.gridWidth(), current.gridHeight());
        }
        return current;
    }
}
