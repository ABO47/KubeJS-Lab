package com.abo47.kubejslab.recipe.immersiveengineering;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.item.crafting.Recipe;

import com.abo47.kubejslab.recipe.model.RecipeField;
import com.abo47.kubejslab.recipe.model.RecipeFieldValues;
import com.abo47.kubejslab.recipe.model.RecipeIngredient;
import com.abo47.kubejslab.recipe.model.RecipeJson;
import com.abo47.kubejslab.recipe.model.RecipeOutput;
import com.abo47.kubejslab.recipe.model.SlotDescriptor;
import com.abo47.kubejslab.recipe.model.SlotKind;

import blusunrize.immersiveengineering.api.crafting.CrusherRecipe;
import blusunrize.immersiveengineering.api.crafting.StackWithChance;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


public class CrusherMachine extends ImmersiveEngineeringMachine {
    public CrusherMachine() {
        super("crusher", RecipeField.ENERGY);
    }

    @Override
    public boolean supportsChance() {
        return true;
    }

    @Override
    public List<SlotDescriptor> inputSlots() {
        return new ArrayList<>(List.of(new SlotDescriptor(true, SlotKind.ITEM, 0, 0, false)));
    }

    @Override
    public List<SlotDescriptor> outputSlots() {
        List<SlotDescriptor> slots = new ArrayList<>();
        slots.add(new SlotDescriptor(false, SlotKind.ITEM, 1, 0, false));
        slots.add(new SlotDescriptor(false, SlotKind.ITEM, 1, 1, true));
        slots.add(new SlotDescriptor(false, SlotKind.ITEM, 2, 1, true));
        return slots;
    }

    @Override
    public JsonObject buildJson(String type, List<RecipeIngredient> inputs, List<RecipeOutput> outputs,
            RecipeFieldValues values) {
        JsonObject json = new JsonObject();
        json.addProperty("type", type);
        if (!inputs.isEmpty()) {
            json.add("input", RecipeJson.ingredientJson(inputs.get(0)));
        }
        if (!outputs.isEmpty()) {
            json.add("result", readOutput(outputs.get(0)));
        }
        JsonArray secondaries = new JsonArray();
        for (int i = 1; i < outputs.size(); i++) {
            JsonObject secondary = new JsonObject();
            secondary.add("output", readOutput(outputs.get(i)));
            secondary.addProperty("chance", outputs.get(i) instanceof RecipeOutput.Item item
                    ? item.chance() : 1.0f);
            secondaries.add(secondary);
        }
        json.add("secondaries", secondaries);
        json.addProperty("energy", Math.max(0, values.energy()));
        return json;
    }

    @Override
    public List<Float> outputChances(Recipe<?> original) {
        if (original instanceof CrusherRecipe crusher) {
            List<Float> chances = new ArrayList<>();
            chances.add(1.0f);
            for (StackWithChance secondary : crusher.secondaryOutputs) {
                chances.add(secondary.chance());
            }
            return chances;
        }
        return List.of();
    }

    @Override
    public RecipeFieldValues prefill(RecipeFieldValues current, Recipe<?> original) {
        if (original instanceof CrusherRecipe crusher) {
            return new RecipeFieldValues(current.shapeless(), current.experience(), current.cookingTime(),
                    current.count(), current.processingTime(), current.heatRequirement(), current.keepHeldItem(),
                    current.acceptMirrored(), current.gridWidth(), current.gridHeight(),
                    crusher.getTotalProcessEnergy(), current.creosoteAmount(), current.mold(),
                    current.blueprintCategory(), current.clocheRenderType(), current.clocheRenderBlock());
        }
        return current;
    }
}