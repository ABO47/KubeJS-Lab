package com.abo47.kubejslab.client.ui.loot;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import com.abo47.kubejslab.client.ui.picker.LabSearchNormalizer;
import com.abo47.kubejslab.loot.model.LabLootFieldValues;
import com.abo47.kubejslab.loot.runtime.LabLootService;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


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

    public static LabLootFieldValues prefillValues(ResourceLocation id, String lootType) {
        JsonObject json = loadLootTable(lootTableId(id, lootType));
        if (json == null || !json.has("pools") || !json.get("pools").isJsonArray()) {
            return withTarget(id);
        }
        JsonArray pools = json.getAsJsonArray("pools");
        if (pools.isEmpty()) {
            return withTarget(id);
        }
        JsonObject pool = pools.get(0).getAsJsonObject();

        String rollsType = "constant";
        float rollsValue = 1f;
        float rollsMin = 0f;
        float rollsMax = 0f;
        JsonElement rollsEl = pool.get("rolls");
        if (rollsEl != null && rollsEl.isJsonObject()) {
            JsonObject rollsObj = rollsEl.getAsJsonObject();
            if (rollsObj.has("min") && rollsObj.has("max")) {
                rollsType = "uniform";
                rollsMin = rollsObj.get("min").getAsFloat();
                rollsMax = rollsObj.get("max").getAsFloat();
            } else if (rollsObj.has("value")) {
                rollsValue = rollsObj.get("value").getAsFloat();
            }
        } else if (rollsEl != null && rollsEl.isJsonPrimitive()) {
            rollsValue = rollsEl.getAsFloat();
        }

        String entryItem = "";
        int entryWeight = 1;
        String countType = "constant";
        float countValue = 1f;
        float countMin = 0f;
        float countMax = 0f;
        float randomChance = 1f;

        if (pool.has("entries") && pool.get("entries").isJsonArray()) {
            JsonArray entries = pool.getAsJsonArray("entries");
            for (int i = 0; i < entries.size(); i++) {
                JsonObject entry = entries.get(i).getAsJsonObject();
                String type = entry.has("type") ? entry.get("type").getAsString() : "";
                if (!"minecraft:item".equals(type)) {
                    continue;
                }
                if (entry.has("name")) {
                    entryItem = entry.get("name").getAsString();
                }
                if (entry.has("weight")) {
                    entryWeight = entry.get("weight").getAsInt();
                }
                if (entry.has("functions") && entry.get("functions").isJsonArray()) {
                    for (JsonElement fn : entry.getAsJsonArray("functions")) {
                        JsonObject f = fn.getAsJsonObject();
                        String fname = f.has("function") ? f.get("function").getAsString() : "";
                        if ("minecraft:set_count".equals(fname) && f.has("count")) {
                            countMin = f.get("count").getAsFloat();
                        }
                    }
                }
                if (entry.has("conditions") && entry.get("conditions").isJsonArray()) {
                    for (JsonElement cond : entry.getAsJsonArray("conditions")) {
                        JsonObject c = cond.getAsJsonObject();
                        String cname = c.has("condition") ? c.get("condition").getAsString() : "";
                        if ("minecraft:random_chance".equals(cname) && c.has("chance")) {
                            randomChance = c.get("chance").getAsFloat();
                        }
                    }
                }
                break;
            }
        }

        if (countMin > 0f && countMin != 1f) {
            countType = "uniform";
            countMax = countMin;
        } else {
            countValue = Math.max(1f, countMin);
        }

        LabLootFieldValues d = LabLootFieldValues.defaults();
        return new LabLootFieldValues(id.toString(), "", rollsType, rollsValue, rollsMin, rollsMax, d.poolRollsN(),
                d.poolRollsP(), "item", entryItem, "", "", "constant", countValue, countMin, countMax, entryWeight, 0,
                d.poolSurvivesExplosion(), randomChance, d.poolKilledByPlayer(), d.poolFurnaceSmelt(),
                d.poolLootingEnchant(), d.poolLootingCount(), d.poolLootingLimit());
    }

    private static LabLootFieldValues withTarget(ResourceLocation id) {
        LabLootFieldValues d = LabLootFieldValues.defaults();
        return new LabLootFieldValues(id.toString(), "", d.poolRollsType(), d.poolRollsValue(), d.poolRollsMin(),
                d.poolRollsMax(), d.poolRollsN(), d.poolRollsP(), d.entryType(), d.entryItem(), d.entryTag(),
                d.entryLootTable(), d.entryCountType(), d.entryCountValue(), d.entryCountMin(), d.entryCountMax(),
                d.entryWeight(), d.entryQuality(), d.poolSurvivesExplosion(), d.poolRandomChance(),
                d.poolKilledByPlayer(), d.poolFurnaceSmelt(), d.poolLootingEnchant(), d.poolLootingCount(),
                d.poolLootingLimit());
    }

    private static ResourceLocation lootTableId(ResourceLocation id, String lootType) {
        if (LabLootService.LOOT_TYPE_BLOCK.equals(lootType)) {
            return new ResourceLocation(id.getNamespace(), "blocks/" + id.getPath());
        }
        if (LabLootService.LOOT_TYPE_ENTITY.equals(lootType)) {
            return new ResourceLocation(id.getNamespace(), "entities/" + id.getPath());
        }
        if (LabLootService.LOOT_TYPE_FISHING.equals(lootType)) {
            return new ResourceLocation("minecraft", "gameplay/fishing");
        }
        if (LabLootService.LOOT_TYPE_GIFT.equals(lootType)) {
            return new ResourceLocation("minecraft", "gameplay/hero_of_the_village");
        }
        return id;
    }

    private static JsonObject loadLootTable(ResourceLocation table) {
        if (table == null || Minecraft.getInstance() == null) {
            return null;
        }
        try {
            ResourceLocation file = new ResourceLocation(table.getNamespace(), "loot_tables/" + table.getPath() + ".json");
            var resource = Minecraft.getInstance().getResourceManager().getResource(file);
            if (resource.isEmpty()) {
                return null;
            }
            try (InputStream in = resource.get().open()) {
                return JsonParser.parseString(new String(in.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
            }
        } catch (Exception ignored) {
            return null;
        }
    }

    public record LabLootEntry(ResourceLocation id, String name, String lootType, boolean kubejs, String normalizedId,
            String normalizedName) {
        public boolean matches(String normalizedQuery) {
            return normalizedId.contains(normalizedQuery) || normalizedName.contains(normalizedQuery);
        }
    }
}