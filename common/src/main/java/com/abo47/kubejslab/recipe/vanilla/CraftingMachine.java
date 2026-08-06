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
import com.abo47.kubejslab.recipe.model.LabRecipeField;
import com.abo47.kubejslab.recipe.model.LabRecipeFieldValues;
import com.abo47.kubejslab.recipe.model.LabRecipeJson;

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
    public JsonObject buildJson(String jsonType, List<ItemStack> inputs, ItemStack output, LabRecipeFieldValues values) {
        JsonObject json = new JsonObject();
        if (values.shapeless()) {
            json.addProperty("type", "kubejs:shapeless");
            JsonArray ingredients = new JsonArray();
            for (ItemStack stack : inputs) {
                if (!stack.isEmpty()) {
                    ingredients.add(LabRecipeJson.itemJson(stack));
                }
            }
            json.add("ingredients", ingredients);
        } else {
            json.addProperty("type", "kubejs:shaped");
            JsonArray pattern = new JsonArray();
            JsonObject key = new JsonObject();
            Map<String, Character> charByItem = new LinkedHashMap<>();
            char nextChar = 'A';
            for (int row = 0; row < 3; row++) {
                StringBuilder rowStr = new StringBuilder();
                for (int col = 0; col < 3; col++) {
                    ItemStack stack = row * 3 + col < inputs.size() ? inputs.get(row * 3 + col) : ItemStack.EMPTY;
                    if (stack.isEmpty()) {
                        rowStr.append(' ');
                        continue;
                    }
                    String itemKey = stack.getItem().builtInRegistryHolder().key().location().toString()
                            + (stack.hasTag() ? "|" + stack.getTag() : "");
                    Character c = charByItem.get(itemKey);
                    if (c == null) {
                        c = nextChar++;
                        charByItem.put(itemKey, c);
                    }
                    rowStr.append((char) c);
                    key.add(String.valueOf((char) c), LabRecipeJson.itemJson(stack));
                }
                pattern.add(rowStr.toString());
            }
            json.add("pattern", pattern);
            json.add("key", key);
        }
        json.add("result", LabRecipeJson.itemWithCount(output));
        return json;
    }

    @Override
    public LabRecipeFieldValues prefill(LabRecipeFieldValues current, Recipe<?> original) {
        return new LabRecipeFieldValues(original instanceof ShapelessRecipe, current.experience(),
                current.cookingTime(), current.count(), current.processingTime(), current.heatRequirement(),
                current.keepHeldItem());
    }
}
