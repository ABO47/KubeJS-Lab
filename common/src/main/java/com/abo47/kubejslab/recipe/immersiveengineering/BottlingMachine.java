package com.abo47.kubejslab.recipe.immersiveengineering;

import java.util.List;

import net.minecraft.world.item.crafting.Recipe;

import com.abo47.kubejslab.recipe.model.LabIngredient;
import com.abo47.kubejslab.recipe.model.LabRecipeField;
import com.abo47.kubejslab.recipe.model.LabRecipeFieldValues;
import com.abo47.kubejslab.recipe.model.LabRecipeOutput;
import com.abo47.kubejslab.recipe.model.LabSlotDescriptor;
import com.abo47.kubejslab.recipe.model.LabSlotKind;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


public class BottlingMachine extends ImmersiveEngineeringMachine {
    public BottlingMachine() {
        super("bottling_machine", LabRecipeField.FLUID_INPUT_AMOUNT);
    }

    @Override
    public boolean supportsFluidInputAmount() {
        return true;
    }

    @Override
    public List<LabSlotDescriptor> inputSlots() {
        return List.of(
                new LabSlotDescriptor(true, LabSlotKind.ITEM, 0, 0, true),
                new LabSlotDescriptor(true, LabSlotKind.ITEM, 0, 1, true),
                new LabSlotDescriptor(true, LabSlotKind.FLUID, 2, 0, false));
    }

    @Override
    public List<LabSlotDescriptor> outputSlots() {
        return List.of(
                new LabSlotDescriptor(false, LabSlotKind.ITEM, 4, 0, false),
                new LabSlotDescriptor(false, LabSlotKind.ITEM, 4, 1, true),
                new LabSlotDescriptor(false, LabSlotKind.ITEM, 4, 2, true));
    }

    @Override
    public JsonObject buildJson(String type, List<LabIngredient> inputs, List<LabRecipeOutput> outputs,
            LabRecipeFieldValues values) {
        JsonObject json = new JsonObject();
        json.addProperty("type", type);
        JsonArray itemInputs = new JsonArray();
        JsonObject fluidInput = null;
        for (LabIngredient input : inputs) {
            if (input instanceof LabIngredient.Fluid) {
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
        for (LabRecipeOutput output : outputs) {
            results.add(readOutput(output));
        }
        json.add("results", results);
        return json;
    }

    @Override
    public LabRecipeFieldValues prefill(LabRecipeFieldValues current, Recipe<?> original) {
        return current;
    }
}