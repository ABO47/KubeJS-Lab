package com.abo47.kubejslab.recipe.model;

public record LabRecipeFieldValues(boolean shapeless, float experience, int cookingTime, int count,
        int processingTime, HeatRequirement heatRequirement, boolean keepHeldItem, boolean acceptMirrored,
        int gridWidth, int gridHeight) {

    public LabRecipeFieldValues {
        experience = Math.max(0f, experience);
        cookingTime = Math.max(0, cookingTime);
        count = Math.max(1, count);
        processingTime = Math.max(0, processingTime);
        heatRequirement = heatRequirement == null ? HeatRequirement.NONE : heatRequirement;
        gridWidth = Math.max(1, Math.min(9, gridWidth));
        gridHeight = Math.max(1, Math.min(9, gridHeight));
    }

    public static LabRecipeFieldValues defaults() {
        return new LabRecipeFieldValues(false, 0f, 200, 1, 100, HeatRequirement.NONE, false, true, 3, 3);
    }
}
