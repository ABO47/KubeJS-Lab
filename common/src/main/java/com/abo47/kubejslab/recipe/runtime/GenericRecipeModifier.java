package com.abo47.kubejslab.recipe.runtime;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.IoSupplier;

import com.abo47.kubejslab.recipe.model.RecipeIngredient;
import com.abo47.kubejslab.recipe.model.RecipeJson;
import com.abo47.kubejslab.recipe.model.RecipeOutput;
import com.abo47.kubejslab.recipe.model.RecipePayload;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public final class GenericRecipeModifier {
    private GenericRecipeModifier() {
    }

    public static JsonObject originalFor(MinecraftServer server, ResourceLocation id) {
        PackRepository repository = server.getPackRepository();
        JsonObject found = null;
        for (String packId : repository.getSelectedIds()) {
            Pack pack = repository.getPack(packId);
            if (pack == null) continue;
            try (PackResources resources = pack.open()) {
                ResourceLocation location = new ResourceLocation(id.getNamespace(), "recipes/" + id.getPath() + ".json");
                IoSupplier<InputStream> supplier = resources.getResource(PackType.SERVER_DATA, location);
                if (supplier == null) continue;
                try (InputStream in = supplier.get()) {
                    found = JsonParser.parseString(new String(in.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
                }
            } catch (Exception ignored) {
            }
        }
        return found;
    }

    public static JsonObject modify(JsonObject original, RecipePayload payload) {
        List<RecipeOutput> outputs = payload.outputs();
        if (outputs.isEmpty()) {
            return null;
        }
        List<RecipeIngredient> inputs = payload.inputs();
        JsonObject json = original.deepCopy();
        if (!inputs.isEmpty()) {
            JsonElement ingredientField = ingredientField(json);
            if (ingredientField != null) {
                replaceIngredients(ingredientField, inputs);
            }
        }
        return replaceOutputs(json, outputs) ? json : null;
    }

    static JsonElement ingredientField(JsonObject json) {
        JsonElement exact = json.get("ingredients");
        if (exact != null && exact.isJsonArray()) return exact;
        JsonElement single = json.get("ingredient");
        if (single != null && isIngredientContainer(single)) return single;
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            String key = entry.getKey();
            if ("result".equals(key) || "results".equals(key) || "output".equals(key)
                    || "outputs".equals(key)) {
                continue;
            }
            if (isIngredientContainer(entry.getValue())) return entry.getValue();
        }
        return null;
    }

    private static boolean isIngredientContainer(JsonElement element) {
        if (element.isJsonArray()) {
            for (JsonElement item : element.getAsJsonArray()) {
                if (item.isJsonObject() && isItemShaped(item.getAsJsonObject())) return true;
            }
            return false;
        }
        if (element.isJsonObject()) return isItemShaped(element.getAsJsonObject());
        return false;
    }

    private static boolean isItemShaped(JsonObject object) {
        return object.has("item") || object.has("tag");
    }

    static void replaceIngredients(JsonElement field, List<RecipeIngredient> inputs) {
        if (field.isJsonArray()) {
            JsonArray array = field.getAsJsonArray();
            JsonArray rebuilt = new JsonArray();
            int inputIndex = 0;
            for (JsonElement element : array) {
                if (element.isJsonObject() && isItemShaped(element.getAsJsonObject())) {
                    if (inputIndex < inputs.size()) {
                        rebuilt.add(RecipeJson.ingredientJson(inputs.get(inputIndex)));
                        inputIndex++;
                    }
                } else {
                    rebuilt.add(element);
                }
            }
            for (int i = inputIndex; i < inputs.size(); i++) {
                rebuilt.add(RecipeJson.ingredientJson(inputs.get(i)));
            }
            while (array.size() > 0) {
                array.remove(0);
            }
            for (JsonElement element : rebuilt) {
                array.add(element);
            }
            return;
        }
        JsonObject target = field.getAsJsonObject();
        JsonObject replacement = RecipeJson.ingredientJson(inputs.get(0));
        for (String key : List.copyOf(target.keySet())) target.remove(key);
        for (Map.Entry<String, JsonElement> entry : replacement.entrySet()) target.add(entry.getKey(), entry.getValue());
    }

    private static boolean replaceOutputs(JsonObject json, List<RecipeOutput> outputs) {
        JsonElement result = json.get("result");
        if (result == null) result = json.get("results");
        if (result == null) return false;
        if (result.isJsonObject()) {
            applyOutput(result.getAsJsonObject(), outputs.get(0));
            return true;
        }
        if (result.isJsonPrimitive()) {
            json.add("result", RecipeJson.outputJson(outputs.get(0)));
            return true;
        }
        if (result.isJsonArray()) {
            JsonArray array = result.getAsJsonArray();
            JsonArray rebuilt = new JsonArray();
            int outputIndex = 0;
            for (JsonElement element : array) {
                if (outputIndex >= outputs.size()) {
                    continue;
                }
                if (element.isJsonObject()) {
                    applyOutput(element.getAsJsonObject(), outputs.get(outputIndex));
                    rebuilt.add(element);
                    outputIndex++;
                } else {
                    rebuilt.add(RecipeJson.outputJson(outputs.get(outputIndex)));
                    outputIndex++;
                }
            }
            for (int i = outputIndex; i < outputs.size(); i++) {
                rebuilt.add(RecipeJson.outputJson(outputs.get(i)));
            }
            while (array.size() > 0) {
                array.remove(0);
            }
            for (JsonElement element : rebuilt) {
                array.add(element);
            }
            return true;
        }
        return false;
    }

    private static void applyOutput(JsonObject object, RecipeOutput output) {
        JsonObject replacement = RecipeJson.outputJson(output);
        for (String key : List.copyOf(object.keySet())) object.remove(key);
        for (Map.Entry<String, JsonElement> entry : replacement.entrySet()) object.add(entry.getKey(), entry.getValue());
    }

    private static final List<String> PASSTHROUGH_KEYS = List.of("group", "category", "conditions",
            "show_notification");

    public static void copyPassthroughKeys(JsonObject original, JsonObject json) {
        for (String key : PASSTHROUGH_KEYS) {
            if (original.has(key) && !json.has(key)) {
                json.add(key, original.get(key));
            }
        }
    }
}
