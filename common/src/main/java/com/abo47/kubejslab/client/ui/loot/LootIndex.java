package com.abo47.kubejslab.client.ui.loot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import com.abo47.kubejslab.client.ui.picker.SearchNormalizer;
import com.abo47.kubejslab.loot.runtime.LootService;


public final class LootIndex {
    private static final Map<ResourceLocation, LootEntry> ENTRIES = new HashMap<>();

    static {
        for (ResourceLocation id : BuiltInRegistries.BLOCK.keySet()) {
            Block block = BuiltInRegistries.BLOCK.get(id);
            if (block == null || block == Blocks.AIR) {
                continue;
            }
            ENTRIES.put(id, new LootEntry(id, id.getPath(), LootService.LOOT_TYPE_BLOCK,
                    id.getNamespace().equals("kubejs"),
                    SearchNormalizer.normalizeUserSearch(id.toString()),
                    SearchNormalizer.normalizeUserSearch(id.getPath())));
        }
        for (ResourceLocation id : BuiltInRegistries.ENTITY_TYPE.keySet()) {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(id);
            if (type == null) {
                continue;
            }
            ENTRIES.put(id, new LootEntry(id, id.getPath(), LootService.LOOT_TYPE_ENTITY,
                    id.getNamespace().equals("kubejs"),
                    SearchNormalizer.normalizeUserSearch(id.toString()),
                    SearchNormalizer.normalizeUserSearch(id.getPath())));
        }
    }

    private static final Set<ResourceLocation> SCANNED = new HashSet<>();

    private LootIndex() {
    }

    public static void setScannedTables(List<ResourceLocation> tables) {
        for (ResourceLocation id : SCANNED) {
            ENTRIES.remove(id);
        }
        SCANNED.clear();
        if (tables == null) {
            return;
        }
        for (ResourceLocation id : tables) {
            String path = id.getPath();
            if (path.startsWith("blocks/") || path.startsWith("entities/")) {
                continue;
            }
            String type = LootService.LOOT_TYPE_GENERIC;
            if (path.startsWith("chests/")) {
                type = LootService.LOOT_TYPE_CHEST;
            } else if (path.equals("gameplay/fishing") || path.startsWith("gameplay/fishing/")) {
                type = LootService.LOOT_TYPE_FISHING;
            } else if (path.equals("gameplay/hero_of_the_village")) {
                type = LootService.LOOT_TYPE_GIFT;
            }
            ENTRIES.put(id, new LootEntry(id, path, type, id.getNamespace().equals("kubejs"),
                    SearchNormalizer.normalizeUserSearch(id.toString()),
                    SearchNormalizer.normalizeUserSearch(path)));
            SCANNED.add(id);
        }
    }

    public static List<LootEntry> search(String query, boolean kubejsOnly) {
        return search(query, kubejsOnly, null);
    }

    public static List<LootEntry> search(String query, boolean kubejsOnly, String lootTypeFilter) {
        String normalizedQuery = SearchNormalizer.normalizeQuery(query);
        List<LootEntry> matches = new ArrayList<>();
        for (LootEntry entry : ENTRIES.values()) {
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
        matches.sort(Comparator.comparing(LootEntry::name, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(LootEntry::id));
        return matches;
    }

    public static List<LootEntry> entriesForType(String lootType) {
        List<LootEntry> matches = new ArrayList<>();
        for (LootEntry entry : ENTRIES.values()) {
            if (lootType != null && !lootType.isBlank() && !lootType.equals(entry.lootType())) {
                continue;
            }
            matches.add(entry);
        }
        matches.sort(Comparator.comparing(LootEntry::name, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(LootEntry::id));
        return matches;
    }

    public record LootEntry(ResourceLocation id, String name, String lootType, boolean kubejs, String normalizedId,
            String normalizedName) {
        public boolean matches(String normalizedQuery) {
            return normalizedId.contains(normalizedQuery) || normalizedName.contains(normalizedQuery);
        }
    }
}
