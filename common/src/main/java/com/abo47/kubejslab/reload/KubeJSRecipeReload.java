package com.abo47.kubejslab.reload;

import java.util.Map;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.crafting.RecipeManager;

import dev.latvian.mods.kubejs.server.KubeJSReloadListener;
import dev.latvian.mods.kubejs.server.ServerScriptManager;

import com.abo47.kubejslab.platform.Services;


public final class KubeJSRecipeReload {
    private KubeJSRecipeReload() {
    }

    public static void reload(MinecraftServer server) {
        RecipeManager recipes = server.getRecipeManager();
        CloseableResourceManager clean = CleanResources.openClean(server);
        try {
            ResourceManager wrapped = ServerScriptManager.instance.wrapResourceManager(clean);
            Map<ResourceLocation, JsonElement> data = RecipeScanner.scan(wrapped);
            Services.platform().applyRecipeData(recipes, data, wrapped);
            KubeJSReloadListener.postAfterRecipes();
        } finally {
            clean.close();
        }
    }
}
