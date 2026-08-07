package com.abo47.kubejslab.recipe.model;

public record LabRecipeFieldValues(boolean shapeless, float experience, int cookingTime, int count,
        int processingTime, HeatRequirement heatRequirement, boolean keepHeldItem, boolean acceptMirrored,
        int gridWidth, int gridHeight, int energy, int creosoteAmount, String mold, String blueprintCategory,
        ClocheRenderType clocheRenderType, String clocheRenderBlock, int fluidInputAmount, int fluidOutputAmount) {

    public LabRecipeFieldValues {
        experience = Math.max(0f, experience);
        cookingTime = Math.max(0, cookingTime);
        count = Math.max(1, count);
        processingTime = Math.max(0, processingTime);
        heatRequirement = heatRequirement == null ? HeatRequirement.NONE : heatRequirement;
        gridWidth = Math.max(1, Math.min(9, gridWidth));
        gridHeight = Math.max(1, Math.min(9, gridHeight));
        energy = Math.max(0, energy);
        creosoteAmount = Math.max(0, creosoteAmount);
        mold = mold == null ? "" : mold;
        blueprintCategory = blueprintCategory == null ? "" : blueprintCategory;
        clocheRenderType = clocheRenderType == null ? ClocheRenderType.GENERIC : clocheRenderType;
        clocheRenderBlock = clocheRenderBlock == null ? "" : clocheRenderBlock;
        fluidInputAmount = Math.max(0, fluidInputAmount);
        fluidOutputAmount = Math.max(0, fluidOutputAmount);
    }

    public LabRecipeFieldValues(boolean shapeless, float experience, int cookingTime, int count,
            int processingTime, HeatRequirement heatRequirement, boolean keepHeldItem, boolean acceptMirrored,
            int gridWidth, int gridHeight) {
        this(shapeless, experience, cookingTime, count, processingTime, heatRequirement, keepHeldItem,
                acceptMirrored, gridWidth, gridHeight, 0, 0, "", "", ClocheRenderType.GENERIC, "", 0, 0);
    }

    public LabRecipeFieldValues(boolean shapeless, float experience, int cookingTime, int count,
            int processingTime, HeatRequirement heatRequirement, boolean keepHeldItem, boolean acceptMirrored,
            int gridWidth, int gridHeight, int energy, int creosoteAmount, String mold, String blueprintCategory,
            ClocheRenderType clocheRenderType, String clocheRenderBlock) {
        this(shapeless, experience, cookingTime, count, processingTime, heatRequirement, keepHeldItem,
                acceptMirrored, gridWidth, gridHeight, energy, creosoteAmount, mold, blueprintCategory,
                clocheRenderType, clocheRenderBlock, 0, 0);
    }

    public static LabRecipeFieldValues defaults() {
        return new LabRecipeFieldValues(false, 0f, 200, 1, 100, HeatRequirement.NONE, false, true, 3, 3, 0, 0,
                "", "", ClocheRenderType.GENERIC, "", 0, 0);
    }
}