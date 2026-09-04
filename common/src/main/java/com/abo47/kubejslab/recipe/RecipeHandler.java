package com.abo47.kubejslab.recipe;

import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

import com.abo47.kubejslab.recipe.model.RecipeField;
import com.abo47.kubejslab.recipe.model.RecipeFieldValues;
import com.abo47.kubejslab.recipe.model.RecipeIngredient;
import com.abo47.kubejslab.recipe.model.RecipeOutput;
import com.abo47.kubejslab.recipe.model.SlotDescriptor;

import com.google.gson.JsonObject;


public interface RecipeHandler {
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

    default List<SlotDescriptor> inputSlots() {
        return List.of();
    }

    default List<SlotDescriptor> outputSlots() {
        return List.of();
    }

    default boolean supportsFluidInputAmount() {
        return false;
    }

    default boolean supportsFluidOutputAmount() {
        return false;
    }

    List<RecipeField> fields();

    JsonObject buildJson(String jsonType, List<RecipeIngredient> inputs, List<RecipeOutput> outputs,
            RecipeFieldValues values);

    default RecipeFieldValues prefill(RecipeFieldValues current, Recipe<?> original) {
        return current;
    }
}
