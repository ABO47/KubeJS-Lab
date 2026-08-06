package com.abo47.kubejslab.recipe.create;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;

import com.simibubi.create.content.kinetics.crafter.MechanicalCraftingRecipe;

import com.abo47.kubejslab.KubeJSLab;
import com.abo47.kubejslab.recipe.LabRecipeMachine;
import com.abo47.kubejslab.recipe.model.LabIngredient;
import com.abo47.kubejslab.recipe.model.LabRecipeField;
import com.abo47.kubejslab.recipe.model.LabRecipeFieldValues;
import com.abo47.kubejslab.recipe.model.LabRecipeJson;
import com.abo47.kubejslab.recipe.model.LabRecipeOutput;

public final class MechanicalCraftingMachine implements LabRecipeMachine {
    @Override
    public ResourceLocation jeiUid() {
        return new ResourceLocation("create", "mechanical_crafting");
    }

    @Override
    public String jsonType() {
        return "create:mechanical_crafting";
    }

    @Override
    public boolean gridLayout() {
        return true;
    }

    @Override
    public List<LabRecipeField> fields() {
        return List.of(LabRecipeField.GRID_WIDTH, LabRecipeField.GRID_HEIGHT, LabRecipeField.ACCEPT_MIRRORED);
    }

    @Override
    public JsonObject buildJson(String jsonType, List<LabIngredient> inputs, List<LabRecipeOutput> outputs,
            LabRecipeFieldValues values) {
        JsonObject json = new JsonObject();
        json.addProperty("type", jsonType);
        int width = Math.max(1, values.gridWidth());
        int height = Math.max(1, values.gridHeight());
        KubeJSLab.LOGGER.info("[MechCrafting] buildJson: {}x{} grid, {} inputs, {} outputs, acceptMirrored={}, jsonType={}",
                width, height, inputs.size(), outputs.size(), values.acceptMirrored(), jsonType);
        JsonArray pattern = new JsonArray();
        JsonObject key = new JsonObject();
        Map<String, Character> charByKey = new LinkedHashMap<>();
        char nextChar = 'A';
        boolean anyFilled = false;
        for (int row = 0; row < height; row++) {
            StringBuilder rowStr = new StringBuilder();
            for (int col = 0; col < width; col++) {
                int index = row * width + col;
                LabIngredient ingredient = index < inputs.size() ? inputs.get(index)
                        : new LabIngredient.Item(net.minecraft.world.item.ItemStack.EMPTY);
                if (ingredient.isEmpty()) {
                    rowStr.append(' ');
                    continue;
                }
                anyFilled = true;
                if (ingredient instanceof LabIngredient.Fluid) {
                    throw new IllegalArgumentException("Mechanical crafting only accepts item or tag inputs");
                }
                String entryKey = ingredientKey(ingredient);
                Character c = charByKey.get(entryKey);
                if (c == null) {
                    c = nextChar++;
                    charByKey.put(entryKey, c);
                }
                rowStr.append((char) c);
                key.add(String.valueOf((char) c), LabRecipeJson.ingredientJson(ingredient));
            }
            pattern.add(rowStr.toString());
        }
        KubeJSLab.LOGGER.info("[MechCrafting] pattern={}, key={}", pattern, key);
        if (!anyFilled) {
            throw new IllegalArgumentException("Mechanical crafting recipe needs at least one filled cell");
        }
        json.add("pattern", pattern);
        json.add("key", key);
        json.add("result", LabRecipeJson.itemWithCount(LabRecipeOutput.firstItem(outputs)));
        if (!values.acceptMirrored()) {
            json.addProperty("acceptMirrored", false);
        }
        KubeJSLab.LOGGER.info("[MechCrafting] final json: {}", json);
        return json;
    }

    private static String ingredientKey(LabIngredient ingredient) {
        if (ingredient instanceof LabIngredient.Item item) {
            return "i:" + item.stack().getItem().builtInRegistryHolder().key().location()
                    + (item.stack().hasTag() ? "|" + item.stack().getTag() : "");
        }
        if (ingredient instanceof LabIngredient.Tag tag) {
            return "t:" + tag.tag();
        }
        return "f:" + ingredient;
    }

    @Override
    public LabRecipeFieldValues prefill(LabRecipeFieldValues current, Recipe<?> original) {
        if (original instanceof MechanicalCraftingRecipe crafting) {
            ShapedRecipe shaped = crafting;
            int height = Math.max(1, Math.min(9, shaped.getHeight()));
            int width = Math.max(1, Math.min(9, shaped.getWidth()));
            KubeJSLab.LOGGER.info("[MechCrafting] prefill: original id={}, pattern {}x{}, acceptMirrored={}",
                    original.getId(), width, height, crafting.acceptsMirrored());
            return new LabRecipeFieldValues(current.shapeless(), current.experience(), current.cookingTime(),
                    current.count(), current.processingTime(), current.heatRequirement(), current.keepHeldItem(),
                    crafting.acceptsMirrored(), width, height, current.outputCount());
        }
        KubeJSLab.LOGGER.warn("[MechCrafting] prefill: original {} is not a MechanicalCraftingRecipe ({}), keeping defaults",
                original == null ? "null" : original.getId(), original == null ? "null" : original.getClass().getName());
        return current;
    }
}
