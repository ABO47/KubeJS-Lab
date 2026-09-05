package com.abo47.kubejslab.client.ui.loot;

import java.util.Locale;

import com.abo47.kubejslab.loot.model.LootField;


public final class LootTooltips {
    private LootTooltips() {
    }

    public static String key(LootField field) {
        return LootKeys.LOOT_TIP_PREFIX + field.name().toLowerCase(Locale.ROOT);
    }
}
