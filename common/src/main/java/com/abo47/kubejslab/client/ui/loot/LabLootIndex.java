package com.abo47.kubejslab.client.ui.loot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import com.abo47.kubejslab.client.ui.picker.LabSearchNormalizer;
import com.abo47.kubejslab.loot.runtime.LabLootService;


public final class LabLootIndex {
    private static final Map<ResourceLocation, LabLootEntry> ENTRIES = new HashMap<>();

    static {
        for (ResourceLocation id : BuiltInRegistries.BLOCK.keySet()) {
            Block block = BuiltInRegistries.BLOCK.get(id);
            if (block == null || block == Blocks.AIR) {
                continue;
            }
            ENTRIES.put(id, new LabLootEntry(id, id.getPath(), LabLootService.LOOT_TYPE_BLOCK,
                    id.getNamespace().equals("kubejs"),
                    LabSearchNormalizer.normalizeUserSearch(id.toString()),
                    LabSearchNormalizer.normalizeUserSearch(id.getPath())));
        }
        for (ResourceLocation id : BuiltInRegistries.ENTITY_TYPE.keySet()) {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(id);
            if (type == null) {
                continue;
            }
            ENTRIES.put(id, new LabLootEntry(id, id.getPath(), LabLootService.LOOT_TYPE_ENTITY,
                    id.getNamespace().equals("kubejs"),
                    LabSearchNormalizer.normalizeUserSearch(id.toString()),
                    LabSearchNormalizer.normalizeUserSearch(id.getPath())));
        }
    }

    private LabLootIndex() {
    }

    public static List<LabLootEntry> search(String query, boolean kubejsOnly) {
        return search(query, kubejsOnly, null);
    }

    public static List<LabLootEntry> search(String query, boolean kubejsOnly, String lootTypeFilter) {
        String normalizedQuery = LabSearchNormalizer.normalizeQuery(query);
        List<LabLootEntry> matches = new ArrayList<>();
        for (LabLootEntry entry : ENTRIES.values()) {
            if (entry.kubejs() != kubejsOnly) {
                continue;
            }
            if (lootTypeFilter != null && !lootTypeFilter.isBlank() && !lootTypeFilter.equals(entry.lootType())) {
                continue;
            }
            if (normalizedQuery.isBlank() || entry.matches(normalizedQuery)) {
                matches.add(entry);
            }
        }
        matches.sort(Comparator.comparing(LabLootEntry::name, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(LabLootEntry::id));
        return matches;
    }

    public static List<LabLootEntry> entriesForType(String lootType) {
        List<LabLootEntry> matches = new ArrayList<>();
        for (LabLootEntry entry : ENTRIES.values()) {
            if (lootType != null && !lootType.isBlank() && !lootType.equals(entry.lootType())) {
                continue;
            }
            matches.add(entry);
        }
        matches.sort(Comparator.comparing(LabLootEntry::name, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(LabLootEntry::id));
        return matches;
    }

    public record LabLootEntry(ResourceLocation id, String name, String lootType, boolean kubejs, String normalizedId,
            String normalizedName) {
        public boolean matches(String normalizedQuery) {
            return normalizedId.contains(normalizedQuery) || normalizedName.contains(normalizedQuery);
        }
    }
}
