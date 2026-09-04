package com.abo47.kubejslab.recipe.vanilla;

import java.util.List;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;

import com.abo47.kubejslab.recipe.RecipeHandler;
import com.abo47.kubejslab.recipe.model.RecipeField;
import com.abo47.kubejslab.recipe.model.RecipeFieldValues;
import com.abo47.kubejslab.recipe.model.RecipeIngredient;
import com.abo47.kubejslab.recipe.model.RecipeJson;
import com.abo47.kubejslab.recipe.model.RecipeOutput;
import com.abo47.kubejslab.recipe.model.SlotDescriptor;
import com.abo47.kubejslab.recipe.model.SlotLayouts;

import com.google.gson.JsonObject;


public final class StonecutterMachine implements RecipeHandler {
    private static final ResourceLocation JEI_UID = new ResourceLocation("minecraft", "stonecutting");
    private static final String JSON_TYPE = "minecraft:stonecutting";

    @Override
    public ResourceLocation jeiUid() {
        return JEI_UID;
    }

    @Override
    public String jsonType() {
        return JSON_TYPE;
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
    public List<RecipeField> fields() {
        return List.of(RecipeField.COUNT);
    }

    @Override
    public JsonObject buildJson(String jsonType, List<RecipeIngredient> inputs, List<RecipeOutput> outputs,
            RecipeFieldValues values) {
        if (inputs.isEmpty() || RecipeOutput.firstItem(outputs).isEmpty()) {
            return null;
        }
        JsonObject json = new JsonObject();
        json.addProperty("type", JSON_TYPE);
        json.add("ingredient", RecipeJson.ingredientJson(inputs.get(0)));
        json.addProperty("result", RecipeOutput.firstItem(outputs).getItem().builtInRegistryHolder().key().location()
                .toString());
        json.addProperty("count", values.count());
        return json;
    }

    @Override
    public RecipeFieldValues prefill(RecipeFieldValues current, Recipe<?> original) {
        if (original instanceof StonecutterRecipe recipe) {
            return new RecipeFieldValues(current.shapeless(), current.experience(), current.cookingTime(),
                    recipe.getResultItem(RegistryAccess.EMPTY).getCount(), current.processingTime(),
                    current.heatRequirement(), current.keepHeldItem(), current.acceptMirrored(),
                    current.gridWidth(), current.gridHeight());
        }
        return current;
    }
}
