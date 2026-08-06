package com.abo47.kubejslab.recipe.model;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;

public record LabRecipePayload(@Nullable ResourceLocation machineUid, List<LabIngredient> inputs,
        List<LabRecipeOutput> outputs, String name, LabRecipeFieldValues values) {

    public LabRecipePayload {
        if (inputs == null) {
            throw new IllegalArgumentException("inputs must not be null");
        }
        if (outputs == null) {
            throw new IllegalArgumentException("outputs must not be null");
        }
    }
}
