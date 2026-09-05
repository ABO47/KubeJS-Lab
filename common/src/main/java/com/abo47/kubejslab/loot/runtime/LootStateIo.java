package com.abo47.kubejslab.loot.runtime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.loot.model.LootAction;
import com.abo47.kubejslab.loot.model.LootEntryValues;
import com.abo47.kubejslab.loot.model.LootFieldValues;
import com.abo47.kubejslab.loot.model.LootPoolValues;
import com.abo47.kubejslab.loot.model.LootStatus;
import com.abo47.kubejslab.workspace.JsonStateFile;
import com.abo47.kubejslab.workspace.WorkspacePaths;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class LootStateIo {

    private LootStateIo() {
    }

    static Map<ResourceLocation, LootSaveEntry> load() {
        Map<ResourceLocation, LootSaveEntry> loaded = new LinkedHashMap<>();

        JsonObject root = JsonStateFile.load(WorkspacePaths.lootStateFile());
        if (root == null) {
            return loaded;
        }
        for (String key : root.keySet()) {
            try {
                JsonObject obj = root.getAsJsonObject(key);
                if (!obj.has("values")) {
                    continue;
                }
                ResourceLocation id = new ResourceLocation(key);
                LootStatus status = LootStatus.valueOf(obj.get("status").getAsString());
                String lootType = obj.has("lootType") ? obj.get("lootType").getAsString() : LootService.LOOT_TYPE_BLOCK;
                String name = obj.has("name") ? obj.get("name").getAsString() : "";
                boolean wasModified = obj.has("wasModified") && obj.get("wasModified").getAsBoolean();
                LootFieldValues values = obj.has("values") ? readValues(obj.getAsJsonObject("values"))
                        : LootFieldValues.defaults();
                List<String> tags = new ArrayList<>();
                if (obj.has("tags")) {
                    for (JsonElement el : obj.getAsJsonArray("tags")) {
                        tags.add(el.getAsString());
                    }
                }
                List<LootAction> actions = new ArrayList<>();
                if (obj.has("actions")) {
                    for (JsonElement el : obj.getAsJsonArray("actions")) {
                        actions.add(LootAction.valueOf(el.getAsString()));
                    }
                }
                loaded.put(id, new LootSaveEntry(lootType, status, name, wasModified, values, tags, actions));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
            return loaded;
    }

    static void save(Map<ResourceLocation, LootSaveEntry> states) throws IOException {
        JsonObject root = new JsonObject();
        for (Map.Entry<ResourceLocation, LootSaveEntry> item : states.entrySet()) {
            LootSaveEntry entry = item.getValue();
            JsonObject obj = new JsonObject();
            obj.addProperty("status", entry.status().name());
            obj.addProperty("lootType", entry.lootType());
            obj.addProperty("name", entry.name());
            obj.addProperty("wasModified", entry.wasModified());
            JsonObject values = new JsonObject();
            writeValues(values, entry.values());
            obj.add("values", values);
            JsonArray tags = new JsonArray();
            for (String tag : entry.tags()) {
                tags.add(tag);
            }
            obj.add("tags", tags);
            JsonArray actions = new JsonArray();
            for (LootAction action : entry.actions()) {
                actions.add(action.name());
            }
            obj.add("actions", actions);
            root.add(item.getKey().toString(), obj);
        }
        JsonStateFile.save(WorkspacePaths.lootStateFile(), root);
    }

    static LootFieldValues readValues(JsonObject obj) {
        String targetId = obj.has("targetId") ? obj.get("targetId").getAsString() : "";
        String customId = obj.has("customId") ? obj.get("customId").getAsString() : "";
        List<LootPoolValues> pools = new ArrayList<>();
        if (obj.has("pools") && obj.get("pools").isJsonArray()) {
            for (JsonElement poolEl : obj.getAsJsonArray("pools")) {
                if (!poolEl.isJsonObject()) {
                    continue;
                }
                pools.add(readPool(poolEl.getAsJsonObject()));
            }
        } else if (obj.has("poolRollsType")) {
            pools.add(readLegacyPool(obj));
        }
        if (pools.isEmpty()) {
            pools.add(LootPoolValues.defaults());
        }
        return new LootFieldValues(targetId, customId, pools, 0, 0);
    }

    static LootPoolValues readPool(JsonObject obj) {
        List<LootEntryValues> entries = new ArrayList<>();
        if (obj.has("entries") && obj.get("entries").isJsonArray()) {
            for (JsonElement entryEl : obj.getAsJsonArray("entries")) {
                if (!entryEl.isJsonObject()) {
                    continue;
                }
                JsonObject e = entryEl.getAsJsonObject();
                entries.add(new LootEntryValues(
                        e.has("type") ? e.get("type").getAsString() : "item",
                        e.has("item") ? e.get("item").getAsString() : "",
                        e.has("tag") ? e.get("tag").getAsString() : "",
                        e.has("lootTable") ? e.get("lootTable").getAsString() : "",
                        e.has("countType") ? e.get("countType").getAsString() : "constant",
                        e.has("countValue") ? e.get("countValue").getAsFloat() : 1f,
                        e.has("countMin") ? e.get("countMin").getAsFloat() : 0f,
                        e.has("countMax") ? e.get("countMax").getAsFloat() : 0f,
                        e.has("weight") ? e.get("weight").getAsInt() : 1,
                        e.has("quality") ? e.get("quality").getAsInt() : 0,
                        e.has("lootBonusMin") ? e.get("lootBonusMin").getAsFloat() : 0f,
                        e.has("lootBonusMax") ? e.get("lootBonusMax").getAsFloat() : 0f,
                        readNotes(e),
                        e.has("toolRequirement") ? e.get("toolRequirement").getAsString() : "",
                        e.has("entryKilledByPlayer") && e.get("entryKilledByPlayer").getAsBoolean(),
                        e.has("entryChance") ? e.get("entryChance").getAsFloat() : 1f,
                        e.has("entryChanceLooting") ? e.get("entryChanceLooting").getAsFloat() : 0f,
                        e.has("alternativeGroup") ? e.get("alternativeGroup").getAsInt() : 0,
                        e.has("fortuneBonus") && e.get("fortuneBonus").getAsBoolean(),
                        e.has("lootBonusLimit") ? e.get("lootBonusLimit").getAsInt() : 0,
                        e.has("explosionDecay") && e.get("explosionDecay").getAsBoolean(),
                        e.has("extraConditions") ? e.get("extraConditions").getAsString() : "",
                        e.has("extraFunctions") ? e.get("extraFunctions").getAsString() : ""));
            }
        }
        if (entries.isEmpty()) {
            entries.add(LootEntryValues.defaults());
        }
        return new LootPoolValues(
                obj.has("rollsType") ? obj.get("rollsType").getAsString() : "constant",
                obj.has("rollsValue") ? obj.get("rollsValue").getAsFloat() : 1f,
                obj.has("rollsMin") ? obj.get("rollsMin").getAsFloat() : 0f,
                obj.has("rollsMax") ? obj.get("rollsMax").getAsFloat() : 0f,
                obj.has("rollsN") ? obj.get("rollsN").getAsInt() : 0,
                obj.has("rollsP") ? obj.get("rollsP").getAsFloat() : 0.5f,
                !obj.has("survivesExplosion") || obj.get("survivesExplosion").getAsBoolean(),
                obj.has("randomChance") ? obj.get("randomChance").getAsFloat() : 1f,
                obj.has("killedByPlayer") && obj.get("killedByPlayer").getAsBoolean(),
                obj.has("furnaceSmelt") && obj.get("furnaceSmelt").getAsBoolean(),
                obj.has("lootingEnchant") && obj.get("lootingEnchant").getAsBoolean(),
                obj.has("lootingCount") ? obj.get("lootingCount").getAsFloat() : 0f,
                obj.has("lootingLimit") ? obj.get("lootingLimit").getAsInt() : 0,
                entries,
                obj.has("bonusRolls") ? obj.get("bonusRolls").getAsFloat() : 0f,
                readPoolNotes(obj));
    }

    static LootPoolValues readLegacyPool(JsonObject obj) {
        return new LootPoolValues(
                obj.get("poolRollsType").getAsString(),
                obj.get("poolRollsValue").getAsFloat(),
                obj.get("poolRollsMin").getAsFloat(),
                obj.get("poolRollsMax").getAsFloat(),
                obj.get("poolRollsN").getAsInt(),
                obj.get("poolRollsP").getAsFloat(),
                obj.get("poolSurvivesExplosion").getAsBoolean(),
                obj.get("poolRandomChance").getAsFloat(),
                obj.get("poolKilledByPlayer").getAsBoolean(),
                obj.get("poolFurnaceSmelt").getAsBoolean(),
                obj.get("poolLootingEnchant").getAsBoolean(),
                obj.get("poolLootingCount").getAsFloat(),
                obj.get("poolLootingLimit").getAsInt(),
                List.of(new LootEntryValues(
                        obj.get("entryType").getAsString(),
                        obj.get("entryItem").getAsString(),
                        obj.get("entryTag").getAsString(),
                        obj.get("entryLootTable").getAsString(),
                        obj.get("entryCountType").getAsString(),
                        obj.get("entryCountValue").getAsFloat(),
                        obj.get("entryCountMin").getAsFloat(),
                        obj.get("entryCountMax").getAsFloat(),
                        obj.get("entryWeight").getAsInt(),
                        obj.get("entryQuality").getAsInt(),
                        0f, 0f, List.of(), "", false, 1f, 0f, 0, false, 0, false, "", "")),
                0f, List.of());
    }

    static void writeValues(JsonObject obj, LootFieldValues v) {
        obj.addProperty("targetId", v.targetId());
        obj.addProperty("customId", v.customId());
        JsonArray pools = new JsonArray();
        for (LootPoolValues p : v.pools()) {
            JsonObject pool = new JsonObject();
            pool.addProperty("rollsType", p.rollsType());
            pool.addProperty("rollsValue", p.rollsValue());
            pool.addProperty("rollsMin", p.rollsMin());
            pool.addProperty("rollsMax", p.rollsMax());
            pool.addProperty("rollsN", p.rollsN());
            pool.addProperty("rollsP", p.rollsP());
            pool.addProperty("survivesExplosion", p.survivesExplosion());
            pool.addProperty("randomChance", p.randomChance());
            pool.addProperty("killedByPlayer", p.killedByPlayer());
            pool.addProperty("furnaceSmelt", p.furnaceSmelt());
            pool.addProperty("lootingEnchant", p.lootingEnchant());
            pool.addProperty("lootingCount", p.lootingCount());
            pool.addProperty("lootingLimit", p.lootingLimit());
            pool.addProperty("bonusRolls", p.bonusRolls());
            JsonArray poolNotes = new JsonArray();
            for (String note : p.poolConditionNotes()) {
                poolNotes.add(note);
            }
            pool.add("poolConditionNotes", poolNotes);
            JsonArray entries = new JsonArray();
            for (LootEntryValues e : p.entries()) {
                JsonObject entry = new JsonObject();
                entry.addProperty("type", e.type());
                entry.addProperty("item", e.item());
                entry.addProperty("tag", e.tag());
                entry.addProperty("lootTable", e.lootTable());
                entry.addProperty("countType", e.countType());
                entry.addProperty("countValue", e.countValue());
                entry.addProperty("countMin", e.countMin());
                entry.addProperty("countMax", e.countMax());
                entry.addProperty("weight", e.weight());
                entry.addProperty("quality", e.quality());
                entry.addProperty("lootBonusMin", e.lootBonusMin());
                entry.addProperty("lootBonusMax", e.lootBonusMax());
                JsonArray notes = new JsonArray();
                for (String note : e.conditionNotes()) {
                    notes.add(note);
                }
                entry.add("conditionNotes", notes);
                entry.addProperty("toolRequirement", e.toolRequirement());
                entry.addProperty("entryKilledByPlayer", e.entryKilledByPlayer());
                entry.addProperty("entryChance", e.entryChance());
                entry.addProperty("entryChanceLooting", e.entryChanceLooting());
                entry.addProperty("alternativeGroup", e.alternativeGroup());
                entry.addProperty("fortuneBonus", e.fortuneBonus());
                entry.addProperty("lootBonusLimit", e.lootBonusLimit());
                entry.addProperty("explosionDecay", e.explosionDecay());
                entry.addProperty("extraConditions", e.extraConditions());
                entry.addProperty("extraFunctions", e.extraFunctions());
                entries.add(entry);
            }
            pool.add("entries", entries);
            pools.add(pool);
        }
        obj.add("pools", pools);
    }

    static List<String> readNotes(JsonObject e) {
        List<String> notes = new ArrayList<>();
        if (e.has("conditionNotes") && e.get("conditionNotes").isJsonArray()) {
            for (JsonElement el : e.getAsJsonArray("conditionNotes")) {
                if (el.isJsonPrimitive() && notes.size() < 16) {
                    notes.add(el.getAsString());
                }
            }
        }
        return notes;
    }

    static List<String> readPoolNotes(JsonObject obj) {
        List<String> notes = new ArrayList<>();
        if (obj.has("poolConditionNotes") && obj.get("poolConditionNotes").isJsonArray()) {
            for (JsonElement el : obj.getAsJsonArray("poolConditionNotes")) {
                if (el.isJsonPrimitive() && notes.size() < 16) {
                    notes.add(el.getAsString());
                }
            }
        }
        return notes;
    }
}
