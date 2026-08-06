package com.abo47.kubejslab.recipe.vanilla;

import java.util.List;

import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;

import com.abo47.kubejslab.recipe.LabRecipeMachine;
import com.abo47.kubejslab.recipe.model.LabIngredient;
import com.abo47.kubejslab.recipe.model.LabRecipeField;
import com.abo47.kubejslab.recipe.model.LabRecipeFieldValues;
import com.abo47.kubejslab.recipe.model.LabRecipeJson;
import com.abo47.kubejslab.recipe.model.LabRecipeOutput;

public final class SmithingMachine implements LabRecipeMachine {
    private static final ResourceLocation JEI_UID = new ResourceLocation("minecraft", "smithing");
    private static final String TRANSFORM_TYPE = "minecraft:smithing_transform";
    private static final String TRIM_TYPE = "minecraft:smithing_trim";

    @Override
    public ResourceLocation jeiUid() {
        return JEI_UID;
    }

    @Override
    public String jsonType() {
        return TRANSFORM_TYPE;
    }

    @Override
    public String jsonTypeFor(Recipe<?> original) {
        return original instanceof SmithingTrimRecipe ? TRIM_TYPE : TRANSFORM_TYPE;
    }

    @Override
    public List<LabRecipeField> fields() {
        return List.of();
    }

    @Override
    public boolean allowsEmptyResult(Recipe<?> original) {
        return original instanceof SmithingTrimRecipe;
    }

    @Override
    public JsonObject buildJson(String jsonType, List<LabIngredient> inputs, List<LabRecipeOutput> outputs,
            LabRecipeFieldValues values) {
        if (inputs.size() < 3) {
            return null;
        }
        JsonObject json = new JsonObject();
        json.addProperty("type", jsonType);
        json.add("template", LabRecipeJson.ingredientJson(inputs.get(0)));
        json.add("base", LabRecipeJson.ingredientJson(inputs.get(1)));
        json.add("addition", LabRecipeJson.ingredientJson(inputs.get(2)));
        if (!TRIM_TYPE.equals(jsonType)) {
            json.add("result", LabRecipeJson.itemWithCount(LabRecipeOutput.firstItem(outputs)));
        }
        return json;
    }
}
