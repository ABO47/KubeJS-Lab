package com.abo47.kubejslab.loot.runtime;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.world.entity.EntityType;

import com.abo47.kubejslab.loot.model.LabLootEntryValues;
import com.abo47.kubejslab.loot.model.LabLootFieldValues;
import com.abo47.kubejslab.loot.model.LabLootPoolValues;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public final class LabLootPrefill {
    public static final int MAX_POOLS = 6;
    public static final int MAX_ENTRIES = 6;

    private LabLootPrefill() {
    }

    public static LabLootFieldValues prefill(MinecraftServer server, ResourceLocation id, String lootType) {
        JsonObject json = loadServerTable(server, lootTableId(id, lootType));
        if (json == null || !json.has("pools") || !json.get("pools").isJsonArray()) {
            return blankFor(id);
        }
        JsonArray pools = json.getAsJsonArray("pools");
        if (pools.isEmpty()) {
            return blankFor(id);
        }
        List<LabLootPoolValues> poolValues = new ArrayList<>();
        int poolCount = Math.min(pools.size(), MAX_POOLS);
        for (int i = 0; i < poolCount; i++) {
            if (!pools.get(i).isJsonObject()) {
                continue;
            }
            poolValues.add(parsePool(pools.get(i).getAsJsonObject()));
        }
        if (poolValues.isEmpty()) {
            return blankFor(id);
        }
        return new LabLootFieldValues(id.toString(), "", poolValues);
    }

    public static LabLootFieldValues blankFor(ResourceLocation id) {
        LabLootFieldValues defaults = LabLootFieldValues.defaults();
        return new LabLootFieldValues(id.toString(), defaults.customId(), defaults.pools());
    }

    public static ResourceLocation lootTableId(ResourceLocation id, String lootType) {
        if (LabLootService.LOOT_TYPE_BLOCK.equals(lootType)) {
            return new ResourceLocation(id.getNamespace(), "blocks/" + id.getPath());
        }
        if (LabLootService.LOOT_TYPE_ENTITY.equals(lootType)) {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(id);
            if (type != null && type.getDefaultLootTable() != null) {
                return type.getDefaultLootTable();
            }
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

    private static JsonObject loadServerTable(MinecraftServer server, ResourceLocation table) {
        if (server == null || table == null) {
            return null;
        }
        PackRepository repository = server.getPackRepository();
        JsonObject found = null;
        ResourceLocation file = new ResourceLocation(table.getNamespace(), "loot_tables/" + table.getPath() + ".json");
        for (String packId : repository.getSelectedIds()) {
            Pack pack = repository.getPack(packId);
            if (pack == null) {
                continue;
            }
            try (PackResources resources = pack.open()) {
                IoSupplier<InputStream> supplier = resources.getResource(PackType.SERVER_DATA, file);
                if (supplier == null) {
                    continue;
                }
                try (InputStream in = supplier.get()) {
                    found = JsonParser.parseString(new String(in.readAllBytes(), StandardCharsets.UTF_8))
                            .getAsJsonObject();
                }
            } catch (Exception ignored) {
            }
        }
        return found;
    }

    private static LabLootPoolValues parsePool(JsonObject pool) {
        String rollsType = "constant";
        float rollsValue = 1f;
        float rollsMin = 0f;
        float rollsMax = 0f;
        int rollsN = 0;
        float rollsP = 0.5f;
        JsonElement rollsEl = pool.get("rolls");
        if (rollsEl != null && rollsEl.isJsonPrimitive()) {
            rollsValue = rollsEl.getAsFloat();
        } else if (rollsEl != null && rollsEl.isJsonObject()) {
            JsonObject rollsObj = rollsEl.getAsJsonObject();
            String providerType = rollsObj.has("type") ? rollsObj.get("type").getAsString() : "";
            if (providerType.contains("binomial") || (rollsObj.has("n") && rollsObj.has("p"))) {
                rollsType = "binomial";
                rollsN = rollsObj.has("n") ? rollsObj.get("n").getAsInt() : 0;
                rollsP = rollsObj.has("p") ? rollsObj.get("p").getAsFloat() : 0.5f;
            } else if (rollsObj.has("min") && rollsObj.has("max")) {
                rollsType = "uniform";
                rollsMin = rollsObj.get("min").getAsFloat();
                rollsMax = rollsObj.get("max").getAsFloat();
            } else if (rollsObj.has("value")) {
                rollsValue = rollsObj.get("value").getAsFloat();
            }
        }

        boolean survivesExplosion = false;
        float randomChance = 1f;
        boolean killedByPlayer = false;
        boolean furnaceSmelt = false;
        boolean lootingEnchant = false;
        float lootingCount = 0f;
        int lootingLimit = 0;
        if (pool.has("conditions") && pool.get("conditions").isJsonArray()) {
            for (JsonElement condEl : pool.getAsJsonArray("conditions")) {
                if (!condEl.isJsonObject()) {
                    continue;
                }
                JsonObject c = condEl.getAsJsonObject();
                String cname = c.has("condition") ? c.get("condition").getAsString() : "";
                if ("minecraft:survives_explosion".equals(cname)) {
                    survivesExplosion = true;
                } else if ("minecraft:random_chance".equals(cname) && c.has("chance")) {
                    randomChance = c.get("chance").getAsFloat();
                } else if ("minecraft:killed_by_player".equals(cname)) {
                    killedByPlayer = true;
                }
            }
        }
        if (pool.has("functions") && pool.get("functions").isJsonArray()) {
            for (JsonElement fnEl : pool.getAsJsonArray("functions")) {
                if (!fnEl.isJsonObject()) {
                    continue;
                }
                JsonObject f = fnEl.getAsJsonObject();
                String fname = f.has("function") ? f.get("function").getAsString() : "";
                if ("minecraft:furnace_smelt".equals(fname)) {
                    furnaceSmelt = true;
                } else if ("minecraft:enchant_with_levels".equals(fname)
                        || "minecraft:looting_enchant".equals(fname)) {
                    lootingEnchant = true;
                    if (f.has("count")) {
                        lootingCount = asFloat(f.get("count"), 0f);
                    }
                    if (f.has("limit")) {
                        lootingLimit = (int) asFloat(f.get("limit"), 0f);
                    }
                }
            }
        }

        List<LabLootEntryValues> entryValues = new ArrayList<>();
        if (pool.has("entries") && pool.get("entries").isJsonArray()) {
            for (JsonElement entryEl : pool.getAsJsonArray("entries")) {
                if (entryValues.size() >= MAX_ENTRIES) {
                    break;
                }
                if (!entryEl.isJsonObject()) {
                    continue;
                }
                collectEntries(entryEl.getAsJsonObject(), entryValues);
            }
        }
        if (entryValues.isEmpty()) {
            entryValues.add(LabLootEntryValues.defaults());
        }
        return new LabLootPoolValues(rollsType, rollsValue, rollsMin, rollsMax, rollsN, rollsP, survivesExplosion,
                randomChance, killedByPlayer, furnaceSmelt, lootingEnchant, lootingCount, lootingLimit, entryValues);
    }

    private static void collectEntries(JsonObject entry, List<LabLootEntryValues> out) {
        if (out.size() >= MAX_ENTRIES) {
            return;
        }
        String rawType = entry.has("type") ? entry.get("type").getAsString() : "";
        if ((rawType.endsWith("alternatives") || rawType.endsWith("sequence") || rawType.endsWith("group"))
                && entry.has("children") && entry.get("children").isJsonArray()) {
            for (JsonElement childEl : entry.getAsJsonArray("children")) {
                if (out.size() >= MAX_ENTRIES) {
                    return;
                }
                if (!childEl.isJsonObject()) {
                    continue;
                }
                collectEntries(childEl.getAsJsonObject(), out);
            }
            return;
        }
        out.add(parseEntry(entry));
    }

    private static LabLootEntryValues parseEntry(JsonObject entry) {
        String rawType = entry.has("type") ? entry.get("type").getAsString() : "";
        String type = "item";
        String item = "";
        String tag = "";
        String lootTable = "";
        if ("minecraft:tag".equals(rawType)) {
            type = "tag";
            tag = entry.has("name") ? entry.get("name").getAsString() : "";
        } else if ("minecraft:loot_table".equals(rawType)) {
            type = "loot_table";
            lootTable = entry.has("name") ? entry.get("name").getAsString() : "";
        } else if ("minecraft:empty".equals(rawType)) {
            type = "empty";
        } else {
            type = "item";
            item = entry.has("name") ? entry.get("name").getAsString() : "";
        }
        int weight = entry.has("weight") ? entry.get("weight").getAsInt() : 1;
        int quality = entry.has("quality") ? entry.get("quality").getAsInt() : 0;

        String countType = "constant";
        float countValue = 1f;
        float countMin = 0f;
        float countMax = 0f;
        if (entry.has("functions") && entry.get("functions").isJsonArray()) {
            for (JsonElement fnEl : entry.getAsJsonArray("functions")) {
                if (!fnEl.isJsonObject()) {
                    continue;
                }
                JsonObject f = fnEl.getAsJsonObject();
                String fname = f.has("function") ? f.get("function").getAsString() : "";
                if (!"minecraft:set_count".equals(fname) || !f.has("count")) {
                    continue;
                }
                JsonElement countEl = f.get("count");
                if (countEl.isJsonPrimitive()) {
                    countValue = Math.max(1f, countEl.getAsFloat());
                } else if (countEl.isJsonObject()) {
                    JsonObject countObj = countEl.getAsJsonObject();
                    String countProvider = countObj.has("type") ? countObj.get("type").getAsString() : "";
                    if (countObj.has("min") && countObj.has("max")) {
                        countType = "uniform";
                        countMin = countObj.get("min").getAsFloat();
                        countMax = countObj.get("max").getAsFloat();
                    } else if (countObj.has("value")) {
                        countValue = Math.max(1f, countObj.get("value").getAsFloat());
                    } else if (countProvider.isBlank() && countObj.has("min")) {
                        countMin = countObj.get("min").getAsFloat();
                        countMax = countMin;
                    }
                }
            }
        }
        return new LabLootEntryValues(type, item, tag, lootTable, countType, countValue, countMin, countMax, weight,
                quality);
    }

    private static float asFloat(JsonElement el, float fallback) {
        try {
            if (el.isJsonPrimitive()) {
                return el.getAsFloat();
            }
            if (el.isJsonObject()) {
                JsonObject obj = el.getAsJsonObject();
                if (obj.has("value")) {
                    return obj.get("value").getAsFloat();
                }
                if (obj.has("min")) {
                    return obj.get("min").getAsFloat();
                }
            }
        } catch (Exception ignored) {
        }
        return fallback;
    }
}
