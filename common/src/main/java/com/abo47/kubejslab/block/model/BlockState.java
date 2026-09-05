package com.abo47.kubejslab.block.model;

import java.util.List;

import net.minecraft.resources.ResourceLocation;


public record BlockState(ResourceLocation id, String type, BlockStatus status, boolean pendingRestart,
        String name, boolean wasModified, BlockFieldValues values, List<String> tags, List<BlockAction> actions) {
}
