package com.abo47.kubejslab.recipe.immersiveengineering;

import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

import com.abo47.kubejslab.recipe.model.RecipeField;
import com.abo47.kubejslab.recipe.model.RecipeFieldValues;
import com.abo47.kubejslab.recipe.model.RecipeIngredient;
import com.abo47.kubejslab.recipe.model.RecipeOutput;
import com.abo47.kubejslab.recipe.model.SlotDescriptor;
import com.abo47.kubejslab.recipe.model.SlotKind;
import com.abo47.kubejslab.recipe.model.SlotTint;

import blusunrize.immersiveengineering.api.crafting.MetalPressRecipe;
import com.google.gson.JsonObject;


public class MetalPressMachine extends ImmersiveEngineeringMachine {
    public MetalPressMachine() {
        super("metal_press", RecipeField.ENERGY, RecipeField.MOLD);
    }

    @Override
    public List<SlotDescriptor> inputSlots() {
        return List.of(
                new SlotDescriptor(true, SlotKind.ITEM, 0, 0, false),
                new SlotDescriptor(true, SlotKind.ITEM, 1, 0, false, SlotTint.MOLD));
    }

    @Override
    public List<SlotDescriptor> outputSlots() {
        return List.of(new SlotDescriptor(false, SlotKind.ITEM, 2, 0, false));
    }

    @Override
    public JsonObject buildJson(String type, List<RecipeIngredient> inputs, List<RecipeOutput> outputs,
            RecipeFieldValues values) {
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
    public RecipeFieldValues prefill(RecipeFieldValues current, Recipe<?> original) {
        if (original instanceof MetalPressRecipe press) {
            ResourceLocation moldKey = BuiltInRegistries.ITEM.getKey(press.mold);
            return new RecipeFieldValues(current.shapeless(), current.experience(), current.cookingTime(),
                    current.count(), current.processingTime(), current.heatRequirement(), current.keepHeldItem(),
                    current.acceptMirrored(), current.gridWidth(), current.gridHeight(),
                    press.getTotalProcessEnergy(), current.creosoteAmount(), moldKey == null ? "" : moldKey.toString(),
                    current.blueprintCategory(), current.clocheRenderType(), current.clocheRenderBlock());
        }
        return current;
    }
}