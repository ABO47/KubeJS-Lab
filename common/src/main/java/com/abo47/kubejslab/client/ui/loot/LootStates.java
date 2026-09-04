package com.abo47.kubejslab.client.ui.loot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.client.ui.picker.SearchNormalizer;
import com.abo47.kubejslab.client.ui.shell.TabCounts;
import com.abo47.kubejslab.loot.model.LootState;
import com.abo47.kubejslab.loot.model.LootStatus;
import com.abo47.kubejslab.loot.runtime.LootService;


public final class LootStates {
    private static final Map<ResourceLocation, LootState> STATE = new HashMap<>();

    private LootStates() {
    }

    public static void apply(Map<ResourceLocation, LootState> states) {
        STATE.clear();
        STATE.putAll(states);
    }

    public static LootStatus statusOf(ResourceLocation id) {
        LootState entry = STATE.get(id);
        return entry == null ? LootStatus.NORMAL : entry.status();
    }

    public static LootState stateOf(ResourceLocation id) {
        return STATE.get(id);
    }

    public static TabCounts counts(boolean kubejs) {
        Set<ResourceLocation> seen = new HashSet<>();
        int total = 0;
        for (LootIndex.LootEntry e : LootIndex.search("", kubejs)) {
            if (seen.add(e.id())) total++;
        }
        for (LootIndex.LootEntry e : stateEntries()) {
            if (e.kubejs() != kubejs) continue;
            if (seen.add(e.id())) total++;
        }
        int disabled = 0;
        int modified = 0;
        for (LootState s : STATE.values()) {
            if (s.id().getNamespace().equals("kubejs") != kubejs) continue;
            if (s.status() == LootStatus.DISABLED) disabled++;
            else if (s.status() == LootStatus.MODIFIED) modified++;
        }
        return new TabCounts(total, disabled, modified);
    }

    public static List<LootIndex.LootEntry> stateEntries() {
        Map<ResourceLocation, LootIndex.LootEntry> result = new LinkedHashMap<>();
        for (LootState entry : STATE.values()) {
            String name = entry.name().isBlank() ? entry.id().getPath() : entry.name();
            result.put(entry.id(), entryOf(entry.id(), name, entry.lootType()));
        }
        return new ArrayList<>(result.values());
    }

    public static LootIndex.LootEntry entryOf(ResourceLocation id, String name, String lootType) {
        String normalizedId = SearchNormalizer.normalizeUserSearch(id.toString());
        String normalizedName = SearchNormalizer.normalizeUserSearch(name);
        return new LootIndex.LootEntry(id, name, lootType == null ? LootService.LOOT_TYPE_BLOCK : lootType,
                id.getNamespace().equals("kubejs"), normalizedId, normalizedName);
    }
}
