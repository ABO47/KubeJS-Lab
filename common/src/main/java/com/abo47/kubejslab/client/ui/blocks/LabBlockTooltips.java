package com.abo47.kubejslab.client.ui.blocks;

import java.util.Locale;

import com.abo47.kubejslab.block.model.LabBlockField;
import com.abo47.kubejslab.client.ui.base.LabGuiKeys;


public final class LabBlockTooltips {
    private LabBlockTooltips() {
    }

    public static String key(LabBlockField field) {
        return LabGuiKeys.LAB_BLOCK_TIP_PREFIX + field.name().toLowerCase(Locale.ROOT);
    }
}
