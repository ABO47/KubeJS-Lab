package com.abo47.kubejslab.recipe.immersiveengineering;

import java.util.List;

import net.minecraft.world.item.crafting.Recipe;

import com.abo47.kubejslab.recipe.model.RecipeField;
import com.abo47.kubejslab.recipe.model.RecipeFieldValues;
import com.abo47.kubejslab.recipe.model.RecipeIngredient;
import com.abo47.kubejslab.recipe.model.RecipeOutput;
import com.abo47.kubejslab.recipe.model.SlotDescriptor;
import com.abo47.kubejslab.recipe.model.SlotKind;

import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import com.google.gson.JsonObject;


public class FermenterSqueezerMachine extends ImmersiveEngineeringMachine {
    public FermenterSqueezerMachine(String type) {
        super(type, RecipeField.ENERGY, RecipeField.FLUID_OUTPUT_AMOUNT);
    }

    @Override
    public boolean supportsFluidOutputAmount() {
        return true;
    }

    @Override
    public List<SlotDescriptor> inputSlots() {
        return List.of(new SlotDescriptor(true, SlotKind.ITEM, 0, 0, false));
    }

    @Override
    public List<SlotDescriptor> outputSlots() {
        return List.of(
                new SlotDescriptor(false, SlotKind.ITEM, 1, 0, true),
                new SlotDescriptor(false, SlotKind.FLUID, 1, 1, true));
    }

    @Override
    public JsonObject buildJson(String type, List<RecipeIngredient> inputs, List<RecipeOutput> outputs,
            RecipeFieldValues values) {
        JsonObject json = new JsonObject();
        json.addProperty("type", type);
        if (!inputs.isEmpty()) {
            json.add("input", ingredientWithSize(inputs.get(0)));
        }
        for (RecipeOutput output : outputs) {
            if (output instanceof RecipeOutput.Fluid) {
                json.add("fluid", outputWithAmount(output, values.fluidOutputAmount()));
            } else {
                json.add("result", readOutput(output));
            }
        }
        json.addProperty("energy", Math.max(0, values.energy()));
        return json;
    }

    @Override
    public RecipeFieldValues prefill(RecipeFieldValues current, Recipe<?> original) {
        if (original instanceof MultiblockRecipe multiblock) {
            return new RecipeFieldValues(current.shapeless(), current.experience(), current.cookingTime(),
                    current.count(), current.processingTime(), current.heatRequirement(), current.keepHeldItem(),
                    current.acceptMirrored(), current.gridWidth(), current.gridHeight(),
                    multiblock.getTotalProcessEnergy(), current.creosoteAmount(), current.mold(),
                    current.blueprintCategory(), current.clocheRenderType(), current.clocheRenderBlock(),
                    current.fluidInputAmount(), current.fluidOutputAmount());
        }
        return current;
    }
}