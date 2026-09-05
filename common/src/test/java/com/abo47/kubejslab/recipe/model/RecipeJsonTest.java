package com.abo47.kubejslab.recipe.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;


class RecipeJsonTest {

    @Test
    void prettyPrintsAcrossLines() {
        JsonObject result = new JsonObject();
        result.addProperty("item", "minecraft:stick");
        JsonObject ingredient = new JsonObject();
        ingredient.addProperty("item", "projectvibrantjourneys:twigs");
        JsonArray ingredients = new JsonArray();
        ingredients.add(ingredient);
        JsonObject json = new JsonObject();
        json.addProperty("type", "kubejs:shapeless");
        json.add("ingredients", ingredients);
        json.add("result", result);

        String pretty = RecipeJson.toPrettyString(json);
        assertTrue(pretty.contains("\n"), "generated recipe json must be pretty printed: " + pretty);
        assertTrue(pretty.contains("\"kubejs:shapeless\""), pretty);
        assertTrue(pretty.contains("\"minecraft:stick\""), pretty);
    }
}
