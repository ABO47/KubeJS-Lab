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

import com.abo47.kubejslab.recipe.model.LabIngredient;
import com.abo47.kubejslab.recipe.model.LabRecipeJson;
import com.abo47.kubejslab.recipe.model.LabRecipeOutput;
import com.abo47.kubejslab.recipe.model.LabRecipePayload;

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

    public static JsonObject modify(JsonObject original, LabRecipePayload payload) {
        List<LabRecipeOutput> outputs = payload.outputs();
        if (outputs.isEmpty()) {
            return null;
        }
        List<LabIngredient> inputs = payload.inputs();
        JsonObject json = original.deepCopy();
        if (!inputs.isEmpty()) {
            JsonElement ingredientField = ingredientField(json);
            if (ingredientField != null) {
                replaceIngredients(ingredientField, inputs);
            }
        }
        return replaceOutputs(json, outputs) ? json : null;
    }

    private static JsonElement ingredientField(JsonObject json) {
        JsonElement exact = json.get("ingredients");
        if (exact != null && exact.isJsonArray()) return exact;
        JsonElement single = json.get("ingredient");
        if (single != null && isIngredientContainer(single)) return single;
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
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

    private static void replaceIngredients(JsonElement field, List<LabIngredient> inputs) {
        if (field.isJsonArray()) {
            JsonArray array = field.getAsJsonArray();
            int inputIndex = 0;
            for (int i = 0; i < array.size() && inputIndex < inputs.size(); i++) {
                JsonElement element = array.get(i);
                if (element.isJsonObject() && isItemShaped(element.getAsJsonObject())) {
                    array.set(i, LabRecipeJson.ingredientJson(inputs.get(inputIndex)));
                    inputIndex++;
                }
            }
            for (int i = inputIndex; i < inputs.size(); i++) {
                array.add(LabRecipeJson.ingredientJson(inputs.get(i)));
            }
            return;
        }
        JsonObject target = field.getAsJsonObject();
        JsonObject replacement = LabRecipeJson.ingredientJson(inputs.get(0));
        for (String key : List.copyOf(target.keySet())) target.remove(key);
        for (Map.Entry<String, JsonElement> entry : replacement.entrySet()) target.add(entry.getKey(), entry.getValue());
    }

    private static boolean replaceOutputs(JsonObject json, List<LabRecipeOutput> outputs) {
        JsonElement result = json.get("result");
        if (result == null) result = json.get("results");
        if (result == null) return false;
        if (result.isJsonObject()) {
            applyOutput(result.getAsJsonObject(), outputs.get(0));
            return true;
        }
        if (result.isJsonArray()) {
            JsonArray array = result.getAsJsonArray();
            for (int i = 0; i < array.size() && i < outputs.size(); i++) {
                JsonElement element = array.get(i);
                if (element.isJsonObject()) {
                    applyOutput(element.getAsJsonObject(), outputs.get(i));
                }
            }
            for (int i = array.size(); i < outputs.size(); i++) {
                array.add(LabRecipeJson.outputJson(outputs.get(i)));
            }
            return true;
        }
        return false;
    }

    private static void applyOutput(JsonObject object, LabRecipeOutput output) {
        JsonObject replacement = LabRecipeJson.outputJson(output);
        for (String key : List.copyOf(object.keySet())) object.remove(key);
        for (Map.Entry<String, JsonElement> entry : replacement.entrySet()) object.add(entry.getKey(), entry.getValue());
    }
}
