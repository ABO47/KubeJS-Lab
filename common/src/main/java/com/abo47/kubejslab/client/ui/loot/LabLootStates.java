package com.abo47.kubejslab.client.ui.loot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.client.ui.base.LabTabCounts;
import com.abo47.kubejslab.client.ui.picker.LabSearchNormalizer;
import com.abo47.kubejslab.loot.model.LabLootState;
import com.abo47.kubejslab.loot.model.LabLootStatus;
import com.abo47.kubejslab.loot.runtime.LabLootService;


public final class LabLootStates {
    private static final Map<ResourceLocation, LabLootState> STATE = new HashMap<>();
    private static final Set<ResourceLocation> PENDING_EXTRA = new HashSet<>();

    private LabLootStates() {
    }

    public static void apply(Map<ResourceLocation, LabLootState> states) {
        apply(states, List.of());
    }

    public static void apply(Map<ResourceLocation, LabLootState> states, List<ResourceLocation> pendingOnly) {
        STATE.clear();
        STATE.putAll(states);
        PENDING_EXTRA.clear();
        PENDING_EXTRA.addAll(pendingOnly);
    }

    public static LabLootStatus statusOf(ResourceLocation id) {
        LabLootState entry = STATE.get(id);
        return entry == null ? LabLootStatus.NORMAL : entry.status();
    }

    public static LabLootState stateOf(ResourceLocation id) {
        return STATE.get(id);
    }

    public static boolean pendingRestartOf(ResourceLocation id) {
        LabLootState entry = STATE.get(id);
        return entry != null && entry.pendingRestart() || PENDING_EXTRA.contains(id);
    }

    public static LabTabCounts counts(boolean kubejs) {
        Set<ResourceLocation> seen = new HashSet<>();
        int total = 0;
        for (LabLootIndex.LabLootEntry e : LabLootIndex.search("", kubejs)) {
            if (seen.add(e.id())) total++;
        }
        for (LabLootIndex.LabLootEntry e : stateEntries()) {
            if (e.kubejs() != kubejs) continue;
            if (seen.add(e.id())) total++;
        }
        int disabled = 0;
        int modified = 0;
        for (LabLootState s : STATE.values()) {
            if (s.id().getNamespace().equals("kubejs") != kubejs) continue;
            if (s.status() == LabLootStatus.DISABLED) disabled++;
            else if (s.status() == LabLootStatus.MODIFIED) modified++;
        }
        return new LabTabCounts(total, disabled, modified);
    }

    public static List<LabLootIndex.LabLootEntry> stateEntries() {
        Map<ResourceLocation, LabLootIndex.LabLootEntry> result = new LinkedHashMap<>();
        for (LabLootState entry : STATE.values()) {
            String name = entry.name().isBlank() ? entry.id().getPath() : entry.name();
            result.put(entry.id(), entryOf(entry.id(), name, entry.lootType()));
        }
        for (ResourceLocation id : PENDING_EXTRA) {
            if (!result.containsKey(id)) {
                result.put(id, entryOf(id, id.getPath(), LabLootService.LOOT_TYPE_BLOCK));
            }
        }
        return new ArrayList<>(result.values());
    }

    public static LabLootIndex.LabLootEntry entryOf(ResourceLocation id, String name, String lootType) {
        String normalizedId = LabSearchNormalizer.normalizeUserSearch(id.toString());
        String normalizedName = LabSearchNormalizer.normalizeUserSearch(name);
        return new LabLootIndex.LabLootEntry(id, name, lootType == null ? LabLootService.LOOT_TYPE_BLOCK : lootType,
                id.getNamespace().equals("kubejs"), normalizedId, normalizedName);
    }
}
