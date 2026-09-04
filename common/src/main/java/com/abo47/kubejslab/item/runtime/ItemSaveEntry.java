package com.abo47.kubejslab.item.runtime;

import java.util.List;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.item.model.CustomTier;
import com.abo47.kubejslab.item.model.ItemAction;
import com.abo47.kubejslab.item.model.ItemFieldValues;
import com.abo47.kubejslab.item.model.ItemStatus;


record ItemSaveEntry(String type, ItemStatus status, String name, boolean wasModified,
        CustomTier customTier, ItemFieldValues values, List<String> tags, List<ItemAction> actions) {
}
