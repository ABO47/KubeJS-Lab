package com.abo47.kubejslab.recipe.immersiveengineering;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.item.crafting.Recipe;

import com.abo47.kubejslab.recipe.model.RecipeField;
import com.abo47.kubejslab.recipe.model.RecipeFieldValues;
import com.abo47.kubejslab.recipe.model.RecipeIngredient;
import com.abo47.kubejslab.recipe.model.RecipeOutput;
import com.abo47.kubejslab.recipe.model.SlotDescriptor;
import com.abo47.kubejslab.recipe.model.SlotKind;
import com.abo47.kubejslab.recipe.model.SlotTint;

import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


public class BlueprintMachine extends ImmersiveEngineeringMachine {
    public BlueprintMachine() {
        super("blueprint", RecipeField.BLUEPRINT_CATEGORY);
    }

    @Override
    public List<SlotDescriptor> inputSlots() {
        return List.of(
                new SlotDescriptor(true, SlotKind.ITEM, 0, 0, false, SlotTint.BLUEPRINT),
                new SlotDescriptor(true, SlotKind.ITEM, 0, 1, true),
                new SlotDescriptor(true, SlotKind.ITEM, 1, 1, true),
                new SlotDescriptor(true, SlotKind.ITEM, 0, 2, true),
                new SlotDescriptor(true, SlotKind.ITEM, 1, 2, true),
                new SlotDescriptor(true, SlotKind.ITEM, 0, 3, true),
                new SlotDescriptor(true, SlotKind.ITEM, 1, 3, true));
    }

    @Override
    public List<SlotDescriptor> outputSlots() {
        return new ArrayList<>(List.of(new SlotDescriptor(false, SlotKind.ITEM, 3, 1, false)));
    }

    @Override
    public JsonObject buildJson(String type, List<RecipeIngredient> inputs, List<RecipeOutput> outputs,
            RecipeFieldValues values) {
        JsonObject json = new JsonObject();
        json.addProperty("type", type);
        json.addProperty("category", values.blueprintCategory());
        JsonArray recipeInputs = new JsonArray();
        for (RecipeIngredient input : inputs) {
            recipeInputs.add(ingredientWithSize(input));
        }
        json.add("inputs", recipeInputs);
        if (!outputs.isEmpty()) {
            json.add("result", readOutput(outputs.get(0)));
        }
        return json;
    }

    @Override
    public RecipeFieldValues prefill(RecipeFieldValues current, Recipe<?> original) {
        if (original instanceof BlueprintCraftingRecipe blueprints) {
            return new RecipeFieldValues(current.shapeless(), current.experience(), current.cookingTime(),
                    current.count(), current.processingTime(), current.heatRequirement(), current.keepHeldItem(),
                    current.acceptMirrored(), current.gridWidth(), current.gridHeight(),
                    current.energy(), current.creosoteAmount(), current.mold(), blueprints.blueprintCategory,
                    current.clocheRenderType(), current.clocheRenderBlock());
        }
        return current;
    }
}