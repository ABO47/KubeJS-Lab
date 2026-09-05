package com.abo47.kubejslab.client.ui.recipes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import dev.architectury.platform.Platform;


public final class BlueprintCategories {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Set<String> CUSTOM = new LinkedHashSet<>();
    private static Path storePath;
    private static boolean loaded;

    private BlueprintCategories() {
    }

    private static Path storePath() {
        if (storePath == null) {
            storePath = Platform.getConfigFolder().resolve("kubejslab").resolve("blueprint_categories.json");
        }
        return storePath;
    }

    public static void load() {
        if (loaded) {
            return;
        }
        loaded = true;
        try {
            Path path = storePath();
            if (Files.exists(path)) {
                String json = Files.readString(path);
                JsonArray array = JsonParser.parseString(json).getAsJsonArray();
                for (JsonElement element : array) {
                    String value = element.getAsString();
                    if (!value.isBlank()) {
                        CUSTOM.add(value);
                    }
                }
            }
        } catch (IOException | IllegalStateException ignored) {
        }
    }

    public static List<String> custom() {
        load();
        return new ArrayList<>(CUSTOM);
    }

    public static boolean isCustom(String category) {
        load();
        return category != null && CUSTOM.contains(category);
    }

    public static void add(String category) {
        if (category == null || category.isBlank()) {
            return;
        }
        load();
        CUSTOM.add(category);
        save();
    }

    public static void remove(String category) {
        load();
        if (CUSTOM.remove(category)) {
            save();
        }
    }

    private static void save() {
        try {
            Path path = storePath();
            Files.createDirectories(path.getParent());
            JsonArray array = new JsonArray();
            for (String value : CUSTOM) {
                array.add(value);
            }
            Files.writeString(path, GSON.toJson(array));
        } catch (IOException ignored) {
        }
    }
}