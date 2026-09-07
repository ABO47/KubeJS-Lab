package com.abo47.kubejslab.recipe.runtime;

import java.util.List;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.recipe.model.RecipeIngredient;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


class GenericRecipeModifierTest {

    @Test
    void ingredientFieldSkipsResultObject() {
        JsonObject shaped = JsonParser.parseString(
                "{type: 'minecraft:crafting_shaped', pattern: ['A'], key: {A: {item: 'minecraft:stick'}}, "
                        + "result: {item: 'minecraft:torch', count: 4}}").getAsJsonObject();
        assertNull(GenericRecipeModifier.ingredientField(shaped),
                "result must never be mistaken for the ingredient field");
    }

    @Test
    void ingredientFieldFindsIngredientsArray() {
        JsonObject shapeless = JsonParser.parseString(
                "{type: 'minecraft:crafting_shapeless', ingredients: [{item: 'minecraft:stick'}], "
                        + "result: {item: 'minecraft:torch'}}").getAsJsonObject();
        JsonElement field = GenericRecipeModifier.ingredientField(shapeless);
        assertTrue(field != null && field.isJsonArray());
    }

    @Test
    void shrinkingInputsDropsSurplusEntries() {
        JsonArray ingredients = JsonParser.parseString(
                "[{item: 'minecraft:a'}, {item: 'minecraft:b'}, {item: 'minecraft:c'}]").getAsJsonArray();
        GenericRecipeModifier.replaceIngredients(ingredients,
                List.of(new RecipeIngredient.Tag(new ResourceLocation("minecraft:planks"))));
        assertEquals(1, ingredients.size(), "stale ingredients must not survive a shrink: " + ingredients);
        assertEquals("minecraft:planks", ingredients.get(0).getAsJsonObject().get("tag").getAsString());
    }

    @Test
    void passthroughCopiesMissingKeysOnly() {
        JsonObject original = JsonParser.parseString(
                "{type: 'minecraft:smelting', group: 'iron', category: 'misc'}").getAsJsonObject();
        JsonObject json = JsonParser.parseString("{type: 'kubejs:smelting'}").getAsJsonObject();
        GenericRecipeModifier.copyPassthroughKeys(original, json);
        assertEquals("iron", json.get("group").getAsString());
        assertEquals("misc", json.get("category").getAsString());
        JsonObject existing = JsonParser.parseString("{type: 'x', group: 'keep'}").getAsJsonObject();
        GenericRecipeModifier.copyPassthroughKeys(original, existing);
        assertEquals("keep", existing.get("group").getAsString());
    }
}
