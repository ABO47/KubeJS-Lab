package com.abo47.kubejslab.client.ui.loot;

import java.util.Locale;

import com.abo47.kubejslab.client.ui.base.LabGuiKeys;
import com.abo47.kubejslab.loot.model.LabLootField;


public final class LabLootTooltips {
    private LabLootTooltips() {
    }

    public static String key(LabLootField field) {
        return LabGuiKeys.LAB_LOOT_TIP_PREFIX + field.name().toLowerCase(Locale.ROOT);
    }
}
