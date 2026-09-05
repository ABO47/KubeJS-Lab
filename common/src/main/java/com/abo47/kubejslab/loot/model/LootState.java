package com.abo47.kubejslab.loot.model;

import java.util.List;

import net.minecraft.resources.ResourceLocation;

public record LootState(ResourceLocation id, String lootType, LootStatus status,
        String name, boolean wasModified, LootFieldValues values, List<String> tags, List<LootAction> actions) {
}
