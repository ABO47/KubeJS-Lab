package com.abo47.kubejslab.recipe.model;

public record LabSlotDescriptor(boolean input, LabSlotKind kind, int col, int row, boolean optional,
        LabSlotTint tint) {
    public LabSlotDescriptor(boolean input, LabSlotKind kind, int col, int row, boolean optional) {
        this(input, kind, col, row, optional, LabSlotTint.NORMAL);
    }
}
