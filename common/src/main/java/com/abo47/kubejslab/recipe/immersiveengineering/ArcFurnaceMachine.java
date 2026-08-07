package com.abo47.kubejslab.recipe.immersiveengineering;

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import blusunrize.immersiveengineering.api.crafting.ArcFurnaceRecipe;
import blusunrize.immersiveengineering.api.crafting.StackWithChance;

import com.abo47.kubejslab.recipe.model.LabIngredient;
import com.abo47.kubejslab.recipe.model.LabRecipeField;
import com.abo47.kubejslab.recipe.model.LabRecipeFieldValues;
import com.abo47.kubejslab.recipe.model.LabRecipeOutput;

import net.minecraft.world.item.crafting.Recipe;

public class ArcFurnaceMachine extends ImmersiveEngineeringMachine {
    public ArcFurnaceMachine() {
        super("arc_furnace", LabRecipeField.PROCESSING_TIME, LabRecipeField.ENERGY);
    }

    @Override
    public boolean supportsChance() {
        return true;
    }

    @Override
    public boolean supportsOutputCount() {
        return true;
    }

    @Override
    public JsonObject buildJson(String type, List<LabIngredient> inputs, List<LabRecipeOutput> outputs,
            LabRecipeFieldValues values) {
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
        for (LabRecipeOutput output : outputs) {
            if (output instanceof LabRecipeOutput.Item item && item.chance() < 1.0f) {
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
        json.addProperty("time", values.processingTime());
        json.addProperty("energy", Math.max(0, values.energy()));
        return json;
    }

    @Override
    public List<Float> outputChances(Recipe<?> original) {
        if (original instanceof ArcFurnaceRecipe arc) {
            List<Float> chances = new java.util.ArrayList<>();
            chances.add(1.0f);
            for (StackWithChance secondary : arc.secondaryOutputs) {
                chances.add(secondary.chance());
            }
            return chances;
        }
        return List.of();
    }

    @Override
    public LabRecipeFieldValues prefill(LabRecipeFieldValues current, Recipe<?> original) {
        if (original instanceof ArcFurnaceRecipe arc) {
            return new LabRecipeFieldValues(current.shapeless(), current.experience(), current.cookingTime(),
                    current.count(), arc.getTotalProcessTime(), current.heatRequirement(), current.keepHeldItem(),
                    current.acceptMirrored(), current.gridWidth(), current.gridHeight(), current.outputCount(),
                    arc.getTotalProcessEnergy(), current.creosoteAmount(), current.mold(), current.blueprintCategory(),
                    current.clocheRenderType(), current.clocheRenderBlock());
        }
        return current;
    }
}