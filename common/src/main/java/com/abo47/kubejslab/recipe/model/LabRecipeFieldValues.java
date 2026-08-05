package com.abo47.kubejslab.recipe.model;

public record LabRecipeFieldValues(boolean shapeless, float experience, int cookingTime, int count) {

    public LabRecipeFieldValues {
        experience = Math.max(0f, experience);
        cookingTime = Math.max(0, cookingTime);
        count = Math.max(1, count);
    }

    public static LabRecipeFieldValues defaults() {
        return new LabRecipeFieldValues(false, 0f, 200, 1);
    }
}
