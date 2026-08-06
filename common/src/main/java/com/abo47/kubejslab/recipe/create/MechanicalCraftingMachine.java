package com.abo47.kubejslab.recipe.create;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

import com.simibubi.create.content.kinetics.crafter.MechanicalCraftingRecipe;

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
        JsonArray pattern = new JsonArray();
        JsonObject key = new JsonObject();
        Map<String, Character> charByKey = new LinkedHashMap<>();
        char nextChar = 'A';
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
        json.add("pattern", pattern);
        json.add("key", key);
        json.add("result", LabRecipeJson.itemWithCount(LabRecipeOutput.firstItem(outputs)));
        if (!values.acceptMirrored()) {
            json.addProperty("acceptMirrored", false);
        }
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
            int height = Math.max(1, Math.min(9, crafting.getHeight()));
            int width = Math.max(1, Math.min(9, crafting.getWidth()));
            return new LabRecipeFieldValues(current.shapeless(), current.experience(), current.cookingTime(),
                    current.count(), current.processingTime(), current.heatRequirement(), current.keepHeldItem(),
                    crafting.acceptsMirrored(), width, height);
        }
        return current;
    }
}
