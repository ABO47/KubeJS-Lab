package com.abo47.kubejslab.client.ui.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import com.abo47.kubejslab.client.ui.picker.LabSearchNormalizer;
import com.abo47.kubejslab.item.model.LabCustomTier;
import com.abo47.kubejslab.item.model.LabItemState;
import com.abo47.kubejslab.item.model.LabItemStatus;


public final class LabItemStates {
    private static final Map<ResourceLocation, LabItemState> STATE = new HashMap<>();
    private static final Set<ResourceLocation> PENDING_EXTRA = new HashSet<>();

    private LabItemStates() {
    }

    public static void apply(Map<ResourceLocation, LabItemState> states) {
        apply(states, List.of());
    }

    public static void apply(Map<ResourceLocation, LabItemState> states, List<ResourceLocation> pendingOnly) {
        STATE.clear();
        STATE.putAll(states);
        PENDING_EXTRA.clear();
        PENDING_EXTRA.addAll(pendingOnly);
    }

    public static LabItemStatus statusOf(ResourceLocation id) {
        LabItemState entry = STATE.get(id);
        return entry == null ? LabItemStatus.NORMAL : entry.status();
    }

    public static LabItemState stateOf(ResourceLocation id) {
        return STATE.get(id);
    }

    public static boolean pendingRestartOf(ResourceLocation id) {
        LabItemState entry = STATE.get(id);
        return entry != null && entry.pendingRestart() || PENDING_EXTRA.contains(id);
    }

    public static String nameOf(ResourceLocation id) {
        LabItemState entry = STATE.get(id);
        return entry == null ? "" : entry.name();
    }

    public static List<String> customTierIds(boolean armor) {
        List<String> ids = new ArrayList<>();
        for (LabItemState entry : STATE.values()) {
            LabCustomTier tier = entry.customTier();
            if (tier != null && tier.armor() == armor) {
                ids.add(tier.id());
            }
        }
        return ids;
    }

    public static List<LabItemIndex.LabItemEntry> stateEntries() {
        Map<ResourceLocation, LabItemIndex.LabItemEntry> result = new LinkedHashMap<>();
        for (LabItemState entry : STATE.values()) {
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

    private static LabItemIndex.LabItemEntry entryOf(ResourceLocation id, String name) {
        Item item = BuiltInRegistries.ITEM.get(id);
        ItemStack stack = item == null ? ItemStack.EMPTY : new ItemStack(item);
        return new LabItemIndex.LabItemEntry(id, stack, name, id.getNamespace().equals("kubejs"),
                LabSearchNormalizer.normalizeUserSearch(id.toString()),
                LabSearchNormalizer.normalizeUserSearch(name));
    }
}