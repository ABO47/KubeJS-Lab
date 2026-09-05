package com.abo47.kubejslab.loot.runtime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.KubeJSLab;
import com.abo47.kubejslab.loot.model.LootEntryValues;
import com.abo47.kubejslab.loot.model.LootFieldValues;
import com.abo47.kubejslab.loot.model.LootPoolValues;
import com.abo47.kubejslab.loot.model.LootStatus;
import com.abo47.kubejslab.workspace.ScriptWriter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class LootScriptWriter {

    private LootScriptWriter() {
    }

    static void writeServerScript(Map<ResourceLocation, LootSaveEntry> states) throws IOException {
        if (states.isEmpty()) {
            ScriptWriter.write("server_scripts", "loot.js", "");
            return;
        }
        StringBuilder sb = new StringBuilder();
        Map<String, StringBuilder> typeBuffers = new HashMap<>();
        for (Map.Entry<ResourceLocation, LootSaveEntry> entry : states.entrySet()) {
            ResourceLocation id = entry.getKey();
            LootSaveEntry data = entry.getValue();
            String type = data.lootType();
            if (type == null || type.isBlank()) {
                continue;
            }
            StringBuilder typeBuf = typeBuffers.computeIfAbsent(type, k -> new StringBuilder());
            writeLootEntry(typeBuf, type, id, data);
        }
        for (Map.Entry<String, StringBuilder> entry : typeBuffers.entrySet()) {
            String type = entry.getKey();
            StringBuilder typeBuf = entry.getValue();
            if (typeBuf.length() == 0) {
                continue;
            }
            String eventName = eventNameFor(type);
            String methodName = methodNameFor(type);
            sb.append("ServerEvents.").append(eventName).append("(event => {\n");
            sb.append(typeBuf);
            sb.append("});\n\n");
        }
        ScriptWriter.write("server_scripts", "loot.js", sb.toString());
    }

    static void writeLootEntry(StringBuilder sb, String type, ResourceLocation id, LootSaveEntry data) {
        if (data.status() == LootStatus.DISABLED) {
            return;
        }
        LootFieldValues v = data.values();
        String targetId = v.targetId();
        if (targetId == null || targetId.isBlank()) {
            return;
        }
        String customId = v.customId();
        String entryId = customId == null || customId.isBlank() ? targetId : customId;
        if (v.pools().isEmpty()) {
            return;
        }
        String methodName = methodNameFor(type);
        if (LootService.LOOT_TYPE_ENTITY.equals(type)) {
            sb.append("    event.").append(methodName)
                    .append("(Utils.getRegistry('minecraft:entity_type').getValue('").append(js(entryId))
                    .append("'), loot => {\n");
        } else {
            sb.append("    event.").append(methodName).append("('").append(js(entryId)).append("', loot => {\n");
        }
        for (LootPoolValues pool : v.pools()) {
            StringBuilder poolBody = new StringBuilder();
            writeRolls(poolBody, pool);
            Set<Integer> emittedGroups = new HashSet<>();
            boolean wroteEntry = false;
            for (LootEntryValues entry : pool.entries()) {
                int group = Math.max(0, entry.alternativeGroup());
                if (group > 0) {
                    if (!emittedGroups.add(group)) {
                        continue;
                    }
                    int before = poolBody.length();
                    writeAlternatives(poolBody, pool, group);
                    wroteEntry |= poolBody.length() != before;
                } else {
                    int before = poolBody.length();
                    writeEntry(poolBody, entry);
                    wroteEntry |= poolBody.length() != before;
                }
            }
            if (!wroteEntry) {
                continue;
            }
            sb.append("        loot.addPool(pool => {\n");
            sb.append(poolBody);
            writePoolFunctions(sb, pool);
            writePoolConditions(sb, pool);
            sb.append("        });\n");
        }
        sb.append("    });\n");
    }

    public static boolean writesEntry(LootEntryValues e) {
        String type = e.type() == null ? "item" : e.type();
        return switch (type) {
            case "dynamic" -> e.item() != null && !e.item().isBlank();
            case "empty" -> true;
            case "tag" -> e.tag() != null && !e.tag().isBlank();
            case "loot_table" -> e.lootTable() != null && !e.lootTable().isBlank();
            default -> e.item() != null && !e.item().isBlank();
        };
    }

    public static boolean writesPool(LootPoolValues pool) {
        Set<Integer> seenGroups = new HashSet<>();
        for (LootEntryValues entry : pool.entries()) {
            int group = Math.max(0, entry.alternativeGroup());
            if (group > 0) {
                if (!seenGroups.add(group)) {
                    continue;
                }
                if (groupWrites(pool, group)) {
                    return true;
                }
            } else if (writesEntry(entry)) {
                return true;
            }
        }
        return false;
    }

    private static boolean groupWrites(LootPoolValues pool, int group) {
        for (LootEntryValues entry : pool.entries()) {
            if (Math.max(0, entry.alternativeGroup()) == group && childJson(entry) != null) {
                return true;
            }
        }
        return false;
    }

    static void writeRolls(StringBuilder sb, LootPoolValues p) {
        String type = p.rollsType() == null ? "constant" : p.rollsType();
        switch (type) {
            case "uniform" -> sb.append("            pool.setUniformRolls(").append(fmt(p.rollsMin())).append(", ")
                    .append(fmt(p.rollsMax())).append(");\n");
            case "binomial" -> sb.append("            pool.setBinomialRolls(").append(p.rollsN()).append(", ")
                    .append(fmt(p.rollsP())).append(");\n");
            default -> {
                if (p.rollsValue() != 1f) {
                    sb.append("            pool.setUniformRolls(").append(fmt(p.rollsValue())).append(", ")
                            .append(fmt(p.rollsValue())).append(");\n");
                }
            }
        }
    }

    static void writeEntry(StringBuilder sb, LootEntryValues e) {
        String type = e.type() == null ? "item" : e.type();
        switch (type) {
            case "dynamic" -> {
                String name = e.item();
                if (name == null || name.isBlank()) {
                    return;
                }
                sb.append("            pool.addEntry({type: 'minecraft:dynamic', name: '").append(js(name))
                        .append("'})");
            }
            case "empty" -> {
                sb.append("            pool.addEmpty(").append(e.weight()).append(")");
            }
            case "tag" -> {
                String tag = e.tag();
                if (tag == null || tag.isBlank()) {
                    return;
                }
                sb.append("            pool.addTag('").append(js(tag)).append("', true)");
            }
            case "loot_table" -> {
                String lootTable = e.lootTable();
                if (lootTable == null || lootTable.isBlank()) {
                    return;
                }
                sb.append("            pool.addLootTable('").append(js(lootTable)).append("')");
            }
            default -> {
                if (!writesEntry(e)) {
                    return;
                }
                sb.append("            pool.addItem(").append(itemStackExpr(e)).append(", ").append(e.weight());
                String countType = e.countType() == null ? "constant" : e.countType();
                if ("uniform".equals(countType)) {
                    if (e.countMin() != e.countMax()) {
                        sb.append(", [").append(fmt(e.countMin())).append(", ").append(fmt(e.countMax()))
                                .append("]");
                    } else {
                        sb.append(", ").append(fmt(e.countMin()));
                    }
                }
                sb.append(")");
                if (e.quality() > 0) {
                    sb.append(".quality(").append(e.quality()).append(")");
                }
            }
        }
        writeEntryChains(sb, e);
        sb.append(";\n");
    }

    static String itemStackExpr(LootEntryValues e) {
        StringBuilder expr = new StringBuilder("Item.of('").append(js(e.item())).append("'");
        String countType = e.countType() == null ? "constant" : e.countType();
        if ("constant".equals(countType) && e.countValue() > 1f) {
            expr.append(", ").append(fmt(e.countValue()));
        }
        return expr.append(")").toString();
    }

    static void writeEntryChains(StringBuilder sb, LootEntryValues e) {
        if (e.entryKilledByPlayer()) {
            sb.append(".killedByPlayer()");
        }
        if (e.entryChance() < 1f) {
            if (e.entryChanceLooting() > 0f) {
                sb.append(".randomChanceWithLooting(").append(fmt(e.entryChance())).append(", ")
                        .append(fmt(e.entryChanceLooting())).append(")");
            } else {
                sb.append(".randomChance(").append(fmt(e.entryChance())).append(")");
            }
        }
        if ("silk_touch".equals(e.toolRequirement()) || "fortune".equals(e.toolRequirement())) {
            sb.append(".addCondition({condition: 'minecraft:match_tool', predicate: {enchantments: ")
                    .append("[{enchantment: 'minecraft:").append(e.toolRequirement()).append("', levels: 1}]}})");
        }
        if (e.fortuneBonus()) {
            sb.append(".addFunction({function: 'minecraft:apply_bonus', enchantment: 'minecraft:fortune', ")
                    .append("formula: 'minecraft:ore_drops'})");
        }
        if (e.lootBonusMax() > 0f) {
            sb.append(".addFunction({function: 'minecraft:looting_enchant', count: ")
                    .append(bonusCountJson(e)).append(", limit: ").append(e.lootBonusLimit()).append("})");
        }
        if (e.explosionDecay()) {
            sb.append(".addFunction({function: 'minecraft:explosion_decay'})");
        }
        appendRawJson(sb, "addCondition", e.extraConditions());
        appendRawJson(sb, "addFunction", e.extraFunctions());
    }

    static String bonusCountJson(LootEntryValues e) {
        if (e.lootBonusMin() != e.lootBonusMax()) {
            return "{type: 'minecraft:uniform', min: " + fmt(e.lootBonusMin()) + ", max: "
                    + fmt(e.lootBonusMax()) + "}";
        }
        return fmt(e.lootBonusMax());
    }

    static void appendRawJson(StringBuilder sb, String method, String raw) {
        JsonArray elements = parseRawArray(raw);
        if (elements == null) {
            return;
        }
        for (JsonElement el : elements) {
            if (el.isJsonObject()) {
                sb.append(".").append(method).append("(").append(el).append(")");
            }
        }
    }

    static JsonArray parseRawArray(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            JsonElement parsed = JsonParser.parseString(raw.trim());
            if (parsed.isJsonArray()) {
                return parsed.getAsJsonArray();
            }
        } catch (Exception ignored) {
            KubeJSLab.LOGGER.warn("[LootService] ignoring invalid custom loot JSON: {}", raw);
        }
        return null;
    }

    static void writeAlternatives(StringBuilder sb, LootPoolValues pool, int group) {
        List<JsonObject> children = new ArrayList<>();
        for (LootEntryValues entry : pool.entries()) {
            if (Math.max(0, entry.alternativeGroup()) != group) {
                continue;
            }
            JsonObject child = childJson(entry);
            if (child != null) {
                children.add(child);
            }
        }
        if (children.isEmpty()) {
            return;
        }
        JsonObject alternatives = new JsonObject();
        alternatives.addProperty("type", "minecraft:alternatives");
        JsonArray array = new JsonArray();
        for (JsonObject child : children) {
            array.add(child);
        }
        alternatives.add("children", array);
        sb.append("            pool.addEntry(").append(alternatives).append(");\n");
    }

    static JsonObject childJson(LootEntryValues e) {
        String type = e.type() == null ? "item" : e.type();
        JsonObject o = new JsonObject();
        switch (type) {
            case "empty" -> o.addProperty("type", "minecraft:empty");
            case "tag" -> {
                if (e.tag() == null || e.tag().isBlank()) {
                    return null;
                }
                o.addProperty("type", "minecraft:tag");
                o.addProperty("name", e.tag());
                o.addProperty("expand", true);
            }
            case "loot_table" -> {
                if (e.lootTable() == null || e.lootTable().isBlank()) {
                    return null;
                }
                o.addProperty("type", "minecraft:loot_table");
                o.addProperty("name", e.lootTable());
            }
            case "dynamic" -> {
                if (e.item() == null || e.item().isBlank()) {
                    return null;
                }
                o.addProperty("type", "minecraft:dynamic");
                o.addProperty("name", e.item());
            }
            default -> {
                if (e.item() == null || e.item().isBlank()) {
                    return null;
                }
                o.addProperty("type", "minecraft:item");
                o.addProperty("name", e.item());
            }
        }
        o.addProperty("weight", e.weight());
        if (e.quality() > 0) {
            o.addProperty("quality", e.quality());
        }
        if ("item".equals(type)) {
            String countType = e.countType() == null ? "constant" : e.countType();
            JsonObject count = null;
            if ("uniform".equals(countType) && e.countMin() != e.countMax()) {
                count = new JsonObject();
                count.addProperty("type", "minecraft:uniform");
                count.addProperty("min", e.countMin());
                count.addProperty("max", e.countMax());
            } else if (("uniform".equals(countType) && e.countMin() > 1f)
                    || ("constant".equals(countType) && e.countValue() > 1f)) {
                float value = "uniform".equals(countType) ? e.countMin() : e.countValue();
                count = new JsonObject();
                count.addProperty("type", "minecraft:constant");
                count.addProperty("value", value);
            }
            if (count != null) {
                JsonObject fn = new JsonObject();
                fn.addProperty("function", "minecraft:set_count");
                fn.add("count", count);
                JsonArray functions = new JsonArray();
                functions.add(fn);
                o.add("functions", functions);
            }
        }
        JsonArray conditions = entryConditionsJson(e);
        JsonArray extraConditions = parseRawArray(e.extraConditions());
        if (extraConditions != null) {
            for (JsonElement el : extraConditions) {
                if (el.isJsonObject()) {
                    conditions.add(el);
                }
            }
        }
        if (conditions.size() > 0) {
            o.add("conditions", conditions);
        }
        JsonArray functions = o.has("functions") ? o.getAsJsonArray("functions") : new JsonArray();
        if (e.fortuneBonus()) {
            JsonObject fn = new JsonObject();
            fn.addProperty("function", "minecraft:apply_bonus");
            fn.addProperty("enchantment", "minecraft:fortune");
            fn.addProperty("formula", "minecraft:ore_drops");
            functions.add(fn);
        }
        if (e.lootBonusMax() > 0f) {
            JsonObject fn = new JsonObject();
            fn.addProperty("function", "minecraft:looting_enchant");
            if (e.lootBonusMin() != e.lootBonusMax()) {
                JsonObject count = new JsonObject();
                count.addProperty("type", "minecraft:uniform");
                count.addProperty("min", e.lootBonusMin());
                count.addProperty("max", e.lootBonusMax());
                fn.add("count", count);
            } else {
                fn.addProperty("count", e.lootBonusMax());
            }
            fn.addProperty("limit", e.lootBonusLimit());
            functions.add(fn);
        }
        if (e.explosionDecay()) {
            JsonObject fn = new JsonObject();
            fn.addProperty("function", "minecraft:explosion_decay");
            functions.add(fn);
        }
        JsonArray extraFunctions = parseRawArray(e.extraFunctions());
        if (extraFunctions != null) {
            for (JsonElement el : extraFunctions) {
                if (el.isJsonObject()) {
                    functions.add(el);
                }
            }
        }
        if (functions.size() > 0) {
            o.add("functions", functions);
        }
        return o;
    }

    static JsonArray entryConditionsJson(LootEntryValues e) {
        JsonArray conditions = new JsonArray();
        if (e.entryKilledByPlayer()) {
            JsonObject killed = new JsonObject();
            killed.addProperty("condition", "minecraft:killed_by_player");
            conditions.add(killed);
        }
        if (e.entryChance() < 1f) {
            if (e.entryChanceLooting() > 0f) {
                JsonObject chance = new JsonObject();
                chance.addProperty("condition", "minecraft:random_chance_with_looting");
                chance.addProperty("chance", e.entryChance());
                chance.addProperty("looting_multiplier", e.entryChanceLooting());
                conditions.add(chance);
            } else {
                JsonObject chance = new JsonObject();
                chance.addProperty("condition", "minecraft:random_chance");
                chance.addProperty("chance", e.entryChance());
                conditions.add(chance);
            }
        }
        if ("silk_touch".equals(e.toolRequirement()) || "fortune".equals(e.toolRequirement())) {
            JsonObject predicate = new JsonObject();
            JsonArray enchantments = new JsonArray();
            JsonObject enchant = new JsonObject();
            enchant.addProperty("enchantment", "minecraft:" + e.toolRequirement());
            enchant.addProperty("levels", 1);
            enchantments.add(enchant);
            predicate.add("enchantments", enchantments);
            JsonObject tool = new JsonObject();
            tool.addProperty("condition", "minecraft:match_tool");
            tool.add("predicate", predicate);
            conditions.add(tool);
        }
        return conditions;
    }

    static void writePoolFunctions(StringBuilder sb, LootPoolValues p) {
        if (p.survivesExplosion()) {
            sb.append("            pool.survivesExplosion();\n");
        }
        if (p.furnaceSmelt()) {
            sb.append("            pool.furnaceSmelt();\n");
        }
        if (p.randomChance() < 1f) {
            sb.append("            pool.randomChance(").append(fmt(p.randomChance())).append(");\n");
        }
        if (p.lootingEnchant()) {
            sb.append("            pool.lootingEnchant(").append(fmt(p.lootingCount())).append(", ")
                    .append(p.lootingLimit()).append(");\n");
        }
    }

    static void writePoolConditions(StringBuilder sb, LootPoolValues p) {
        if (p.killedByPlayer()) {
            sb.append("            pool.killedByPlayer();\n");
        }
    }

    static String eventNameFor(String type) {
        return switch (type) {
            case LootService.LOOT_TYPE_BLOCK -> "blockLootTables";
            case LootService.LOOT_TYPE_ENTITY -> "entityLootTables";
            case LootService.LOOT_TYPE_CHEST -> "chestLootTables";
            case LootService.LOOT_TYPE_FISHING -> "fishingLootTables";
            case LootService.LOOT_TYPE_GIFT -> "giftLootTables";
            default -> "genericLootTables";
        };
    }

    static String methodNameFor(String type) {
        return switch (type) {
            case LootService.LOOT_TYPE_BLOCK -> "addBlock";
            case LootService.LOOT_TYPE_ENTITY -> "addEntity";
            case LootService.LOOT_TYPE_CHEST -> "addChest";
            case LootService.LOOT_TYPE_FISHING -> "addFishing";
            case LootService.LOOT_TYPE_GIFT -> "addGift";
            default -> "addGeneric";
        };
    }

    static String fmt(float f) {
        return f == (int) f ? Integer.toString((int) f) : Float.toString(f);
    }

    static String js(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n").replace("\r", "\\r");
    }
}
