package com.abo47.kubejslab.recipe.vanilla;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import com.abo47.kubejslab.recipe.LabRecipeMachine;
import com.abo47.kubejslab.recipe.model.LabIngredient;
import com.abo47.kubejslab.recipe.model.LabRecipeField;
import com.abo47.kubejslab.recipe.model.LabRecipeFieldValues;
import com.abo47.kubejslab.recipe.model.LabRecipeJson;
import com.abo47.kubejslab.recipe.model.LabRecipeOutput;

public final class CraftingMachine implements LabRecipeMachine {
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
    public List<LabRecipeField> fields() {
        return List.of(LabRecipeField.SHAPELESS);
    }

    @Override
    public JsonObject buildJson(String jsonType, List<LabIngredient> inputs, List<LabRecipeOutput> outputs,
            LabRecipeFieldValues values) {
        JsonObject json = new JsonObject();
        if (values.shapeless()) {
            json.addProperty("type", "kubejs:shapeless");
            JsonArray ingredients = new JsonArray();
            for (LabIngredient ingredient : inputs) {
                if (!ingredient.isEmpty()) {
                    ingredients.add(LabRecipeJson.ingredientJson(ingredient));
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
                    LabIngredient ingredient = row * 3 + col < inputs.size() ? inputs.get(row * 3 + col)
                            : new LabIngredient.Item(ItemStack.EMPTY);
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
        }
        json.add("result", LabRecipeJson.itemWithCount(LabRecipeOutput.firstItem(outputs)));
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
        return new LabRecipeFieldValues(original instanceof ShapelessRecipe, current.experience(),
                current.cookingTime(), current.count(), current.processingTime(), current.heatRequirement(),
                current.keepHeldItem(), current.acceptMirrored(), current.gridWidth(), current.gridHeight());
    }
}
