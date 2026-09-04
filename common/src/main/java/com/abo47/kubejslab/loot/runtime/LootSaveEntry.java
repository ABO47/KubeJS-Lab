package com.abo47.kubejslab.loot.runtime;

import java.util.List;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.loot.model.LootAction;
import com.abo47.kubejslab.loot.model.LootFieldValues;
import com.abo47.kubejslab.loot.model.LootStatus;


record LootSaveEntry(String lootType, LootStatus status, String name, boolean wasModified,
        LootFieldValues values, List<String> tags, List<LootAction> actions) {
}
