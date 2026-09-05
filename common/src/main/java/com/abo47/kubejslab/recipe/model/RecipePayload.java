package com.abo47.kubejslab.recipe.model;

import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;


public record RecipePayload(@Nullable ResourceLocation machineUid, List<RecipeIngredient> inputs,
        List<RecipeOutput> outputs, String name, RecipeFieldValues values) {

    public RecipePayload {
        if (inputs == null) {
            throw new IllegalArgumentException("inputs must not be null");
        }
        if (outputs == null) {
            throw new IllegalArgumentException("outputs must not be null");
        }
    }
}
