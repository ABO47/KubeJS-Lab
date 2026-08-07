package com.abo47.kubejslab.recipe.immersiveengineering;

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import blusunrize.immersiveengineering.api.crafting.ClocheRecipe;

import com.abo47.kubejslab.recipe.model.ClocheRenderType;
import com.abo47.kubejslab.recipe.model.LabIngredient;
import com.abo47.kubejslab.recipe.model.LabRecipeField;
import com.abo47.kubejslab.recipe.model.LabRecipeFieldValues;
import com.abo47.kubejslab.recipe.model.LabRecipeJson;
import com.abo47.kubejslab.recipe.model.LabRecipeOutput;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

public class ClocheMachine extends ImmersiveEngineeringMachine {
    public ClocheMachine() {
        super("cloche", LabRecipeField.PROCESSING_TIME, LabRecipeField.CLOCHE_RENDER_TYPE,
                LabRecipeField.CLOCHE_RENDER_BLOCK);
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
        if (inputs.size() >= 2) {
            json.add("input", LabRecipeJson.ingredientJson(inputs.get(0)));
            json.add("soil", LabRecipeJson.ingredientJson(inputs.get(1)));
        }
        JsonArray results = new JsonArray();
        for (LabRecipeOutput output : outputs) {
            results.add(readOutput(output));
        }
        json.add("results", results);
        json.addProperty("time", values.processingTime());
        JsonObject render = new JsonObject();
        render.addProperty("type", renderTypeName(values.clocheRenderType()));
        render.addProperty("block", values.clocheRenderBlock());
        json.add("render", render);
        return json;
    }

    private static String renderTypeName(ClocheRenderType type) {
        return switch (type) {
            case CROP -> "crop";
            case STACKING -> "stacking";
            case STEM -> "stem";
            case GENERIC -> "generic";
        };
    }

    @Override
    public LabRecipeFieldValues prefill(LabRecipeFieldValues current, Recipe<?> original) {
        if (original instanceof ClocheRecipe cloche) {
            ResourceLocation blockKey = BuiltInRegistries.BLOCK.getKey(cloche.renderReference.getBlock());
            return new LabRecipeFieldValues(current.shapeless(), current.experience(), current.cookingTime(),
                    current.count(), cloche.time, current.heatRequirement(), current.keepHeldItem(),
                    current.acceptMirrored(), current.gridWidth(), current.gridHeight(), current.outputCount(),
                    current.energy(), current.creosoteAmount(), current.mold(), current.blueprintCategory(),
                    ClocheRenderType.byName(cloche.renderReference.getType()),
                    blockKey == null ? "" : blockKey.toString());
        }
        return current;
    }
}