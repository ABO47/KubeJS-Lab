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

    private LabLootStates() {
    }

    public static void apply(Map<ResourceLocation, LabLootState> states) {
        STATE.clear();
        STATE.putAll(states);
    }

    public static LabLootStatus statusOf(ResourceLocation id) {
        LabLootState entry = STATE.get(id);
        return entry == null ? LabLootStatus.NORMAL : entry.status();
    }

    public static LabLootState stateOf(ResourceLocation id) {
        return STATE.get(id);
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
        return new ArrayList<>(result.values());
    }

    public static LabLootIndex.LabLootEntry entryOf(ResourceLocation id, String name, String lootType) {
        String normalizedId = LabSearchNormalizer.normalizeUserSearch(id.toString());
        String normalizedName = LabSearchNormalizer.normalizeUserSearch(name);
        return new LabLootIndex.LabLootEntry(id, name, lootType == null ? LabLootService.LOOT_TYPE_BLOCK : lootType,
                id.getNamespace().equals("kubejs"), normalizedId, normalizedName);
    }
}
