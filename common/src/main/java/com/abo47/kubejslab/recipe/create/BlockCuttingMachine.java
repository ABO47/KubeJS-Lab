package com.abo47.kubejslab.recipe.create;

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
import com.simibubi.create.compat.jei.category.BlockCuttingCategory.CondensedBlockCuttingRecipe;


public final class BlockCuttingMachine implements RecipeHandler {
    private static final ResourceLocation JEI_UID = new ResourceLocation("create", "block_cutting");
    private static final String JSON_TYPE = "minecraft:stonecutting";

    @Override
    public ResourceLocation jeiUid() {
        return JEI_UID;
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
    public String displayLabel() {
        return "cutting";
    }

    @Override
    public ResourceLocation recipeIdSourceUid() {
        return new ResourceLocation("minecraft", "stonecutting");
    }

    @Override
    public String jsonType() {
        return JSON_TYPE;
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
        int count = current.count();
        if (original instanceof CondensedBlockCuttingRecipe condensed && !condensed.getOutputs().isEmpty()) {
            count = condensed.getOutputs().get(0).getCount();
        } else if (original instanceof StonecutterRecipe recipe) {
            count = recipe.getResultItem(RegistryAccess.EMPTY).getCount();
        }
        return new RecipeFieldValues(current.shapeless(), current.experience(), current.cookingTime(), count,
                current.processingTime(), current.heatRequirement(), current.keepHeldItem(), current.acceptMirrored(),
                current.gridWidth(), current.gridHeight());
    }
}
