package com.abo47.kubejslab.recipe.model;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record LabRecipePayload(@Nullable ResourceLocation machineUid, List<ItemStack> inputs, ItemStack output,
        String name, LabRecipeFieldValues values) {

    public LabRecipePayload {
        if (inputs == null) {
            throw new IllegalArgumentException("inputs must not be null");
        }
    }
}
