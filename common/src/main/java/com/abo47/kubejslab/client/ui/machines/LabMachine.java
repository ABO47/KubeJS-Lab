package com.abo47.kubejslab.client.ui.machines;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import mezz.jei.api.recipe.category.IRecipeCategory;

public record LabMachine(
        IRecipeCategory<?> category,
        ItemStack icon,
        String name,
        boolean supported
) {
    public ResourceLocation recipeTypeUid() {
        return category.getRecipeType().getUid();
    }
}