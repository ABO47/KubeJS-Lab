package com.abo47.kubejslab.recipe.vanilla;

import java.util.List;

import com.google.gson.JsonObject;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;

import com.abo47.kubejslab.recipe.LabRecipeMachine;
import com.abo47.kubejslab.recipe.model.LabRecipeField;
import com.abo47.kubejslab.recipe.model.LabRecipeFieldValues;
import com.abo47.kubejslab.recipe.model.LabRecipeJson;

public abstract class CookingBase implements LabRecipeMachine {
    @Override
    public List<LabRecipeField> fields() {
        return List.of(LabRecipeField.EXPERIENCE, LabRecipeField.COOKING_TIME);
    }

    @Override
    public JsonObject buildJson(String jsonType, List<ItemStack> inputs, ItemStack output, LabRecipeFieldValues values) {
        if (inputs.isEmpty() || output.isEmpty()) {
            return null;
        }
        JsonObject json = new JsonObject();
        json.addProperty("type", jsonType);
        json.add("ingredient", LabRecipeJson.itemJson(inputs.get(0)));
        json.add("result", LabRecipeJson.itemWithCount(output));
        json.addProperty("experience", values.experience());
        json.addProperty("cookingtime", values.cookingTime());
        return json;
    }

    @Override
    public LabRecipeFieldValues prefill(LabRecipeFieldValues current, Recipe<?> original) {
        if (original instanceof AbstractCookingRecipe cooking) {
            return new LabRecipeFieldValues(current.shapeless(), cooking.getExperience(),
                    cooking.getCookingTime(), current.count(), current.processingTime(), current.heatRequirement(),
                    current.keepHeldItem());
        }
        return current;
    }
}
