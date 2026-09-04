package com.abo47.kubejslab.recipe.immersiveengineering;

import java.util.List;

import net.minecraft.world.item.crafting.Recipe;

import com.abo47.kubejslab.recipe.model.RecipeField;
import com.abo47.kubejslab.recipe.model.RecipeFieldValues;
import com.abo47.kubejslab.recipe.model.RecipeIngredient;
import com.abo47.kubejslab.recipe.model.RecipeOutput;
import com.abo47.kubejslab.recipe.model.SlotDescriptor;
import com.abo47.kubejslab.recipe.model.SlotKind;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


public class BottlingMachine extends ImmersiveEngineeringMachine {
    public BottlingMachine() {
        super("bottling_machine", RecipeField.FLUID_INPUT_AMOUNT);
    }

    @Override
    public boolean supportsFluidInputAmount() {
        return true;
    }

    @Override
    public List<SlotDescriptor> inputSlots() {
        return List.of(
                new SlotDescriptor(true, SlotKind.ITEM, 0, 0, true),
                new SlotDescriptor(true, SlotKind.ITEM, 0, 1, true),
                new SlotDescriptor(true, SlotKind.FLUID, 2, 0, false));
    }

    @Override
    public List<SlotDescriptor> outputSlots() {
        return List.of(
                new SlotDescriptor(false, SlotKind.ITEM, 4, 0, false),
                new SlotDescriptor(false, SlotKind.ITEM, 4, 1, true),
                new SlotDescriptor(false, SlotKind.ITEM, 4, 2, true));
    }

    @Override
    public JsonObject buildJson(String type, List<RecipeIngredient> inputs, List<RecipeOutput> outputs,
            RecipeFieldValues values) {
        JsonObject json = new JsonObject();
        json.addProperty("type", type);
        JsonArray itemInputs = new JsonArray();
        JsonObject fluidInput = null;
        for (RecipeIngredient input : inputs) {
            if (input instanceof RecipeIngredient.Fluid) {
                fluidInput = fluidTagInput(input, values.fluidInputAmount());
            } else {
                itemInputs.add(ingredientWithSize(input));
            }
        }
        if (itemInputs.size() == 1) {
            json.add("input", itemInputs.get(0));
        } else if (itemInputs.size() > 1) {
            json.add("inputs", itemInputs);
        }
        if (fluidInput != null) {
            json.add("fluid", fluidInput);
        }
        JsonArray results = new JsonArray();
        for (RecipeOutput output : outputs) {
            results.add(readOutput(output));
        }
        json.add("results", results);
        return json;
    }

    @Override
    public RecipeFieldValues prefill(RecipeFieldValues current, Recipe<?> original) {
        return current;
    }
}