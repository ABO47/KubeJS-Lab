package com.abo47.kubejslab.item.model;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;


public record LabItemState(ResourceLocation id, String type, LabItemStatus status, boolean pendingRestart, String name,
        boolean wasModified, @Nullable LabCustomTier customTier, LabItemFieldValues values, List<String> tags,
        List<LabItemAction> actions) {
}