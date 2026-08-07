package com.abo47.kubejslab.client.ui.recipes;

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
import blusunrize.immersiveengineering.api.crafting.MetalPressRecipe;


public final class LabOptionLibrary {
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

    public static List<String> moldOptions() {
        Set<String> options = new LinkedHashSet<>(DEFAULT_MOLDS);
        RecipeManager manager = currentManager();
        if (manager != null) {
            for (Recipe<?> recipe : manager.getRecipes()) {
                if (recipe instanceof MetalPressRecipe press) {
                    BuiltInRegistries.ITEM.getKey(press.mold);
                    options.add(BuiltInRegistries.ITEM.getKey(press.mold).toString());
                }
            }
        }
        return new ArrayList<>(options);
    }

    public static List<String> blueprintCategoryOptions() {
        Set<String> options = new LinkedHashSet<>(LabBlueprintCategories.custom());
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
        options.removeIf(String::isBlank);
        return new ArrayList<>(options);
    }

    public static List<String> clocheRenderTypes() {
        return List.of("crop", "stacking", "stem", "generic", "hemp", "chorus");
    }

    private static RecipeManager currentManager() {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        return connection == null ? null : connection.getRecipeManager();
    }
}