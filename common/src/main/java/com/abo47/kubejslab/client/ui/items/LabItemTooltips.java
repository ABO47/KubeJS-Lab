package com.abo47.kubejslab.client.ui.items;

import java.util.Locale;

import com.abo47.kubejslab.client.ui.base.LabGuiKeys;
import com.abo47.kubejslab.item.model.LabItemField;


public final class LabItemTooltips {
    private LabItemTooltips() {
    }

    public static String key(LabItemField field) {
        return LabGuiKeys.LAB_ITEM_TIP_PREFIX + field.name().toLowerCase(Locale.ROOT);
    }
}