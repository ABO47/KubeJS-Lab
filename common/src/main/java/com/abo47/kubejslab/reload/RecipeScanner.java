package com.abo47.kubejslab.reload;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;

import com.abo47.kubejslab.KubeJSLab;


public final class RecipeScanner {
    private static final Gson GSON = new GsonBuilder().setLenient().create();
    private static final FileToIdConverter LISTER = FileToIdConverter.json("recipes");

    private RecipeScanner() {
    }

    public static Map<ResourceLocation, JsonElement> scan(ResourceManager resources) {
        Map<ResourceLocation, JsonElement> recipes = new HashMap<>();
        for (Map.Entry<ResourceLocation, Resource> entry : LISTER.listMatchingResources(resources).entrySet()) {
            ResourceLocation file = entry.getKey();
            ResourceLocation id = LISTER.fileToId(file);
            try (Reader reader = entry.getValue().openAsReader()) {
                JsonElement json = GsonHelper.fromJson(GSON, reader, JsonElement.class);
                if (json != null) {
                    recipes.put(id, json);
                }
            } catch (Exception e) {
                KubeJSLab.LOGGER.error("[{}] skipped recipe file {} (from {}): {}", KubeJSLab.MOD_ID, id, file,
                        e.toString());
            }
        }
        return recipes;
    }
}
