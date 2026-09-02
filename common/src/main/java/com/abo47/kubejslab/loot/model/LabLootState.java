package com.abo47.kubejslab.loot.model;

import java.util.List;

import net.minecraft.resources.ResourceLocation;

public record LabLootState(ResourceLocation id, String lootType, LabLootStatus status, boolean pendingRestart,
        String name, boolean wasModified, LabLootFieldValues values, List<String> tags, List<LabLootAction> actions) {
}
