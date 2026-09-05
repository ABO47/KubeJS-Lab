package com.abo47.kubejslab.recipe.model;

public record SlotDescriptor(boolean input, SlotKind kind, int col, int row, boolean optional,
        SlotTint tint) {
    public SlotDescriptor(boolean input, SlotKind kind, int col, int row, boolean optional) {
        this(input, kind, col, row, optional, SlotTint.NORMAL);
    }
}
