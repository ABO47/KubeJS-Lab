package com.abo47.kubejslab.recipe.immersiveengineering;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.item.crafting.Recipe;

import com.abo47.kubejslab.recipe.model.LabIngredient;
import com.abo47.kubejslab.recipe.model.LabRecipeField;
import com.abo47.kubejslab.recipe.model.LabRecipeFieldValues;
import com.abo47.kubejslab.recipe.model.LabRecipeOutput;
import com.abo47.kubejslab.recipe.model.LabSlotDescriptor;
import com.abo47.kubejslab.recipe.model.LabSlotKind;
import com.abo47.kubejslab.recipe.model.LabSlotTint;

import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


public class BlueprintMachine extends ImmersiveEngineeringMachine {
    public BlueprintMachine() {
        super("blueprint", LabRecipeField.BLUEPRINT_CATEGORY);
    }

    @Override
    public List<LabSlotDescriptor> inputSlots() {
        return List.of(
                new LabSlotDescriptor(true, LabSlotKind.ITEM, 0, 0, false, LabSlotTint.BLUEPRINT),
                new LabSlotDescriptor(true, LabSlotKind.ITEM, 0, 1, true),
                new LabSlotDescriptor(true, LabSlotKind.ITEM, 1, 1, true),
                new LabSlotDescriptor(true, LabSlotKind.ITEM, 0, 2, true),
                new LabSlotDescriptor(true, LabSlotKind.ITEM, 1, 2, true),
                new LabSlotDescriptor(true, LabSlotKind.ITEM, 0, 3, true),
                new LabSlotDescriptor(true, LabSlotKind.ITEM, 1, 3, true));
    }

    @Override
    public List<LabSlotDescriptor> outputSlots() {
        return new ArrayList<>(List.of(new LabSlotDescriptor(false, LabSlotKind.ITEM, 3, 1, false)));
    }

    @Override
    public JsonObject buildJson(String type, List<LabIngredient> inputs, List<LabRecipeOutput> outputs,
            LabRecipeFieldValues values) {
        JsonObject json = new JsonObject();
        json.addProperty("type", type);
        json.addProperty("category", values.blueprintCategory());
        JsonArray recipeInputs = new JsonArray();
        for (LabIngredient input : inputs) {
            recipeInputs.add(ingredientWithSize(input));
        }
        json.add("inputs", recipeInputs);
        if (!outputs.isEmpty()) {
            json.add("result", readOutput(outputs.get(0)));
        }
        return json;
    }

    @Override
    public LabRecipeFieldValues prefill(LabRecipeFieldValues current, Recipe<?> original) {
        if (original instanceof BlueprintCraftingRecipe blueprints) {
            return new LabRecipeFieldValues(current.shapeless(), current.experience(), current.cookingTime(),
                    current.count(), current.processingTime(), current.heatRequirement(), current.keepHeldItem(),
                    current.acceptMirrored(), current.gridWidth(), current.gridHeight(),
                    current.energy(), current.creosoteAmount(), current.mold(), blueprints.blueprintCategory,
                    current.clocheRenderType(), current.clocheRenderBlock());
        }
        return current;
    }
}