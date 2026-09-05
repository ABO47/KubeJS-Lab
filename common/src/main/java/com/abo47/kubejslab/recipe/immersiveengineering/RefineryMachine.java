package com.abo47.kubejslab.recipe.immersiveengineering;

import java.util.List;

import net.minecraft.world.item.crafting.Recipe;

import com.abo47.kubejslab.recipe.model.RecipeField;
import com.abo47.kubejslab.recipe.model.RecipeFieldValues;
import com.abo47.kubejslab.recipe.model.RecipeIngredient;
import com.abo47.kubejslab.recipe.model.RecipeJson;
import com.abo47.kubejslab.recipe.model.RecipeOutput;
import com.abo47.kubejslab.recipe.model.SlotDescriptor;
import com.abo47.kubejslab.recipe.model.SlotKind;

import blusunrize.immersiveengineering.api.crafting.RefineryRecipe;
import com.google.gson.JsonObject;


public class RefineryMachine extends ImmersiveEngineeringMachine {
    public RefineryMachine() {
        super("refinery", RecipeField.ENERGY, RecipeField.FLUID_INPUT_AMOUNT,
                RecipeField.FLUID_OUTPUT_AMOUNT);
    }

    @Override
    public boolean supportsFluidInputAmount() {
        return true;
    }

    @Override
    public boolean supportsFluidOutputAmount() {
        return true;
    }

    @Override
    public List<SlotDescriptor> inputSlots() {
        return List.of(
                new SlotDescriptor(true, SlotKind.FLUID, 0, 0, false),
                new SlotDescriptor(true, SlotKind.FLUID, 1, 0, true),
                new SlotDescriptor(true, SlotKind.ITEM, 0, 1, true));
    }

    @Override
    public List<SlotDescriptor> outputSlots() {
        return List.of(new SlotDescriptor(false, SlotKind.FLUID, 2, 1, false));
    }

    @Override
    public JsonObject buildJson(String type, List<RecipeIngredient> inputs, List<RecipeOutput> outputs,
            RecipeFieldValues values) {
        JsonObject json = new JsonObject();
        json.addProperty("type", type);
        int fluidIndex = 0;
        for (RecipeIngredient input : inputs) {
            if (input instanceof RecipeIngredient.Fluid) {
                json.add(fluidIndex == 0 ? "input0" : "input1",
                        fluidTagInput(input, values.fluidInputAmount()));
                fluidIndex++;
            } else {
                json.add("catalyst", RecipeJson.ingredientJson(input));
            }
        }
        for (RecipeOutput output : outputs) {
            if (output instanceof RecipeOutput.Fluid) {
                json.add("result", outputWithAmount(output, values.fluidOutputAmount()));
            }
        }
        json.addProperty("energy", Math.max(0, values.energy()));
        return json;
    }

    @Override
    public RecipeFieldValues prefill(RecipeFieldValues current, Recipe<?> original) {
        if (original instanceof RefineryRecipe refinery) {
            int fluidIn = 0;
            if (refinery.input0 != null) {
                fluidIn = refinery.input0.getAmount();
            }
            return new RecipeFieldValues(current.shapeless(), current.experience(), current.cookingTime(),
                    current.count(), current.processingTime(), current.heatRequirement(), current.keepHeldItem(),
                    current.acceptMirrored(), current.gridWidth(), current.gridHeight(),
                    refinery.getTotalProcessEnergy(), current.creosoteAmount(), current.mold(),
                    current.blueprintCategory(), current.clocheRenderType(), current.clocheRenderBlock(),
                    fluidIn, 0);
        }
        return current;
    }
}