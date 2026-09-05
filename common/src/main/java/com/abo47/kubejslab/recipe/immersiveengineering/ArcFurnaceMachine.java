package com.abo47.kubejslab.recipe.immersiveengineering;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.item.crafting.Recipe;

import com.abo47.kubejslab.recipe.model.RecipeField;
import com.abo47.kubejslab.recipe.model.RecipeFieldValues;
import com.abo47.kubejslab.recipe.model.RecipeIngredient;
import com.abo47.kubejslab.recipe.model.RecipeOutput;
import com.abo47.kubejslab.recipe.model.SlotDescriptor;
import com.abo47.kubejslab.recipe.model.SlotKind;
import com.abo47.kubejslab.recipe.model.SlotTint;

import blusunrize.immersiveengineering.api.crafting.ArcFurnaceRecipe;
import blusunrize.immersiveengineering.api.crafting.StackWithChance;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


public class ArcFurnaceMachine extends ImmersiveEngineeringMachine {
    public ArcFurnaceMachine() {
        super("arc_furnace", RecipeField.PROCESSING_TIME, RecipeField.ENERGY);
    }

    @Override
    public boolean supportsChance() {
        return true;
    }

    @Override
    public List<SlotDescriptor> inputSlots() {
        return List.of(
                new SlotDescriptor(true, SlotKind.ITEM, 0, 0, false),
                new SlotDescriptor(true, SlotKind.ITEM, 0, 2, true, SlotTint.ADDITIVE),
                new SlotDescriptor(true, SlotKind.ITEM, 1, 2, true, SlotTint.ADDITIVE),
                new SlotDescriptor(true, SlotKind.ITEM, 2, 2, true, SlotTint.ADDITIVE),
                new SlotDescriptor(true, SlotKind.ITEM, 3, 2, true, SlotTint.ADDITIVE));
    }

    @Override
    public List<SlotDescriptor> outputSlots() {
        return List.of(
                new SlotDescriptor(false, SlotKind.ITEM, 6, 0, true),
                new SlotDescriptor(false, SlotKind.ITEM, 7, 0, true),
                new SlotDescriptor(false, SlotKind.ITEM, 6, 1, true),
                new SlotDescriptor(false, SlotKind.ITEM, 7, 1, true),
                new SlotDescriptor(false, SlotKind.ITEM, 6, 2, true),
                new SlotDescriptor(false, SlotKind.ITEM, 7, 2, true),
                new SlotDescriptor(false, SlotKind.ITEM, 8, 2, false));
    }

    @Override
    public JsonObject buildJson(String type, List<RecipeIngredient> inputs, List<RecipeOutput> outputs,
            RecipeFieldValues values) {
        JsonObject json = new JsonObject();
        json.addProperty("type", type);
        if (!inputs.isEmpty()) {
            json.add("input", ingredientWithSize(inputs.get(0)));
        }
        JsonArray additives = new JsonArray();
        for (int i = 1; i < inputs.size(); i++) {
            additives.add(ingredientWithSize(inputs.get(i)));
        }
        json.add("additives", additives);
        JsonArray results = new JsonArray();
        JsonArray secondaries = new JsonArray();
        int sliceEnd = Math.min(outputs.size(), 6);
        for (int i = 0; i < sliceEnd; i++) {
            RecipeOutput output = outputs.get(i);
            if (output instanceof RecipeOutput.Item item && item.chance() < 1.0f) {
                JsonObject secondary = new JsonObject();
                secondary.add("output", readOutput(item));
                secondary.addProperty("chance", item.chance());
                secondaries.add(secondary);
            } else {
                results.add(readOutput(output));
            }
        }
        json.add("results", results);
        json.add("secondaries", secondaries);
        if (outputs.size() > 6) {
            json.add("slag", readOutput(outputs.get(6)));
        }
        json.addProperty("time", values.processingTime());
        json.addProperty("energy", Math.max(0, values.energy()));
        return json;
    }

    @Override
    public List<Float> outputChances(Recipe<?> original) {
        if (original instanceof ArcFurnaceRecipe arc) {
            List<Float> chances = new ArrayList<>();
            chances.add(1.0f);
            for (StackWithChance secondary : arc.secondaryOutputs) {
                chances.add(secondary.chance());
            }
            return chances;
        }
        return List.of();
    }

    @Override
    public RecipeFieldValues prefill(RecipeFieldValues current, Recipe<?> original) {
        if (original instanceof ArcFurnaceRecipe arc) {
            return new RecipeFieldValues(current.shapeless(), current.experience(), current.cookingTime(),
                    current.count(), arc.getTotalProcessTime(), current.heatRequirement(), current.keepHeldItem(),
                    current.acceptMirrored(), current.gridWidth(), current.gridHeight(),
                    arc.getTotalProcessEnergy(), current.creosoteAmount(), current.mold(), current.blueprintCategory(),
                    current.clocheRenderType(), current.clocheRenderBlock());
        }
        return current;
    }
}