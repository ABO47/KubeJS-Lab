package com.abo47.kubejslab.client.ui.items;

import java.util.Locale;

import com.abo47.kubejslab.item.model.ItemField;


public final class ItemTooltips {
    private ItemTooltips() {
    }

    public static String key(ItemField field) {
        return ItemKeys.ITEM_TIP_PREFIX + field.name().toLowerCase(Locale.ROOT);
    }
}