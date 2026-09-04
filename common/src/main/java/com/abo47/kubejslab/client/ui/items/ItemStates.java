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

import com.abo47.kubejslab.client.ui.picker.SearchNormalizer;
import com.abo47.kubejslab.client.ui.shell.TabCounts;
import com.abo47.kubejslab.item.model.CustomTier;
import com.abo47.kubejslab.item.model.ItemState;
import com.abo47.kubejslab.item.model.ItemStatus;


public final class ItemStates {
    private static final Map<ResourceLocation, ItemState> STATE = new HashMap<>();
    private static final Set<ResourceLocation> PENDING_EXTRA = new HashSet<>();

    private ItemStates() {
    }

    public static void apply(Map<ResourceLocation, ItemState> states) {
        apply(states, List.of());
    }

    public static void apply(Map<ResourceLocation, ItemState> states, List<ResourceLocation> pendingOnly) {
        STATE.clear();
        STATE.putAll(states);
        PENDING_EXTRA.clear();
        PENDING_EXTRA.addAll(pendingOnly);
    }

    public static ItemStatus statusOf(ResourceLocation id) {
        ItemState entry = STATE.get(id);
        return entry == null ? ItemStatus.NORMAL : entry.status();
    }

    public static ItemState stateOf(ResourceLocation id) {
        return STATE.get(id);
    }

    public static boolean pendingRestartOf(ResourceLocation id) {
        ItemState entry = STATE.get(id);
        return entry != null && entry.pendingRestart() || PENDING_EXTRA.contains(id);
    }

    public static TabCounts counts(boolean kubejs) {
        Set<ResourceLocation> seen = new HashSet<>();
        int total = 0;
        for (ItemIndex.ItemEntry e : ItemIndex.search("", kubejs)) {
            if (seen.add(e.id())) total++;
        }
        for (ItemIndex.ItemEntry e : stateEntries()) {
            if (e.kubejs() != kubejs) continue;
            if (seen.add(e.id())) total++;
        }
        int disabled = 0;
        int modified = 0;
        for (ItemState s : STATE.values()) {
            if (s.id().getNamespace().equals("kubejs") != kubejs) continue;
            if (s.status() == ItemStatus.DISABLED) disabled++;
            else if (s.status() == ItemStatus.MODIFIED) modified++;
        }
        return new TabCounts(total, disabled, modified);
    }

    public static String nameOf(ResourceLocation id) {
        ItemState entry = STATE.get(id);
        return entry == null ? "" : entry.name();
    }

    public static List<String> customTierIds(boolean armor) {
        List<String> ids = new ArrayList<>();
        for (ItemState entry : STATE.values()) {
            CustomTier tier = entry.customTier();
            if (tier != null && tier.armor() == armor) {
                ids.add(tier.id());
            }
        }
        return ids;
    }

    public static List<ItemIndex.ItemEntry> stateEntries() {
        Map<ResourceLocation, ItemIndex.ItemEntry> result = new LinkedHashMap<>();
        for (ItemState entry : STATE.values()) {
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

    private static ItemIndex.ItemEntry entryOf(ResourceLocation id, String name) {
        Item item = BuiltInRegistries.ITEM.get(id);
        ItemStack stack = item == null ? ItemStack.EMPTY : new ItemStack(item);
        return new ItemIndex.ItemEntry(id, stack, name, id.getNamespace().equals("kubejs"),
                SearchNormalizer.normalizeUserSearch(id.toString()),
                SearchNormalizer.normalizeUserSearch(name));
    }
}