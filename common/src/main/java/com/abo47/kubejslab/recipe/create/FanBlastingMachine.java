package com.abo47.kubejslab.recipe.create;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.Recipe;

import com.abo47.kubejslab.recipe.vanilla.CookingBase;


public final class FanBlastingMachine extends CookingBase {
    @Override
    public ResourceLocation jeiUid() {
        return new ResourceLocation("create", "fan_blasting");
    }

    @Override
    public String jsonType() {
        return "minecraft:blasting";
    }

    @Override
    public String jsonTypeFor(Recipe<?> original) {
        return original instanceof BlastingRecipe ? "minecraft:blasting" : "minecraft:smelting";
    }
}