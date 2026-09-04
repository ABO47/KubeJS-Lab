package com.abo47.kubejslab.loot.model;

import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;

public record LootPayload(@Nullable ResourceLocation target, String lootType, LootFieldValues values,
        List<String> tags, List<LootAction> actions) {

    public LootPayload {
        if (lootType == null || lootType.isBlank()) {
            throw new IllegalArgumentException("lootType must not be blank");
        }
        if (values == null) {
            throw new IllegalArgumentException("values must not be null");
        }
        if (tags == null) {
            throw new IllegalArgumentException("tags must not be null");
        }
        if (actions == null) {
            throw new IllegalArgumentException("actions must not be null");
        }
    }
}
