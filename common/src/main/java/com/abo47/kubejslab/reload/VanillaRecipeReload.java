package com.abo47.kubejslab.reload;

import java.util.Map;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.crafting.RecipeManager;

import com.abo47.kubejslab.platform.Services;


public final class VanillaRecipeReload {
    private VanillaRecipeReload() {
    }

    public static void reload(MinecraftServer server) {
        RecipeManager recipes = server.getRecipeManager();
        ResourceManager resources = server.getResourceManager();
        Map<ResourceLocation, JsonElement> data = RecipeScanner.scan(resources);
        Services.platform().applyRecipeData(recipes, data, resources);
    }
}
