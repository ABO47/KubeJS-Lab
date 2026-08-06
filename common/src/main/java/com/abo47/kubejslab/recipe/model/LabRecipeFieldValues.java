package com.abo47.kubejslab.recipe.model;

public record LabRecipeFieldValues(boolean shapeless, float experience, int cookingTime, int count,
        int processingTime, HeatRequirement heatRequirement, boolean keepHeldItem) {

    public LabRecipeFieldValues {
        experience = Math.max(0f, experience);
        cookingTime = Math.max(0, cookingTime);
        count = Math.max(1, count);
        processingTime = Math.max(0, processingTime);
        heatRequirement = heatRequirement == null ? HeatRequirement.NONE : heatRequirement;
    }

    public static LabRecipeFieldValues defaults() {
        return new LabRecipeFieldValues(false, 0f, 200, 1, 100, HeatRequirement.NONE, false);
    }
}
