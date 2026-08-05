package com.abo47.kubejslab.recipe.model;

import javax.annotation.Nullable;

import net.minecraft.world.item.ItemStack;

public record LabRecipePayload(boolean shapeless, @Nullable ItemStack[] grid, ItemStack output, String name) {

    public LabRecipePayload {
        if (grid != null && grid.length != 9) {
            throw new IllegalArgumentException("grid must be 9 cells");
        }
    }

    public boolean hasGrid() {
        return grid != null;
    }
}
