package com.abo47.kubejslab.recipe.model;

public record LabSlotDescriptor(boolean input, LabSlotKind kind, int col, int row, boolean optional) {
}