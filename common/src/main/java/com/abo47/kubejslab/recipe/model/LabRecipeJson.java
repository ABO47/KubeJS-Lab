package com.abo47.kubejslab.recipe.model;

import com.google.gson.JsonObject;

import net.minecraft.world.item.ItemStack;

public final class LabRecipeJson {
    private LabRecipeJson() {
    }

    public static JsonObject itemJson(ItemStack stack) {
        JsonObject obj = new JsonObject();
        obj.addProperty("item", stack.getItem().builtInRegistryHolder().key().location().toString());
        if (stack.hasTag()) {
            obj.addProperty("nbt", stack.getTag().toString());
        }
        return obj;
    }

    public static JsonObject itemWithCount(ItemStack stack) {
        JsonObject obj = itemJson(stack);
        if (stack.getCount() > 1) {
            obj.addProperty("count", stack.getCount());
        }
        return obj;
    }
}
