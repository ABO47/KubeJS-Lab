package com.abo47.kubejslab.recipe.immersiveengineering;

import java.util.List;

import net.minecraft.world.item.crafting.Recipe;

import com.abo47.kubejslab.recipe.model.RecipeField;
import com.abo47.kubejslab.recipe.model.RecipeFieldValues;
import com.abo47.kubejslab.recipe.model.RecipeIngredient;
import com.abo47.kubejslab.recipe.model.RecipeOutput;
import com.abo47.kubejslab.recipe.model.SlotDescriptor;
import com.abo47.kubejslab.recipe.model.SlotKind;

import blusunrize.immersiveengineering.api.crafting.BlastFurnaceRecipe;
import com.google.gson.JsonObject;


public class BlastFurnaceMachine extends ImmersiveEngineeringMachine {
    public BlastFurnaceMachine() {
        super("blast_furnace", RecipeField.PROCESSING_TIME);
    }

    @Override
    public List<SlotDescriptor> inputSlots() {
        return List.of(new SlotDescriptor(true, SlotKind.ITEM, 0, 0, false));
    }

    @Override
    public List<SlotDescriptor> outputSlots() {
        return List.of(
                new SlotDescriptor(false, SlotKind.ITEM, 0, 0, false),
                new SlotDescriptor(false, SlotKind.ITEM, 0, 1, true));
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
        if (outputs.size() > 1) {
            json.add("slag", readOutput(outputs.get(1)));
        }
        json.addProperty("time", values.processingTime());
        return json;
    }

    @Override
    public RecipeFieldValues prefill(RecipeFieldValues current, Recipe<?> original) {
        if (original instanceof BlastFurnaceRecipe blast) {
            return new RecipeFieldValues(current.shapeless(), current.experience(), current.cookingTime(),
                    current.count(), blast.time, current.heatRequirement(), current.keepHeldItem(),
                    current.acceptMirrored(), current.gridWidth(), current.gridHeight(),
                    current.energy(), current.creosoteAmount(), current.mold(), current.blueprintCategory(),
                    current.clocheRenderType(), current.clocheRenderBlock());
        }
        return current;
    }
}