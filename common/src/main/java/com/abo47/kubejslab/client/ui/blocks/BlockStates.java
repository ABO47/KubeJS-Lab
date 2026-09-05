package com.abo47.kubejslab.client.ui.blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import com.abo47.kubejslab.block.model.BlockState;
import com.abo47.kubejslab.block.model.BlockStatus;
import com.abo47.kubejslab.client.ui.picker.SearchNormalizer;
import com.abo47.kubejslab.client.ui.shell.TabCounts;


public final class BlockStates {
    private static final Map<ResourceLocation, BlockState> STATE = new HashMap<>();
    private static final Set<ResourceLocation> PENDING_EXTRA = new HashSet<>();

    private BlockStates() {
    }

    public static void apply(Map<ResourceLocation, BlockState> states, List<ResourceLocation> pendingOnly) {
        STATE.clear();
        STATE.putAll(states);
        PENDING_EXTRA.clear();
        PENDING_EXTRA.addAll(pendingOnly);
    }

    public static BlockStatus statusOf(ResourceLocation id) {
        BlockState entry = STATE.get(id);
        return entry == null ? BlockStatus.NORMAL : entry.status();
    }

    public static BlockState stateOf(ResourceLocation id) {
        return STATE.get(id);
    }

    public static boolean pendingRestartOf(ResourceLocation id) {
        BlockState entry = STATE.get(id);
        return entry != null && entry.pendingRestart() || PENDING_EXTRA.contains(id);
    }

    public static TabCounts counts(boolean kubejs) {
        Set<ResourceLocation> seen = new HashSet<>();
        int total = 0;
        for (BlockIndex.BlockEntry e : BlockIndex.search("", kubejs)) {
            if (seen.add(e.id())) total++;
        }
        for (BlockIndex.BlockEntry e : stateEntries()) {
            if (e.kubejs() != kubejs) continue;
            if (seen.add(e.id())) total++;
        }
        int disabled = 0;
        int modified = 0;
        for (BlockState s : STATE.values()) {
            if (s.id().getNamespace().equals("kubejs") != kubejs) continue;
            if (s.status() == BlockStatus.DISABLED) disabled++;
            else if (s.status() == BlockStatus.MODIFIED) modified++;
        }
        return new TabCounts(total, disabled, modified);
    }

    public static List<BlockIndex.BlockEntry> stateEntries() {
        Map<ResourceLocation, BlockIndex.BlockEntry> result = new LinkedHashMap<>();
        for (BlockState entry : STATE.values()) {
            result.put(entry.id(),
                    entryOf(entry.id(), entry.name().isBlank() ? entry.id().getPath() : entry.name()));
        }
        for (ResourceLocation id : PENDING_EXTRA) {
            if (!result.containsKey(id)) {
                result.put(id, entryOf(id, id.getPath()));
            }
        }
        return new ArrayList<>(result.values());
    }

    private static BlockIndex.BlockEntry entryOf(ResourceLocation id, String name) {
        Block block = BuiltInRegistries.BLOCK.get(id);
        ItemStack stack;
        if (block == null) {
            stack = ItemStack.EMPTY;
        } else {
            var item = block.asItem();
            stack = item == net.minecraft.world.item.Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
        }
        return new BlockIndex.BlockEntry(id, stack, name, id.getNamespace().equals("kubejs"),
                SearchNormalizer.normalizeUserSearch(id.toString()),
                SearchNormalizer.normalizeUserSearch(name));
    }
}
