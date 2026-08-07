package com.abo47.kubejslab.recipe.immersiveengineering;

import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

import com.abo47.kubejslab.recipe.model.LabIngredient;
import com.abo47.kubejslab.recipe.model.LabRecipeField;
import com.abo47.kubejslab.recipe.model.LabRecipeFieldValues;
import com.abo47.kubejslab.recipe.model.LabRecipeOutput;

import blusunrize.immersiveengineering.api.crafting.MetalPressRecipe;
import com.google.gson.JsonObject;


public class MetalPressMachine extends ImmersiveEngineeringMachine {
    public MetalPressMachine() {
        super("metal_press", LabRecipeField.ENERGY, LabRecipeField.MOLD);
    }

    @Override
    public JsonObject buildJson(String type, List<LabIngredient> inputs, List<LabRecipeOutput> outputs,
            LabRecipeFieldValues values) {
        JsonObject json = new JsonObject();
        json.addProperty("type", type);
        if (!inputs.isEmpty()) {
            json.add("input", ingredientWithSize(inputs.get(0)));
        }
        json.addProperty("mold", values.mold());
        if (!outputs.isEmpty()) {
            json.add("result", readOutput(outputs.get(0)));
        }
        json.addProperty("energy", Math.max(0, values.energy()));
        return json;
    }

    @Override
    public LabRecipeFieldValues prefill(LabRecipeFieldValues current, Recipe<?> original) {
        if (original instanceof MetalPressRecipe press) {
            ResourceLocation moldKey = BuiltInRegistries.ITEM.getKey(press.mold);
            return new LabRecipeFieldValues(current.shapeless(), current.experience(), current.cookingTime(),
                    current.count(), current.processingTime(), current.heatRequirement(), current.keepHeldItem(),
                    current.acceptMirrored(), current.gridWidth(), current.gridHeight(),
                    press.getTotalProcessEnergy(), current.creosoteAmount(), moldKey == null ? "" : moldKey.toString(),
                    current.blueprintCategory(), current.clocheRenderType(), current.clocheRenderBlock());
        }
        return current;
    }
}