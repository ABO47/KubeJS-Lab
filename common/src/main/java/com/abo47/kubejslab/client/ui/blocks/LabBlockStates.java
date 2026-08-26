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

import com.abo47.kubejslab.block.model.LabBlockState;
import com.abo47.kubejslab.block.model.LabBlockStatus;
import com.abo47.kubejslab.client.ui.picker.LabSearchNormalizer;


public final class LabBlockStates {
    private static final Map<ResourceLocation, LabBlockState> STATE = new HashMap<>();
    private static final Set<ResourceLocation> PENDING_EXTRA = new HashSet<>();

    private LabBlockStates() {
    }

    public static void apply(Map<ResourceLocation, LabBlockState> states, List<ResourceLocation> pendingOnly) {
        STATE.clear();
        STATE.putAll(states);
        PENDING_EXTRA.clear();
        PENDING_EXTRA.addAll(pendingOnly);
    }

    public static LabBlockStatus statusOf(ResourceLocation id) {
        LabBlockState entry = STATE.get(id);
        return entry == null ? LabBlockStatus.NORMAL : entry.status();
    }

    public static LabBlockState stateOf(ResourceLocation id) {
        return STATE.get(id);
    }

    public static boolean pendingRestartOf(ResourceLocation id) {
        LabBlockState entry = STATE.get(id);
        return entry != null && entry.pendingRestart() || PENDING_EXTRA.contains(id);
    }

    public static List<LabBlockIndex.LabBlockEntry> stateEntries() {
        Map<ResourceLocation, LabBlockIndex.LabBlockEntry> result = new LinkedHashMap<>();
        for (LabBlockState entry : STATE.values()) {
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

    private static LabBlockIndex.LabBlockEntry entryOf(ResourceLocation id, String name) {
        Block block = BuiltInRegistries.BLOCK.get(id);
        ItemStack stack;
        if (block == null) {
            stack = ItemStack.EMPTY;
        } else {
            var item = block.asItem();
            stack = item == net.minecraft.world.item.Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
        }
        return new LabBlockIndex.LabBlockEntry(id, stack, name, id.getNamespace().equals("kubejs"),
                LabSearchNormalizer.normalizeUserSearch(id.toString()),
                LabSearchNormalizer.normalizeUserSearch(name));
    }
}
