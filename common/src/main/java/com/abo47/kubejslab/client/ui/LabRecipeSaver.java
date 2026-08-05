package com.abo47.kubejslab.client.ui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class LabRecipeSaver {
    private static final String KUBEJS_NAMESPACE = "kubejs";
    private static final String RECIPE_ROOT = "kubejs/data";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private LabRecipeSaver() {
    }

    public static ResourceLocation saveShaped(ItemStack[][] grid, ItemStack output) {
        List<String> pattern = new ArrayList<>();
        Map<Character, JsonObject> key = new LinkedHashMap<>();
        Map<String, Character> charByItem = new LinkedHashMap<>();
        char nextChar = 'A';

        for (int row = 0; row < 3; row++) {
            StringBuilder rowStr = new StringBuilder();
            for (int col = 0; col < 3; col++) {
                ItemStack stack = grid[row][col];
                if (stack.isEmpty()) {
                    rowStr.append(' ');
                } else {
                    String itemKey = stack.getItem().builtInRegistryHolder().key().location().toString()
                            + (stack.hasTag() ? "|" + stack.getTag() : "");
                    Character c = charByItem.get(itemKey);
                    if (c == null) {
                        c = nextChar++;
                        charByItem.put(itemKey, c);
                    }
                    rowStr.append((char) c);
                    key.put(c, itemJson(stack));
                }
            }
            pattern.add(rowStr.toString());
        }

        JsonObject json = new JsonObject();
        json.addProperty("type", KUBEJS_NAMESPACE + ":shaped");

        JsonArray patternArr = new JsonArray();
        for (String row : pattern) patternArr.add(row);
        json.add("pattern", patternArr);

        JsonObject keyObj = new JsonObject();
        for (var entry : key.entrySet()) {
            keyObj.add(String.valueOf(entry.getKey()), entry.getValue());
        }
        json.add("key", keyObj);

        json.add("result", itemWithCount(output));

        return writeRecipe(generateId(output), json);
    }

    public static ResourceLocation saveShapeless(ItemStack[] inputs, ItemStack output) {
        JsonArray ingredients = new JsonArray();
        for (ItemStack stack : inputs) {
            if (!stack.isEmpty()) {
                ingredients.add(itemJson(stack));
            }
        }

        JsonObject json = new JsonObject();
        json.addProperty("type", KUBEJS_NAMESPACE + ":shapeless");
        json.add("ingredients", ingredients);
        json.add("result", itemWithCount(output));

        return writeRecipe(generateId(output), json);
    }

    private static JsonObject itemJson(ItemStack stack) {
        JsonObject obj = new JsonObject();
        obj.addProperty("item", stack.getItem().builtInRegistryHolder().key().location().toString());
        if (stack.hasTag()) {
            obj.add("nbt", new JsonPrimitive(stack.getTag().toString()));
        }
        return obj;
    }

    private static JsonObject itemWithCount(ItemStack stack) {
        JsonObject obj = itemJson(stack);
        if (stack.getCount() > 1) {
            obj.addProperty("count", stack.getCount());
        }
        return obj;
    }

    private static ResourceLocation generateId(ItemStack output) {
        String path = output.getItem().builtInRegistryHolder().key().location().getPath();
        return new ResourceLocation(KUBEJS_NAMESPACE, "lab/" + path);
    }

    private static ResourceLocation writeRecipe(ResourceLocation baseId, JsonObject json) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.gameDirectory == null) return null;

        try {
            Path dir = mc.gameDirectory.toPath().resolve(RECIPE_ROOT).resolve(baseId.getNamespace()).resolve("recipes");
            Files.createDirectories(dir);
            String basePath = baseId.getPath();
            Path file = dir.resolve(basePath + ".json");
            int suffix = 2;
            while (Files.exists(file)) {
                file = dir.resolve(basePath + "_" + suffix + ".json");
                suffix++;
            }
            Files.writeString(file, GSON.toJson(json) + "\n");
            String fileName = file.getFileName().toString();
            String path = fileName.substring(0, fileName.length() - ".json".length());
            return new ResourceLocation(baseId.getNamespace(), path);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void triggerReload() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getConnection() == null) return;

        mc.getConnection().sendCommand("reload");
    }
}
