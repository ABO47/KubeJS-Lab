package com.abo47.kubejslab.recipe.immersiveengineering;

import java.util.List;

import net.minecraft.world.item.crafting.Recipe;

import com.abo47.kubejslab.recipe.model.LabIngredient;
import com.abo47.kubejslab.recipe.model.LabRecipeField;
import com.abo47.kubejslab.recipe.model.LabRecipeFieldValues;
import com.abo47.kubejslab.recipe.model.LabRecipeOutput;
import com.abo47.kubejslab.recipe.model.LabSlotDescriptor;
import com.abo47.kubejslab.recipe.model.LabSlotKind;

import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import com.google.gson.JsonObject;


public class FermenterSqueezerMachine extends ImmersiveEngineeringMachine {
    public FermenterSqueezerMachine(String type) {
        super(type, LabRecipeField.ENERGY, LabRecipeField.FLUID_OUTPUT_AMOUNT);
    }

    @Override
    public boolean supportsFluidOutputAmount() {
        return true;
    }

    @Override
    public List<LabSlotDescriptor> inputSlots() {
        return List.of(new LabSlotDescriptor(true, LabSlotKind.ITEM, 0, 0, false));
    }

    @Override
    public List<LabSlotDescriptor> outputSlots() {
        return List.of(
                new LabSlotDescriptor(false, LabSlotKind.ITEM, 1, 0, true),
                new LabSlotDescriptor(false, LabSlotKind.FLUID, 1, 1, true));
    }

    @Override
    public JsonObject buildJson(String type, List<LabIngredient> inputs, List<LabRecipeOutput> outputs,
            LabRecipeFieldValues values) {
        JsonObject json = new JsonObject();
        json.addProperty("type", type);
        if (!inputs.isEmpty()) {
            json.add("input", ingredientWithSize(inputs.get(0)));
        }
        for (LabRecipeOutput output : outputs) {
            if (output instanceof LabRecipeOutput.Fluid) {
                json.add("fluid", outputWithAmount(output, values.fluidOutputAmount()));
            } else {
                json.add("result", readOutput(output));
            }
        }
        json.addProperty("energy", Math.max(0, values.energy()));
        return json;
    }

    @Override
    public LabRecipeFieldValues prefill(LabRecipeFieldValues current, Recipe<?> original) {
        if (original instanceof MultiblockRecipe multiblock) {
            return new LabRecipeFieldValues(current.shapeless(), current.experience(), current.cookingTime(),
                    current.count(), current.processingTime(), current.heatRequirement(), current.keepHeldItem(),
                    current.acceptMirrored(), current.gridWidth(), current.gridHeight(),
                    multiblock.getTotalProcessEnergy(), current.creosoteAmount(), current.mold(),
                    current.blueprintCategory(), current.clocheRenderType(), current.clocheRenderBlock(),
                    current.fluidInputAmount(), current.fluidOutputAmount());
        }
        return current;
    }
}