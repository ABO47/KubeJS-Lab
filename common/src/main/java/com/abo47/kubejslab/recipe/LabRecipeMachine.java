package com.abo47.kubejslab.recipe;

import java.util.List;

import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

import com.abo47.kubejslab.recipe.model.LabIngredient;
import com.abo47.kubejslab.recipe.model.LabRecipeField;
import com.abo47.kubejslab.recipe.model.LabRecipeFieldValues;
import com.abo47.kubejslab.recipe.model.LabRecipeOutput;

public interface LabRecipeMachine {
    ResourceLocation jeiUid();

    String jsonType();

    default String jsonTypeFor(Recipe<?> original) {
        return jsonType();
    }

    default boolean gridLayout() {
        return false;
    }

    default boolean supportsGridSize() {
        return false;
    }

    default boolean supportsChance() {
        return false;
    }

    default boolean supportsOutputCount() {
        return false;
    }

    default boolean allowsEmptyResult(Recipe<?> original) {
        return false;
    }

    default List<Float> outputChances(Recipe<?> original) {
        return List.of();
    }

    default ResourceLocation tagForInput(Recipe<?> original, int inputSlotIndex) {
        return null;
    }

    default String displayLabel() {
        return null;
    }

    default ResourceLocation recipeIdSourceUid() {
        return null;
    }

    List<LabRecipeField> fields();

    JsonObject buildJson(String jsonType, List<LabIngredient> inputs, List<LabRecipeOutput> outputs,
            LabRecipeFieldValues values);

    default LabRecipeFieldValues prefill(LabRecipeFieldValues current, Recipe<?> original) {
        return current;
    }
}
