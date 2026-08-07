package com.abo47.kubejslab.recipe.immersiveengineering;

import java.util.List;

import net.minecraft.world.item.crafting.Recipe;

import com.abo47.kubejslab.recipe.model.LabIngredient;
import com.abo47.kubejslab.recipe.model.LabRecipeField;
import com.abo47.kubejslab.recipe.model.LabRecipeFieldValues;
import com.abo47.kubejslab.recipe.model.LabRecipeJson;
import com.abo47.kubejslab.recipe.model.LabRecipeOutput;
import com.abo47.kubejslab.recipe.model.LabSlotDescriptor;
import com.abo47.kubejslab.recipe.model.LabSlotKind;

import blusunrize.immersiveengineering.api.crafting.RefineryRecipe;
import com.google.gson.JsonObject;


public class RefineryMachine extends ImmersiveEngineeringMachine {
    public RefineryMachine() {
        super("refinery", LabRecipeField.ENERGY, LabRecipeField.FLUID_INPUT_AMOUNT,
                LabRecipeField.FLUID_OUTPUT_AMOUNT);
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
    public List<LabSlotDescriptor> inputSlots() {
        return List.of(
                new LabSlotDescriptor(true, LabSlotKind.FLUID, 0, 0, false),
                new LabSlotDescriptor(true, LabSlotKind.FLUID, 1, 0, true),
                new LabSlotDescriptor(true, LabSlotKind.ITEM, 0, 1, true));
    }

    @Override
    public List<LabSlotDescriptor> outputSlots() {
        return List.of(new LabSlotDescriptor(false, LabSlotKind.FLUID, 2, 1, false));
    }

    @Override
    public JsonObject buildJson(String type, List<LabIngredient> inputs, List<LabRecipeOutput> outputs,
            LabRecipeFieldValues values) {
        JsonObject json = new JsonObject();
        json.addProperty("type", type);
        int fluidIndex = 0;
        for (LabIngredient input : inputs) {
            if (input instanceof LabIngredient.Fluid) {
                json.add(fluidIndex == 0 ? "input0" : "input1",
                        fluidTagInput(input, values.fluidInputAmount()));
                fluidIndex++;
            } else {
                json.add("catalyst", LabRecipeJson.ingredientJson(input));
            }
        }
        for (LabRecipeOutput output : outputs) {
            if (output instanceof LabRecipeOutput.Fluid) {
                json.add("result", outputWithAmount(output, values.fluidOutputAmount()));
            }
        }
        json.addProperty("energy", Math.max(0, values.energy()));
        return json;
    }

    @Override
    public LabRecipeFieldValues prefill(LabRecipeFieldValues current, Recipe<?> original) {
        if (original instanceof RefineryRecipe refinery) {
            int fluidIn = 0;
            if (refinery.input0 != null) {
                fluidIn = refinery.input0.getAmount();
            }
            return new LabRecipeFieldValues(current.shapeless(), current.experience(), current.cookingTime(),
                    current.count(), current.processingTime(), current.heatRequirement(), current.keepHeldItem(),
                    current.acceptMirrored(), current.gridWidth(), current.gridHeight(),
                    refinery.getTotalProcessEnergy(), current.creosoteAmount(), current.mold(),
                    current.blueprintCategory(), current.clocheRenderType(), current.clocheRenderBlock(),
                    fluidIn, 0);
        }
        return current;
    }
}