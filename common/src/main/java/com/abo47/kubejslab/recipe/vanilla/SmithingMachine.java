package com.abo47.kubejslab.recipe.vanilla;

import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;

import com.abo47.kubejslab.recipe.RecipeHandler;
import com.abo47.kubejslab.recipe.model.RecipeField;
import com.abo47.kubejslab.recipe.model.RecipeFieldValues;
import com.abo47.kubejslab.recipe.model.RecipeIngredient;
import com.abo47.kubejslab.recipe.model.RecipeJson;
import com.abo47.kubejslab.recipe.model.RecipeOutput;
import com.abo47.kubejslab.recipe.model.SlotDescriptor;
import com.abo47.kubejslab.recipe.model.SlotKind;

import com.google.gson.JsonObject;


public final class SmithingMachine implements RecipeHandler {
    private static final ResourceLocation JEI_UID = new ResourceLocation("minecraft", "smithing");
    private static final String TRANSFORM_TYPE = "minecraft:smithing_transform";
    private static final String TRIM_TYPE = "minecraft:smithing_trim";

    @Override
    public List<SlotDescriptor> inputSlots() {
        return List.of(
                new SlotDescriptor(true, SlotKind.ITEM, 0, 0, false),
                new SlotDescriptor(true, SlotKind.ITEM, 1, 0, false),
                new SlotDescriptor(true, SlotKind.ITEM, 2, 0, false));
    }

    @Override
    public List<SlotDescriptor> outputSlots() {
        return List.of(new SlotDescriptor(false, SlotKind.ITEM, 0, 0, true));
    }

    @Override
    public ResourceLocation jeiUid() {
        return JEI_UID;
    }

    @Override
    public String jsonType() {
        return TRANSFORM_TYPE;
    }

    @Override
    public String jsonTypeFor(Recipe<?> original) {
        return original instanceof SmithingTrimRecipe ? TRIM_TYPE : TRANSFORM_TYPE;
    }

    @Override
    public List<RecipeField> fields() {
        return List.of();
    }

    @Override
    public boolean allowsEmptyResult(Recipe<?> original) {
        return original instanceof SmithingTrimRecipe;
    }

    @Override
    public JsonObject buildJson(String jsonType, List<RecipeIngredient> inputs, List<RecipeOutput> outputs,
            RecipeFieldValues values) {
        if (inputs.size() < 3) {
            return null;
        }
        JsonObject json = new JsonObject();
        json.addProperty("type", jsonType);
        json.add("template", RecipeJson.ingredientJson(inputs.get(0)));
        json.add("base", RecipeJson.ingredientJson(inputs.get(1)));
        json.add("addition", RecipeJson.ingredientJson(inputs.get(2)));
        if (!TRIM_TYPE.equals(jsonType)) {
            json.add("result", RecipeJson.itemWithCount(RecipeOutput.firstItem(outputs)));
        }
        return json;
    }
}
