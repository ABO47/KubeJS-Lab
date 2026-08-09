package com.abo47.kubejslab.recipe.create;

import java.util.List;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;

import com.abo47.kubejslab.recipe.LabRecipeMachine;
import com.abo47.kubejslab.recipe.model.LabIngredient;
import com.abo47.kubejslab.recipe.model.LabRecipeField;
import com.abo47.kubejslab.recipe.model.LabRecipeFieldValues;
import com.abo47.kubejslab.recipe.model.LabRecipeJson;
import com.abo47.kubejslab.recipe.model.LabRecipeOutput;
import com.abo47.kubejslab.recipe.model.LabSlotDescriptor;
import com.abo47.kubejslab.recipe.model.LabSlotLayouts;

import com.google.gson.JsonObject;
import com.simibubi.create.compat.jei.category.BlockCuttingCategory.CondensedBlockCuttingRecipe;


public final class BlockCuttingMachine implements LabRecipeMachine {
    private static final ResourceLocation JEI_UID = new ResourceLocation("create", "block_cutting");
    private static final String JSON_TYPE = "minecraft:stonecutting";

    @Override
    public ResourceLocation jeiUid() {
        return JEI_UID;
    }

    @Override
    public List<LabSlotDescriptor> inputSlots() {
        return LabSlotLayouts.oneInput();
    }

    @Override
    public List<LabSlotDescriptor> outputSlots() {
        return LabSlotLayouts.oneOutput();
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
    public List<LabRecipeField> fields() {
        return List.of(LabRecipeField.COUNT);
    }

    @Override
    public JsonObject buildJson(String jsonType, List<LabIngredient> inputs, List<LabRecipeOutput> outputs,
            LabRecipeFieldValues values) {
        if (inputs.isEmpty() || LabRecipeOutput.firstItem(outputs).isEmpty()) {
            return null;
        }
        JsonObject json = new JsonObject();
        json.addProperty("type", JSON_TYPE);
        json.add("ingredient", LabRecipeJson.ingredientJson(inputs.get(0)));
        json.addProperty("result", LabRecipeOutput.firstItem(outputs).getItem().builtInRegistryHolder().key().location()
                .toString());
        json.addProperty("count", values.count());
        return json;
    }

    @Override
    public LabRecipeFieldValues prefill(LabRecipeFieldValues current, Recipe<?> original) {
        int count = current.count();
        if (original instanceof CondensedBlockCuttingRecipe condensed && !condensed.getOutputs().isEmpty()) {
            count = condensed.getOutputs().get(0).getCount();
        } else if (original instanceof StonecutterRecipe recipe) {
            count = recipe.getResultItem(RegistryAccess.EMPTY).getCount();
        }
        return new LabRecipeFieldValues(current.shapeless(), current.experience(), current.cookingTime(), count,
                current.processingTime(), current.heatRequirement(), current.keepHeldItem(), current.acceptMirrored(),
                current.gridWidth(), current.gridHeight());
    }
}
