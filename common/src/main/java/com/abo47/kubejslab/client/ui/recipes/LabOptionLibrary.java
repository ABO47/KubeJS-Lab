package com.abo47.kubejslab.client.ui.recipes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;

import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.api.crafting.ClocheRenderFunction;
import blusunrize.immersiveengineering.api.crafting.MetalPressRecipe;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import dev.architectury.platform.Platform;


public final class LabOptionLibrary {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Set<String> CUSTOM_MOLDS = new LinkedHashSet<>();
    private static Path customMoldsPath;
    private static boolean customMoldsLoaded;

    private static final List<String> DEFAULT_MOLDS = List.of(
            "immersiveengineering:mold_plate",
            "immersiveengineering:mold_gear",
            "immersiveengineering:mold_rod",
            "immersiveengineering:mold_bullet_casing",
            "immersiveengineering:mold_wire",
            "immersiveengineering:mold_packing_4",
            "immersiveengineering:mold_packing_9",
            "immersiveengineering:mold_unpacking");

    private LabOptionLibrary() {
    }

    public static List<String> customMolds() {
        loadCustomMolds();
        return new ArrayList<>(CUSTOM_MOLDS);
    }

    public static boolean isCustomMold(String mold) {
        loadCustomMolds();
        return CUSTOM_MOLDS.contains(mold);
    }

    public static void addCustomMold(String mold) {
        loadCustomMolds();
        if (CUSTOM_MOLDS.add(mold)) {
            saveCustomMolds();
        }
    }

    public static void removeCustomMold(String mold) {
        loadCustomMolds();
        if (CUSTOM_MOLDS.remove(mold)) {
            saveCustomMolds();
        }
    }

    private static void loadCustomMolds() {
        if (customMoldsLoaded) {
            return;
        }
        customMoldsLoaded = true;
        Path path = customMoldsPath();
        if (!Files.exists(path)) {
            return;
        }
        try {
            JsonElement root = JsonParser.parseString(Files.readString(path));
            if (root.isJsonArray()) {
                for (JsonElement element : root.getAsJsonArray()) {
                    if (element.isJsonPrimitive()) {
                        String mold = element.getAsString();
                        if (!mold.isBlank()) {
                            CUSTOM_MOLDS.add(mold);
                        }
                    }
                }
            }
        } catch (IOException | IllegalStateException ignored) {
        }
    }

    private static void saveCustomMolds() {
        try {
            JsonArray array = new JsonArray();
            for (String mold : CUSTOM_MOLDS) {
                array.add(mold);
            }
            Path path = customMoldsPath();
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(array));
        } catch (IOException ignored) {
        }
    }

    private static Path customMoldsPath() {
        if (customMoldsPath == null) {
            customMoldsPath = Platform.getConfigFolder().resolve("kubejslab").resolve("custom_molds.json");
        }
        return customMoldsPath;
    }

    public static List<String> moldOptions() {
        Set<String> options = new LinkedHashSet<>(customMolds());
        options.addAll(DEFAULT_MOLDS);
        if (Platform.isModLoaded("immersiveengineering")) {
            RecipeManager manager = currentManager();
            if (manager != null) {
                for (Recipe<?> recipe : manager.getRecipes()) {
                    if (recipe instanceof MetalPressRecipe press) {
                        options.add(BuiltInRegistries.ITEM.getKey(press.mold).toString());
                    }
                }
            }
        }
        return new ArrayList<>(options);
    }

    public static List<String> blueprintCategoryOptions() {
        Set<String> options = new LinkedHashSet<>(LabBlueprintCategories.custom());
        if (Platform.isModLoaded("immersiveengineering")) {
            if (Minecraft.getInstance().level != null) {
                options.addAll(BlueprintCraftingRecipe.getCategoriesWithRecipes(Minecraft.getInstance().level));
            }
            RecipeManager manager = currentManager();
            if (manager != null) {
                for (Recipe<?> recipe : manager.getRecipes()) {
                    if (recipe instanceof BlueprintCraftingRecipe blueprint) {
                        options.add(blueprint.blueprintCategory);
                    }
                }
            }
        }
        options.removeIf(String::isBlank);
        return new ArrayList<>(options);
    }

    public static List<String> clocheRenderTypes() {
        try {
            Set<String> keys = ClocheRenderFunction.RENDER_FUNCTION_FACTORIES.keySet();
            if (!keys.isEmpty()) {
                return new ArrayList<>(keys);
            }
        } catch (RuntimeException | LinkageError ignored) {
        }
        return List.of("crop", "stacking", "stem", "generic", "hemp", "chorus");
    }

    private static RecipeManager currentManager() {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        return connection == null ? null : connection.getRecipeManager();
    }
}