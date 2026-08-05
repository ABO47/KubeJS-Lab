package com.abo47.kubejslab.recipe.model;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record LabRecipeStateEntry(ResourceLocation id, LabRecipeStatus status, ItemStack output, String name,
        boolean wasModified) {
}
