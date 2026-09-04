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
import com.abo47.kubejslab.loot.model.LabLootNotes;
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
        int droppedEntries = 0;
        int poolCount = Math.min(pools.size(), MAX_POOLS);
        for (int i = 0; i < poolCount; i++) {
            if (!pools.get(i).isJsonObject()) {
                continue;
            }
            int[] dropped = new int[1];
            poolValues.add(parsePool(pools.get(i).getAsJsonObject(), dropped));
            droppedEntries += dropped[0];
        }
        if (poolValues.isEmpty()) {
            return blankFor(id);
        }
        return new LabLootFieldValues(id.toString(), "", poolValues,
                Math.max(0, pools.size() - poolCount), droppedEntries);
    }

    public static LabLootFieldValues blankFor(ResourceLocation id) {
        LabLootFieldValues defaults = LabLootFieldValues.defaults();
        return new LabLootFieldValues(id.toString(), defaults.customId(), defaults.pools(), 0, 0);
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

    private static LabLootPoolValues parsePool(JsonObject pool, int[] droppedOut) {
        String rollsType = "constant";
        float rollsValue = 1f;
        float rollsMin = 0f;
        float rollsMax = 0f;
        int rollsN = 0;
        float rollsP = 0.5f;
        List<String> poolNotes = new ArrayList<>();
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
            } else if (!providerType.isBlank()) {
                addNote(poolNotes,
                        LabLootNotes.encode(LabLootNotes.ROLLS_PROVIDER, prettifyId(providerType)));
            }
        }
        float bonusRolls = 0f;
        if (pool.has("bonus_rolls")) {
            float[] bonus = rangeOf(pool.get("bonus_rolls"), 0f);
            bonusRolls = Math.max(bonus[0], bonus[1]);
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
                } else {
                    addNote(poolNotes, describeCondition(c));
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

        List<LabLootEntryValues> allEntries = new ArrayList<>();
        int[] groupId = new int[] { 0 };
        if (pool.has("entries") && pool.get("entries").isJsonArray()) {
            for (JsonElement entryEl : pool.getAsJsonArray("entries")) {
                if (!entryEl.isJsonObject()) {
                    continue;
                }
                collectEntries(entryEl.getAsJsonObject(), allEntries, groupId);
            }
        }
        int dropped = Math.max(0, allEntries.size() - MAX_ENTRIES);
        List<LabLootEntryValues> entryValues =
                new ArrayList<>(allEntries.subList(0, Math.min(allEntries.size(), MAX_ENTRIES)));
        if (entryValues.isEmpty()) {
            entryValues.add(LabLootEntryValues.defaults());
        }
        if (droppedOut != null && droppedOut.length > 0) {
            droppedOut[0] = dropped;
        }
        return new LabLootPoolValues(rollsType, rollsValue, rollsMin, rollsMax, rollsN, rollsP, survivesExplosion,
                randomChance, killedByPlayer, furnaceSmelt, lootingEnchant, lootingCount, lootingLimit, entryValues,
                bonusRolls, poolNotes);
    }

    private static void collectEntries(JsonObject entry, List<LabLootEntryValues> out, int[] groupId) {
        String rawType = entry.has("type") ? entry.get("type").getAsString() : "";
        if (rawType.endsWith("alternatives") && entry.has("children")
                && entry.get("children").isJsonArray()) {
            groupId[0]++;
            int group = groupId[0];
            for (JsonElement childEl : entry.getAsJsonArray("children")) {
                if (!childEl.isJsonObject()) {
                    continue;
                }
                LabLootEntryValues child = parseEntry(childEl.getAsJsonObject());
                out.add(withGroup(child, group));
            }
            return;
        }
        if ((rawType.endsWith("sequence") || rawType.endsWith("group")) && entry.has("children")
                && entry.get("children").isJsonArray()) {
            for (JsonElement childEl : entry.getAsJsonArray("children")) {
                if (!childEl.isJsonObject()) {
                    continue;
                }
                collectEntries(childEl.getAsJsonObject(), out, groupId);
            }
            return;
        }
        out.add(parseEntry(entry));
    }

    private static LabLootEntryValues withGroup(LabLootEntryValues entry, int group) {
        return new LabLootEntryValues(entry.type(), entry.item(), entry.tag(), entry.lootTable(),
                entry.countType(), entry.countValue(), entry.countMin(), entry.countMax(), entry.weight(),
                entry.quality(), entry.lootBonusMin(), entry.lootBonusMax(), entry.conditionNotes(),
                entry.toolRequirement(), entry.entryKilledByPlayer(), entry.entryChance(),
                entry.entryChanceLooting(), group, entry.fortuneBonus(), entry.lootBonusLimit(),
                entry.explosionDecay(), entry.extraConditions(), entry.extraFunctions());
    }

    private static void addNote(List<String> notes, String note) {
        if (note != null && !note.isBlank() && !notes.contains(note) && notes.size() < 8) {
            notes.add(note);
        }
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
        } else if ("minecraft:dynamic".equals(rawType)) {
            type = "dynamic";
            item = entry.has("name") ? entry.get("name").getAsString() : "";
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
        float lootBonusMin = 0f;
        float lootBonusMax = 0f;
        int lootBonusLimit = 0;
        boolean explosionDecay = false;
        boolean fortuneBonus = false;
        List<String> conditionNotes = new ArrayList<>();
        if (entry.has("functions") && entry.get("functions").isJsonArray()) {
            for (JsonElement fnEl : entry.getAsJsonArray("functions")) {
                if (!fnEl.isJsonObject()) {
                    continue;
                }
                JsonObject f = fnEl.getAsJsonObject();
                String fname = f.has("function") ? f.get("function").getAsString() : "";
                if ("minecraft:looting_enchant".equals(fname)) {
                    float[] bonus = rangeOf(f.get("count"), 1f);
                    lootBonusMin = bonus[0];
                    lootBonusMax = bonus[1];
                    lootBonusLimit = f.has("limit") ? Math.max(0, (int) asFloat(f.get("limit"), 0f)) : 0;
                    continue;
                }
                if ("minecraft:explosion_decay".equals(fname)) {
                    explosionDecay = true;
                    continue;
                }
                if ("minecraft:apply_bonus".equals(fname)) {
                    String enchant = f.has("enchantment") ? f.get("enchantment").getAsString() : "";
                    String formula = f.has("formula") ? f.get("formula").getAsString() : "";
                    if ("minecraft:fortune".equals(enchant) && "minecraft:ore_drops".equals(formula)) {
                        fortuneBonus = true;
                    } else if (!enchant.isBlank()) {
                        addNote(conditionNotes, LabLootNotes.encode(LabLootNotes.ENCHANT_BONUS,
                                LabLootNotes.vanillaRef("enchantment." + enchant)));
                    } else {
                        addNote(conditionNotes, describeFunction(f));
                    }
                    continue;
                }
                if (!"minecraft:set_count".equals(fname)) {
                    addNote(conditionNotes, describeFunction(f));
                    continue;
                }
                if (!f.has("count")) {
                    continue;
                }
                JsonElement countEl = f.get("count");
                if (countEl.isJsonPrimitive()) {
                    countValue = Math.max(1f, countEl.getAsFloat());
                } else if (countEl.isJsonObject()) {
                    JsonObject countObj = countEl.getAsJsonObject();
                    String countProvider = countObj.has("type") ? countObj.get("type").getAsString() : "";
                    if (countObj.has("min") && countObj.has("max")
                            && !countProvider.contains("binomial")) {
                        countType = "uniform";
                        countMin = countObj.get("min").getAsFloat();
                        countMax = countObj.get("max").getAsFloat();
                    } else if (countObj.has("value")) {
                        countValue = Math.max(1f, countObj.get("value").getAsFloat());
                    } else if (countProvider.contains("binomial") && countObj.has("n")
                            && countObj.has("p")) {
                        addNote(conditionNotes, LabLootNotes.encode(LabLootNotes.BINOMIAL,
                                countObj.get("n").getAsString(), countObj.get("p").getAsString()));
                    } else if (countProvider.isBlank() && countObj.has("min")) {
                        countMin = countObj.get("min").getAsFloat();
                        countMax = countMin;
                    } else if (!countProvider.isBlank()) {
                        addNote(conditionNotes, LabLootNotes.encode(LabLootNotes.COUNT_PROVIDER,
                                prettifyId(countProvider)));
                    }
                }
            }
        }
        String toolRequirement = "";
        boolean entryKilledByPlayer = false;
        float entryChance = 1f;
        float entryChanceLooting = 0f;
        if (entry.has("conditions") && entry.get("conditions").isJsonArray()) {
            for (JsonElement condEl : entry.getAsJsonArray("conditions")) {
                if (!condEl.isJsonObject()) {
                    continue;
                }
                JsonObject c = condEl.getAsJsonObject();
                String cname = c.has("condition") ? c.get("condition").getAsString() : "";
                switch (cname) {
                    case "minecraft:killed_by_player" -> entryKilledByPlayer = true;
                    case "minecraft:random_chance" -> entryChance =
                            Math.max(0f, Math.min(1f, asFloat(c.get("chance"), 1f)));
                    case "minecraft:random_chance_with_looting" -> {
                        entryChance = Math.max(0f, Math.min(1f, asFloat(c.get("chance"), 0f)));
                        entryChanceLooting = Math.max(0f, asFloat(c.get("looting_multiplier"), 0f));
                    }
                    case "minecraft:match_tool" -> {
                        String tool = matchToolEnchant(c);
                        if (tool != null && !toolRequirement.equals("silk_touch")) {
                            toolRequirement = tool;
                        } else if (tool == null) {
                            addNote(conditionNotes, describeCondition(c));
                        }
                    }
                    default -> addNote(conditionNotes, describeCondition(c));
                }
            }
        }
        return new LabLootEntryValues(type, item, tag, lootTable, countType, countValue, countMin, countMax, weight,
                quality, lootBonusMin, lootBonusMax, conditionNotes, toolRequirement, entryKilledByPlayer,
                entryChance, entryChanceLooting, 0, fortuneBonus, lootBonusLimit, explosionDecay, "", "");
    }

    private static String matchToolEnchant(JsonObject condition) {
        if (!condition.has("predicate") || !condition.get("predicate").isJsonObject()) {
            return null;
        }
        JsonObject predicate = condition.getAsJsonObject("predicate");
        if (!predicate.has("enchantments") || !predicate.get("enchantments").isJsonArray()) {
            return null;
        }
        for (JsonElement el : predicate.getAsJsonArray("enchantments")) {
            if (!el.isJsonObject()) {
                continue;
            }
            String id = el.getAsJsonObject().has("enchantment")
                    ? el.getAsJsonObject().get("enchantment").getAsString()
                    : "";
            if ("minecraft:silk_touch".equals(id)) {
                return "silk_touch";
            }
            if ("minecraft:fortune".equals(id)) {
                return "fortune";
            }
        }
        return null;
    }

    private static String describeFunction(JsonObject function) {
        String name = function.has("function") ? function.get("function").getAsString() : "";
        switch (name) {
            case "minecraft:furnace_smelt" -> {
                if (function.has("conditions") && function.get("conditions").isJsonArray()) {
                    for (JsonElement el : function.getAsJsonArray("conditions")) {
                        if (el.isJsonObject()
                                && LabLootNotes.WHILE_BURNING.equals(entityNote(el.getAsJsonObject()))) {
                            return LabLootNotes.SMELTED_BURNING;
                        }
                    }
                }
                return LabLootNotes.SMELTED;
            }
            case "minecraft:copy_state" -> {
                return LabLootNotes.COPY_STATE;
            }
            case "minecraft:copy_name" -> {
                return LabLootNotes.COPY_NAME;
            }
            case "minecraft:set_contents" -> {
                return LabLootNotes.KEEP_CONTENTS;
            }
            case "minecraft:enchant_randomly" -> {
                return LabLootNotes.ENCHANT_RANDOM;
            }
            case "minecraft:enchant_with_levels" -> {
                return LabLootNotes.ENCHANTED;
            }
            case "minecraft:fill_player_head" -> {
                return LabLootNotes.PLAYER_HEAD;
            }
            case "minecraft:set_nbt", "minecraft:set_name", "minecraft:set_lore", "minecraft:copy_nbt",
                    "minecraft:set_attributes" -> {
                return LabLootNotes.NBT_EDITS;
            }
            default -> {
                return prettifyId(name);
            }
        }
    }

    private static String describeCondition(JsonObject condition) {
        String name = condition.has("condition") ? condition.get("condition").getAsString() : "";
        return switch (name) {
            case "minecraft:killed_by_player" -> LabLootNotes.KILLED_BY_PLAYER;
            case "minecraft:survives_explosion" -> LabLootNotes.SURVIVES_EXPLOSION;
            case "minecraft:random_chance" -> LabLootNotes.encode(LabLootNotes.CHANCE,
                    formatPercent(asFloat(condition.get("chance"), 1f)));
            case "minecraft:random_chance_with_looting" -> LabLootNotes.encode(LabLootNotes.CHANCE_LOOTING,
                    formatPercent(asFloat(condition.get("chance"), 0f)),
                    formatPercent(asFloat(condition.get("looting_multiplier"), 0f)));
            case "minecraft:match_tool" -> LabLootNotes.encode(LabLootNotes.MATCH_TOOL, toolNote(condition));
            case "minecraft:entity_properties" -> entityNote(condition);
            case "minecraft:inverted" -> condition.has("term") && condition.get("term").isJsonObject()
                    ? LabLootNotes.encode(LabLootNotes.INVERTED,
                            describeCondition(condition.getAsJsonObject("term")))
                    : "inverted";
            case "minecraft:alternative", "minecraft:any_of" ->
                LabLootNotes.encode(LabLootNotes.ANY_OF, joinTerms(condition).toArray(new String[0]));
            case "minecraft:all_of" ->
                LabLootNotes.encode(LabLootNotes.ALL_OF, joinTerms(condition).toArray(new String[0]));
            default -> prettifyId(name);
        };
    }

    private static String toolNote(JsonObject condition) {
        if (!condition.has("predicate") || !condition.get("predicate").isJsonObject()) {
            return LabLootNotes.MATCHING_TOOL;
        }
        JsonObject predicate = condition.getAsJsonObject("predicate");
        if (!predicate.has("enchantments") || !predicate.get("enchantments").isJsonArray()) {
            return LabLootNotes.MATCHING_TOOL;
        }
        List<String> parts = new ArrayList<>();
        for (JsonElement el : predicate.getAsJsonArray("enchantments")) {
            if (!el.isJsonObject()) {
                continue;
            }
            JsonObject enchant = el.getAsJsonObject();
            String id = enchant.has("enchantment") ? enchant.get("enchantment").getAsString() : "";
            if (id.isBlank()) {
                continue;
            }
            String ref = LabLootNotes.vanillaRef("enchantment." + id);
            if (enchant.has("levels")) {
                float[] levels = rangeOf(enchant.get("levels"), 1f);
                parts.add(levels[0] == levels[1]
                        ? LabLootNotes.encode(LabLootNotes.ENCHANT, ref)
                        : LabLootNotes.encode(LabLootNotes.ENCHANT_LEVELED, ref,
                                formatCount(levels[0]) + "-" + formatCount(levels[1])));
            } else {
                parts.add(LabLootNotes.encode(LabLootNotes.ENCHANT, ref));
            }
        }
        if (parts.isEmpty()) {
            return LabLootNotes.MATCHING_TOOL;
        }
        return parts.size() == 1 ? parts.get(0)
                : LabLootNotes.encode(LabLootNotes.ANY_OF, parts.toArray(new String[0]));
    }

    private static String entityNote(JsonObject condition) {
        if (condition.has("predicate") && condition.get("predicate").isJsonObject()) {
            JsonObject predicate = condition.getAsJsonObject("predicate");
            if (predicate.has("flags") && predicate.getAsJsonObject("flags").has("is_on_fire")
                    && predicate.getAsJsonObject("flags").get("is_on_fire").getAsBoolean()) {
                return LabLootNotes.WHILE_BURNING;
            }
        }
        return prettifyId(condition.has("condition") ? condition.get("condition").getAsString() : "");
    }

    private static List<String> joinTerms(JsonObject condition) {
        List<String> parts = new ArrayList<>();
        if (!condition.has("terms") || !condition.get("terms").isJsonArray()) {
            String fallback = prettifyId(condition.has("condition") ? condition.get("condition").getAsString() : "");
            if (!fallback.isBlank()) {
                parts.add(fallback);
            }
            return parts;
        }
        for (JsonElement el : condition.getAsJsonArray("terms")) {
            if (!el.isJsonObject() || parts.size() >= 4) {
                continue;
            }
            String note = describeCondition(el.getAsJsonObject());
            if (!note.isBlank()) {
                parts.add(note);
            }
        }
        return parts;
    }

    private static String prettifyId(String id) {
        if (id == null || id.isBlank()) {
            return "special condition";
        }
        String path = id.contains(":") ? id.substring(id.indexOf(':') + 1) : id;
        return path.replace('_', ' ').trim();
    }

    private static String formatPercent(float fraction) {
        float percent = Math.round(fraction * 1000f) / 10f;
        return formatCount(percent) + "%";
    }

    private static String formatCount(float value) {
        if (value == (int) value) {
            return Integer.toString((int) value);
        }
        return Float.toString(value);
    }

    private static float[] rangeOf(JsonElement el, float fallback) {
        if (el == null || el.isJsonNull()) {
            return new float[] { Math.max(0f, fallback), Math.max(0f, fallback) };
        }
        if (el.isJsonPrimitive()) {
            float v = Math.max(0f, el.getAsFloat());
            return new float[] { v, v };
        }
        if (el.isJsonObject()) {
            JsonObject obj = el.getAsJsonObject();
            if (obj.has("value")) {
                float v = Math.max(0f, asFloat(obj.get("value"), fallback));
                return new float[] { v, v };
            }
            if (obj.has("min") || obj.has("max")) {
                float min = Math.max(0f, asFloat(obj.get("min"), fallback));
                float max = Math.max(min, asFloat(obj.get("max"), min));
                return new float[] { min, max };
            }
        }
        float v = Math.max(0f, fallback);
        return new float[] { v, v };
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
