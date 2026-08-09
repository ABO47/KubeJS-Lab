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

import blusunrize.immersiveengineering.api.crafting.SawmillRecipe;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


public class SawmillMachine extends ImmersiveEngineeringMachine {
    public SawmillMachine() {
        super("sawmill", LabRecipeField.ENERGY);
    }

    @Override
    public List<LabSlotDescriptor> inputSlots() {
        return List.of(new LabSlotDescriptor(true, LabSlotKind.ITEM, 0, 0, false));
    }

    @Override
    public List<LabSlotDescriptor> outputSlots() {
        return List.of(
                new LabSlotDescriptor(false, LabSlotKind.ITEM, 0, 0, false),
                new LabSlotDescriptor(false, LabSlotKind.ITEM, 1, 0, true),
                new LabSlotDescriptor(false, LabSlotKind.ITEM, 0, 1, true));
    }

    @Override
    public boolean supportsChance() {
        return false;
    }

    @Override
    public JsonObject buildJson(String type, List<LabIngredient> inputs, List<LabRecipeOutput> outputs,
            LabRecipeFieldValues values) {
        JsonObject json = new JsonObject();
        json.addProperty("type", type);
        if (!inputs.isEmpty()) {
            json.add("input", LabRecipeJson.ingredientJson(inputs.get(0)));
        }
        if (!outputs.isEmpty()) {
            json.add("result", readOutput(outputs.get(0)));
        }
        if (outputs.size() > 1) {
            json.add("stripped", readOutput(outputs.get(1)));
        }
        JsonArray secondaries = new JsonArray();
        for (int i = 2; i < outputs.size(); i++) {
            JsonObject secondary = new JsonObject();
            secondary.add("output", readOutput(outputs.get(i)));
            secondary.addProperty("stripping", false);
            secondaries.add(secondary);
        }
        json.add("secondaries", secondaries);
        json.addProperty("energy", Math.max(0, values.energy()));
        return json;
    }

    @Override
    public LabRecipeFieldValues prefill(LabRecipeFieldValues current, Recipe<?> original) {
        if (original instanceof SawmillRecipe sawmill) {
            return new LabRecipeFieldValues(current.shapeless(), current.experience(), current.cookingTime(),
                    current.count(), current.processingTime(), current.heatRequirement(), current.keepHeldItem(),
                    current.acceptMirrored(), current.gridWidth(), current.gridHeight(),
                    sawmill.getTotalProcessEnergy(), current.creosoteAmount(), current.mold(),
                    current.blueprintCategory(), current.clocheRenderType(), current.clocheRenderBlock());
        }
        return current;
    }
}