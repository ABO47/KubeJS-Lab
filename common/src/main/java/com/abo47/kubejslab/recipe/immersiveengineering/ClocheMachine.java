package com.abo47.kubejslab.recipe.immersiveengineering;

import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

import com.abo47.kubejslab.recipe.model.RecipeField;
import com.abo47.kubejslab.recipe.model.RecipeFieldValues;
import com.abo47.kubejslab.recipe.model.RecipeIngredient;
import com.abo47.kubejslab.recipe.model.RecipeJson;
import com.abo47.kubejslab.recipe.model.RecipeOutput;
import com.abo47.kubejslab.recipe.model.SlotDescriptor;
import com.abo47.kubejslab.recipe.model.SlotKind;

import blusunrize.immersiveengineering.api.crafting.ClocheRecipe;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


public class ClocheMachine extends ImmersiveEngineeringMachine {
    public ClocheMachine() {
        super("cloche", RecipeField.PROCESSING_TIME, RecipeField.CLOCHE_RENDER_TYPE,
                RecipeField.CLOCHE_RENDER_BLOCK);
    }

    @Override
    public List<SlotDescriptor> inputSlots() {
        return List.of(
                new SlotDescriptor(true, SlotKind.ITEM, 0, 0, false),
                new SlotDescriptor(true, SlotKind.ITEM, 0, 1, false));
    }

    @Override
    public List<SlotDescriptor> outputSlots() {
        return List.of(
                new SlotDescriptor(false, SlotKind.ITEM, 2, 0, true),
                new SlotDescriptor(false, SlotKind.ITEM, 3, 0, true),
                new SlotDescriptor(false, SlotKind.ITEM, 2, 1, true),
                new SlotDescriptor(false, SlotKind.ITEM, 3, 1, true));
    }

    @Override
    public JsonObject buildJson(String type, List<RecipeIngredient> inputs, List<RecipeOutput> outputs,
            RecipeFieldValues values) {
        JsonObject json = new JsonObject();
        json.addProperty("type", type);
        if (inputs.size() >= 2) {
            json.add("input", RecipeJson.ingredientJson(inputs.get(0)));
            json.add("soil", RecipeJson.ingredientJson(inputs.get(1)));
        }
        JsonArray results = new JsonArray();
        for (RecipeOutput output : outputs) {
            results.add(readOutput(output));
        }
        json.add("results", results);
        json.addProperty("time", values.processingTime());
        JsonObject render = new JsonObject();
        render.addProperty("type", values.clocheRenderType());
        render.addProperty("block", values.clocheRenderBlock());
        json.add("render", render);
        return json;
    }

    @Override
    public RecipeFieldValues prefill(RecipeFieldValues current, Recipe<?> original) {
        if (original instanceof ClocheRecipe cloche) {
            ResourceLocation blockKey = BuiltInRegistries.BLOCK.getKey(cloche.renderReference.getBlock());
            return new RecipeFieldValues(current.shapeless(), current.experience(), current.cookingTime(),
                    current.count(), cloche.time, current.heatRequirement(), current.keepHeldItem(),
                    current.acceptMirrored(), current.gridWidth(), current.gridHeight(),
                    current.energy(), current.creosoteAmount(), current.mold(), current.blueprintCategory(),
                    cloche.renderReference.getType(),
                    blockKey == null ? "" : blockKey.toString());
        }
        return current;
    }
}