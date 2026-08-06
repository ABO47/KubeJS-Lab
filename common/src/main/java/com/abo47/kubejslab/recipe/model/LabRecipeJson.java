package com.abo47.kubejslab.recipe.model;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

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

    public static JsonObject itemOutputJson(ItemStack stack, float chance) {
        JsonObject obj = itemWithCount(stack);
        if (chance != 1f) {
            obj.addProperty("chance", chance);
        }
        return obj;
    }

    public static JsonObject fluidJson(FluidStack fluid) {
        JsonObject obj = new JsonObject();
        obj.addProperty("fluid", BuiltInRegistries.FLUID.getKey(fluid.getFluid()).toString());
        obj.addProperty("amount", fluid.getAmount());
        if (fluid.getTag() != null) {
            obj.add("nbt", JsonParser.parseString(fluid.getTag().toString()));
        }
        return obj;
    }

    public static JsonObject ingredientJson(LabIngredient ingredient) {
        if (ingredient instanceof LabIngredient.Item item) {
            return itemWithCount(item.stack());
        }
        if (ingredient instanceof LabIngredient.Tag tag) {
            JsonObject obj = new JsonObject();
            obj.addProperty("tag", tag.tag().toString());
            return obj;
        }
        if (ingredient instanceof LabIngredient.Fluid fluid) {
            return fluidJson(fluid.fluid());
        }
        return new JsonObject();
    }

    public static JsonObject outputJson(LabRecipeOutput output) {
        if (output instanceof LabRecipeOutput.Item item) {
            return itemOutputJson(item.stack(), item.chance());
        }
        if (output instanceof LabRecipeOutput.Fluid fluid) {
            return fluidJson(fluid.fluid());
        }
        return new JsonObject();
    }
}
