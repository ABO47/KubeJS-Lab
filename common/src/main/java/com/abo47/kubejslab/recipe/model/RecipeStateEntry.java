package com.abo47.kubejslab.recipe.model;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;


public record RecipeStateEntry(ResourceLocation id, RecipeStatus status, ItemStack output, String name,
        boolean wasModified, ResourceLocation machineUid) {
}
