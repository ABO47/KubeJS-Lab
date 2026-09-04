package com.abo47.kubejslab.recipe.immersiveengineering;

import java.util.List;

import net.minecraft.world.item.crafting.Recipe;

import com.abo47.kubejslab.recipe.model.RecipeField;
import com.abo47.kubejslab.recipe.model.RecipeFieldValues;
import com.abo47.kubejslab.recipe.model.RecipeIngredient;
import com.abo47.kubejslab.recipe.model.RecipeOutput;
import com.abo47.kubejslab.recipe.model.SlotDescriptor;
import com.abo47.kubejslab.recipe.model.SlotKind;

import blusunrize.immersiveengineering.api.crafting.MixerRecipe;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


public class MixerMachine extends ImmersiveEngineeringMachine {
    public MixerMachine() {
        super("mixer", RecipeField.ENERGY, RecipeField.FLUID_INPUT_AMOUNT,
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
                new SlotDescriptor(true, SlotKind.ITEM, 0, 0, true),
                new SlotDescriptor(true, SlotKind.ITEM, 1, 0, true),
                new SlotDescriptor(true, SlotKind.ITEM, 2, 0, true),
                new SlotDescriptor(true, SlotKind.ITEM, 0, 1, true),
                new SlotDescriptor(true, SlotKind.ITEM, 1, 1, true),
                new SlotDescriptor(true, SlotKind.ITEM, 2, 1, true),
                new SlotDescriptor(true, SlotKind.FLUID, 2, 2, false));
    }

    @Override
    public List<SlotDescriptor> outputSlots() {
        return List.of(new SlotDescriptor(false, SlotKind.FLUID, 3, 1, false));
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
        json.add("inputs", itemInputs);
        if (fluidInput != null) {
            json.add("fluid", fluidInput);
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
        if (original instanceof MixerRecipe mixer) {
            int fluidIn = 0;
            if (mixer.fluidInput != null) {
                fluidIn = mixer.fluidInput.getAmount();
            }
            return new RecipeFieldValues(current.shapeless(), current.experience(), current.cookingTime(),
                    current.count(), current.processingTime(), current.heatRequirement(), current.keepHeldItem(),
                    current.acceptMirrored(), current.gridWidth(), current.gridHeight(),
                    mixer.getTotalProcessEnergy(), current.creosoteAmount(), current.mold(), current.blueprintCategory(),
                    current.clocheRenderType(), current.clocheRenderBlock(), fluidIn, 0);
        }
        return current;
    }
}