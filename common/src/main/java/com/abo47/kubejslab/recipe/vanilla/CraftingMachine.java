package com.abo47.kubejslab.recipe.vanilla;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.ItemStack;

import com.abo47.kubejslab.recipe.RecipeHandler;
import com.abo47.kubejslab.recipe.model.RecipeField;
import com.abo47.kubejslab.recipe.model.RecipeFieldValues;
import com.abo47.kubejslab.recipe.model.RecipeIngredient;
import com.abo47.kubejslab.recipe.model.RecipeJson;
import com.abo47.kubejslab.recipe.model.RecipeOutput;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


public final class CraftingMachine implements RecipeHandler {
    @Override
    public ResourceLocation jeiUid() {
        return new ResourceLocation("minecraft", "crafting");
    }

    @Override
    public String jsonType() {
        return "kubejs:shaped";
    }

    @Override
    public boolean gridLayout() {
        return true;
    }

    @Override
    public List<RecipeField> fields() {
        return List.of(RecipeField.SHAPELESS);
    }

    @Override
    public JsonObject buildJson(String jsonType, List<RecipeIngredient> inputs, List<RecipeOutput> outputs,
            RecipeFieldValues values) {
        JsonObject json = new JsonObject();
        if (values.shapeless()) {
            json.addProperty("type", "kubejs:shapeless");
            JsonArray ingredients = new JsonArray();
            for (RecipeIngredient ingredient : inputs) {
                if (!ingredient.isEmpty()) {
                    ingredients.add(RecipeJson.ingredientJson(ingredient));
                }
            }
            json.add("ingredients", ingredients);
        } else {
            json.addProperty("type", "kubejs:shaped");
            JsonArray pattern = new JsonArray();
            JsonObject key = new JsonObject();
            Map<String, Character> charByKey = new LinkedHashMap<>();
            char nextChar = 'A';
            for (int row = 0; row < 3; row++) {
                StringBuilder rowStr = new StringBuilder();
                for (int col = 0; col < 3; col++) {
                    RecipeIngredient ingredient = row * 3 + col < inputs.size() ? inputs.get(row * 3 + col)
                            : new RecipeIngredient.Item(ItemStack.EMPTY);
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
                    key.add(String.valueOf((char) c), RecipeJson.ingredientJson(ingredient));
                }
                pattern.add(rowStr.toString());
            }
            json.add("pattern", pattern);
            json.add("key", key);
        }
        json.add("result", RecipeJson.itemWithCount(RecipeOutput.firstItem(outputs)));
        return json;
    }

    private static String ingredientKey(RecipeIngredient ingredient) {
        if (ingredient instanceof RecipeIngredient.Item item) {
            return "i:" + item.stack().getItem().builtInRegistryHolder().key().location()
                    + (item.stack().hasTag() ? "|" + item.stack().getTag() : "");
        }
        if (ingredient instanceof RecipeIngredient.Tag tag) {
            return "t:" + tag.tag();
        }
        return "f:" + ingredient;
    }

    @Override
    public RecipeFieldValues prefill(RecipeFieldValues current, Recipe<?> original) {
        return new RecipeFieldValues(original instanceof ShapelessRecipe, current.experience(),
                current.cookingTime(), current.count(), current.processingTime(), current.heatRequirement(),
                current.keepHeldItem(), current.acceptMirrored(), current.gridWidth(), current.gridHeight());
    }
}
