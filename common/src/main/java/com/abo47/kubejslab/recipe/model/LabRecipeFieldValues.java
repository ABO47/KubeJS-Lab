package com.abo47.kubejslab.recipe.model;

public record LabRecipeFieldValues(boolean shapeless, float experience, int cookingTime, int count,
        int processingTime, HeatRequirement heatRequirement, boolean keepHeldItem, boolean acceptMirrored,
        int gridWidth, int gridHeight, int outputCount, int energy, int creosoteAmount, String mold,
        String blueprintCategory, ClocheRenderType clocheRenderType, String clocheRenderBlock) {

    public LabRecipeFieldValues {
        experience = Math.max(0f, experience);
        cookingTime = Math.max(0, cookingTime);
        count = Math.max(1, count);
        processingTime = Math.max(0, processingTime);
        heatRequirement = heatRequirement == null ? HeatRequirement.NONE : heatRequirement;
        gridWidth = Math.max(1, Math.min(9, gridWidth));
        gridHeight = Math.max(1, Math.min(9, gridHeight));
        outputCount = Math.max(1, Math.min(6, outputCount));
        energy = Math.max(0, energy);
        creosoteAmount = Math.max(0, creosoteAmount);
        mold = mold == null ? "" : mold;
        blueprintCategory = blueprintCategory == null ? "" : blueprintCategory;
        clocheRenderType = clocheRenderType == null ? ClocheRenderType.GENERIC : clocheRenderType;
        clocheRenderBlock = clocheRenderBlock == null ? "" : clocheRenderBlock;
    }

    public LabRecipeFieldValues(boolean shapeless, float experience, int cookingTime, int count,
            int processingTime, HeatRequirement heatRequirement, boolean keepHeldItem, boolean acceptMirrored,
            int gridWidth, int gridHeight, int outputCount) {
        this(shapeless, experience, cookingTime, count, processingTime, heatRequirement, keepHeldItem,
                acceptMirrored, gridWidth, gridHeight, outputCount, 0, 0, "", "", ClocheRenderType.GENERIC, "");
    }

    public static LabRecipeFieldValues defaults() {
        return new LabRecipeFieldValues(false, 0f, 200, 1, 100, HeatRequirement.NONE, false, true, 3, 3, 1, 0, 0,
                "", "", ClocheRenderType.GENERIC, "");
    }
}