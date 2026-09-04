package com.abo47.kubejslab.recipe.model;

import java.util.List;


public final class SlotLayouts {
    private SlotLayouts() {
    }

    public static List<SlotDescriptor> oneInput() {
        return List.of(slot(true, SlotKind.ITEM, 0, 0, false));
    }

    public static List<SlotDescriptor> oneOutput() {
        return List.of(slot(false, SlotKind.ITEM, 1, 0, false));
    }

    public static SlotDescriptor slot(boolean input, SlotKind kind, int col, int row, boolean optional) {
        return new SlotDescriptor(input, kind, col, row, optional);
    }
}
