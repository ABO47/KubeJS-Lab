package com.abo47.kubejslab.recipe.model;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.architectury.platform.Platform;


public final class RecipeJson {
    private RecipeJson() {
    }

    public static JsonObject itemJson(ItemStack stack) {
        JsonObject obj = new JsonObject();
        obj.addProperty("item", stack.getItem().builtInRegistryHolder().key().location().toString());
        if (stack.hasTag()) {
            obj.addProperty("nbt", stack.getTag().toString());
        }
        return obj;
    }

    public static JsonObject itemIngredientJson(ItemStack stack) {
        JsonObject obj = new JsonObject();
        obj.addProperty("item", stack.getItem().builtInRegistryHolder().key().location().toString());
        if (stack.getCount() > 1) {
            obj.addProperty("count", stack.getCount());
        }
        if (stack.hasTag()) {
            CompoundTag nbt = ingredientNbt(stack);
            if (!nbt.isEmpty()) {
                obj.addProperty("type", "forge:partial_nbt");
                obj.addProperty("nbt", nbt.toString());
            }
        }
        return obj;
    }

    private static CompoundTag ingredientNbt(ItemStack stack) {
        CompoundTag tag = stack.getTag().copy();
        if (tag.contains("Damage") && tag.getInt("Damage") == 0) {
            tag.remove("Damage");
        }
        return tag;
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

    public static JsonObject fluidIngredientJson(FluidStack fluid) {
        for (TagKey<Fluid> tag : fluid.getFluid().builtInRegistryHolder().tags().toList()) {
            JsonObject obj = new JsonObject();
            obj.addProperty("tag", tag.location().toString());
            obj.addProperty("amount", fluid.getAmount());
            return obj;
        }
        return fluidJson(fluid);
    }

    public static JsonObject ingredientJson(RecipeIngredient ingredient) {
        if (ingredient instanceof RecipeIngredient.Item item) {
            return Platform.isForge() ? itemIngredientJson(item.stack()) : itemWithCount(item.stack());
        }
        if (ingredient instanceof RecipeIngredient.Tag tag) {
            JsonObject obj = new JsonObject();
            obj.addProperty("tag", tag.tag().toString());
            return obj;
        }
        if (ingredient instanceof RecipeIngredient.Fluid fluid) {
            return fluidJson(fluid.fluid());
        }
        return new JsonObject();
    }

    public static JsonObject outputJson(RecipeOutput output) {
        if (output instanceof RecipeOutput.Item item) {
            return itemOutputJson(item.stack(), item.chance());
        }
        if (output instanceof RecipeOutput.Fluid fluid) {
            return fluidJson(fluid.fluid());
        }
        return new JsonObject();
    }
}
