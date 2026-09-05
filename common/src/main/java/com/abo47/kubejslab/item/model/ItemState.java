package com.abo47.kubejslab.item.model;

import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;


public record ItemState(ResourceLocation id, String type, ItemStatus status, boolean pendingRestart, String name,
        boolean wasModified, @Nullable CustomTier customTier, ItemFieldValues values, List<String> tags,
        List<ItemAction> actions) {
}