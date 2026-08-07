package com.abo47.kubejslab.recipe.immersiveengineering;

import java.util.List;

import net.minecraft.world.item.crafting.Recipe;

import com.abo47.kubejslab.recipe.model.LabIngredient;
import com.abo47.kubejslab.recipe.model.LabRecipeField;
import com.abo47.kubejslab.recipe.model.LabRecipeFieldValues;
import com.abo47.kubejslab.recipe.model.LabRecipeOutput;
import com.abo47.kubejslab.recipe.model.LabSlotDescriptor;
import com.abo47.kubejslab.recipe.model.LabSlotKind;

import blusunrize.immersiveengineering.api.crafting.MixerRecipe;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


public class MixerMachine extends ImmersiveEngineeringMachine {
    public MixerMachine() {
        super("mixer", LabRecipeField.ENERGY, LabRecipeField.FLUID_INPUT_AMOUNT,
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
                new LabSlotDescriptor(true, LabSlotKind.ITEM, 0, 0, true),
                new LabSlotDescriptor(true, LabSlotKind.ITEM, 1, 0, true),
                new LabSlotDescriptor(true, LabSlotKind.ITEM, 2, 0, true),
                new LabSlotDescriptor(true, LabSlotKind.ITEM, 0, 1, true),
                new LabSlotDescriptor(true, LabSlotKind.ITEM, 1, 1, true),
                new LabSlotDescriptor(true, LabSlotKind.ITEM, 2, 1, true),
                new LabSlotDescriptor(true, LabSlotKind.FLUID, 1, 2, false));
    }

    @Override
    public List<LabSlotDescriptor> outputSlots() {
        return List.of(new LabSlotDescriptor(false, LabSlotKind.FLUID, 3, 1, false));
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
        json.add("inputs", itemInputs);
        if (fluidInput != null) {
            json.add("fluid", fluidInput);
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
        if (original instanceof MixerRecipe mixer) {
            int fluidIn = 0;
            if (mixer.fluidInput != null) {
                fluidIn = mixer.fluidInput.getAmount();
            }
            return new LabRecipeFieldValues(current.shapeless(), current.experience(), current.cookingTime(),
                    current.count(), current.processingTime(), current.heatRequirement(), current.keepHeldItem(),
                    current.acceptMirrored(), current.gridWidth(), current.gridHeight(),
                    mixer.getTotalProcessEnergy(), current.creosoteAmount(), current.mold(), current.blueprintCategory(),
                    current.clocheRenderType(), current.clocheRenderBlock(), fluidIn, 0);
        }
        return current;
    }
}