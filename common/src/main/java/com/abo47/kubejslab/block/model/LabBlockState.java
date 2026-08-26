package com.abo47.kubejslab.block.model;

import java.util.List;

import net.minecraft.resources.ResourceLocation;


public record LabBlockState(ResourceLocation id, String type, LabBlockStatus status, boolean pendingRestart,
        String name, boolean wasModified, LabBlockFieldValues values, List<String> tags, List<LabBlockAction> actions) {
}
