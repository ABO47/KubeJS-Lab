package com.abo47.kubejslab.recipe.model;

import java.util.List;


public final class LabSlotLayouts {
    private LabSlotLayouts() {
    }

    public static List<LabSlotDescriptor> oneInput() {
        return List.of(slot(true, LabSlotKind.ITEM, 0, 0, false));
    }

    public static List<LabSlotDescriptor> oneOutput() {
        return List.of(slot(false, LabSlotKind.ITEM, 1, 0, false));
    }

    public static LabSlotDescriptor slot(boolean input, LabSlotKind kind, int col, int row, boolean optional) {
        return new LabSlotDescriptor(input, kind, col, row, optional);
    }
}
