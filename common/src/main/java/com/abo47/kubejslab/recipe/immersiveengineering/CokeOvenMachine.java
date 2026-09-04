package com.abo47.kubejslab.recipe.immersiveengineering;

import java.util.List;

import net.minecraft.world.item.crafting.Recipe;

import com.abo47.kubejslab.recipe.model.RecipeField;
import com.abo47.kubejslab.recipe.model.RecipeFieldValues;
import com.abo47.kubejslab.recipe.model.RecipeIngredient;
import com.abo47.kubejslab.recipe.model.RecipeOutput;
import com.abo47.kubejslab.recipe.model.SlotDescriptor;
import com.abo47.kubejslab.recipe.model.SlotKind;

import blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe;
import com.google.gson.JsonObject;


public class CokeOvenMachine extends ImmersiveEngineeringMachine {
    public CokeOvenMachine() {
        super("coke_oven", RecipeField.PROCESSING_TIME, RecipeField.CREOSOTE_AMOUNT);
    }

    @Override
    public List<SlotDescriptor> inputSlots() {
        return List.of(new SlotDescriptor(true, SlotKind.ITEM, 0, 0, false));
    }

    @Override
    public List<SlotDescriptor> outputSlots() {
        return List.of(new SlotDescriptor(false, SlotKind.ITEM, 0, 0, false));
    }

    @Override
    public JsonObject buildJson(String type, List<RecipeIngredient> inputs, List<RecipeOutput> outputs,
            RecipeFieldValues values) {
        JsonObject json = new JsonObject();
        json.addProperty("type", type);
        if (!inputs.isEmpty()) {
            json.add("input", ingredientWithSize(inputs.get(0)));
        }
        if (!outputs.isEmpty()) {
            json.add("result", readOutput(outputs.get(0)));
        }
        json.addProperty("time", values.processingTime());
        json.addProperty("creosote", values.creosoteAmount());
        return json;
    }

    @Override
    public RecipeFieldValues prefill(RecipeFieldValues current, Recipe<?> original) {
        if (original instanceof CokeOvenRecipe coke) {
            return new RecipeFieldValues(current.shapeless(), current.experience(), current.cookingTime(),
                    current.count(), coke.time, current.heatRequirement(), current.keepHeldItem(),
                    current.acceptMirrored(), current.gridWidth(), current.gridHeight(),
                    current.energy(), coke.creosoteOutput, current.mold(), current.blueprintCategory(),
                    current.clocheRenderType(), current.clocheRenderBlock());
        }
        return current;
    }
}