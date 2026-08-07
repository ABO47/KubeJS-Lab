package com.abo47.kubejslab.recipe.immersiveengineering;

import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

import com.abo47.kubejslab.recipe.model.ClocheRenderType;
import com.abo47.kubejslab.recipe.model.LabIngredient;
import com.abo47.kubejslab.recipe.model.LabRecipeField;
import com.abo47.kubejslab.recipe.model.LabRecipeFieldValues;
import com.abo47.kubejslab.recipe.model.LabRecipeJson;
import com.abo47.kubejslab.recipe.model.LabRecipeOutput;
import com.abo47.kubejslab.recipe.model.LabSlotDescriptor;
import com.abo47.kubejslab.recipe.model.LabSlotKind;

import blusunrize.immersiveengineering.api.crafting.ClocheRecipe;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


public class ClocheMachine extends ImmersiveEngineeringMachine {
    public ClocheMachine() {
        super("cloche", LabRecipeField.PROCESSING_TIME, LabRecipeField.CLOCHE_RENDER_TYPE,
                LabRecipeField.CLOCHE_RENDER_BLOCK);
    }

    @Override
    public List<LabSlotDescriptor> inputSlots() {
        return List.of(
                new LabSlotDescriptor(true, LabSlotKind.ITEM, 0, 0, false),
                new LabSlotDescriptor(true, LabSlotKind.ITEM, 1, 0, false));
    }

    @Override
    public List<LabSlotDescriptor> outputSlots() {
        return List.of(
                new LabSlotDescriptor(false, LabSlotKind.ITEM, 2, 0, true),
                new LabSlotDescriptor(false, LabSlotKind.ITEM, 3, 0, true),
                new LabSlotDescriptor(false, LabSlotKind.ITEM, 2, 1, true),
                new LabSlotDescriptor(false, LabSlotKind.ITEM, 3, 1, true));
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
            case HEMP -> "hemp";
            case CHORUS -> "chorus";
        };
    }

    @Override
    public LabRecipeFieldValues prefill(LabRecipeFieldValues current, Recipe<?> original) {
        if (original instanceof ClocheRecipe cloche) {
            ResourceLocation blockKey = BuiltInRegistries.BLOCK.getKey(cloche.renderReference.getBlock());
            return new LabRecipeFieldValues(current.shapeless(), current.experience(), current.cookingTime(),
                    current.count(), cloche.time, current.heatRequirement(), current.keepHeldItem(),
                    current.acceptMirrored(), current.gridWidth(), current.gridHeight(),
                    current.energy(), current.creosoteAmount(), current.mold(), current.blueprintCategory(),
                    ClocheRenderType.byName(cloche.renderReference.getType()),
                    blockKey == null ? "" : blockKey.toString());
        }
        return current;
    }
}