package com.abo47.kubejslab.block.runtime;

import java.util.List;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.block.model.BlockAction;
import com.abo47.kubejslab.block.model.BlockFieldValues;
import com.abo47.kubejslab.block.model.BlockStatus;


record BlockSaveEntry(String type, BlockStatus status, String name, boolean wasModified,
        BlockFieldValues values, List<String> tags, List<BlockAction> actions) {
}
