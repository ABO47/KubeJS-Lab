package com.abo47.kubejslab.loot.runtime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.kubejslab.KubeJSLab;
import com.abo47.kubejslab.loot.model.LootAction;
import com.abo47.kubejslab.loot.model.LootEditAction;
import com.abo47.kubejslab.loot.model.LootEntryValues;
import com.abo47.kubejslab.loot.model.LootFieldValues;
import com.abo47.kubejslab.loot.model.LootPayload;
import com.abo47.kubejslab.loot.model.LootPoolValues;
import com.abo47.kubejslab.loot.model.LootState;
import com.abo47.kubejslab.loot.model.LootStatus;
import com.abo47.kubejslab.network.NetworkRegistry;
import com.abo47.kubejslab.network.loot.S2CLootStatePacket;
import com.abo47.kubejslab.workspace.JsonStateFile;
import com.abo47.kubejslab.workspace.ScriptWriter;
import com.abo47.kubejslab.workspace.ServerCommands;
import com.abo47.kubejslab.workspace.UniqueIds;
import com.abo47.kubejslab.workspace.WorkspacePaths;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public final class LootService {
    private static final Map<ResourceLocation, LootSaveEntry> STATE = new LinkedHashMap<>();
    private static final Set<ResourceLocation> SESSION_CREATED_IDS = new HashSet<>();
    private static boolean stateLoaded;

    public static final String LOOT_TYPE_BLOCK = "block";
    public static final String LOOT_TYPE_ENTITY = "entity";
    public static final String LOOT_TYPE_CHEST = "chest";
    public static final String LOOT_TYPE_FISHING = "fishing";
    public static final String LOOT_TYPE_GIFT = "gift";
    public static final String LOOT_TYPE_GENERIC = "generic";

    private LootService() {
    }

    public static void handle(ServerPlayer player, LootEditAction action, ResourceLocation targetId,
            LootPayload payload) {
        KubeJSLab.LOGGER.info(
                "[LootService] handle: action={}, targetId={}, lootType={}, target={}, tags={}, actions={}",
                action, targetId, payload.lootType(), payload.values().targetId(), payload.tags().size(),
                payload.actions().size());
        loadStateIfNeeded();
        try {
            switch (action) {
                case SAVE_NEW -> saveNew(payload);
                case MODIFY -> modify(targetId, payload);
                case DUPLICATE -> duplicate(targetId);
                case DISABLE -> disable(targetId);
                case ENABLE -> enable(targetId);
                case RESET -> reset(targetId);
                case DELETE -> delete(targetId);
            }
            saveState();
            writeServerScript();
            MinecraftServer server = player.getServer();
            ServerCommands.kubejsStartupReload(server);
            ServerCommands.reload(server);
            KubeJSLab.LOGGER.info("[LootService] sent /kubejs reload startup_scripts and /reload after {}", action);
            NetworkRegistry.sendLootState(player, statePacket());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
            player.sendSystemMessage(Component.literal("Failed to save loot: " + e.getMessage()));
        }
    }

    public static S2CLootStatePacket statePacket() {
        loadStateIfNeeded();
        Map<ResourceLocation, LootState> states = new HashMap<>();
        for (Map.Entry<ResourceLocation, LootSaveEntry> entry : STATE.entrySet()) {
            LootSaveEntry e = entry.getValue();
            states.put(entry.getKey(), new LootState(entry.getKey(), e.lootType(), e.status(),
                    e.name(), e.wasModified(), e.values(), e.tags(), e.actions()));
        }
        return new S2CLootStatePacket(states);
    }

    private static void saveNew(LootPayload payload) throws IOException {
        String lootType = payload.lootType();
        if (lootType == null || lootType.isBlank()) {
            throw new IllegalArgumentException("Loot type is required");
        }
        String targetIdStr = payload.values().targetId();
        if (targetIdStr == null || targetIdStr.isBlank()) {
            throw new IllegalArgumentException("Target ID is required");
        }
        String baseName = UniqueIds.slugify(targetIdStr);
        if (baseName.isBlank()) {
            throw new IllegalArgumentException("Target ID is required");
        }
        ResourceLocation id = UniqueIds.uniqueId(UniqueIds.labId(baseName),
                existing -> STATE.containsKey(existing) || SESSION_CREATED_IDS.contains(existing));
        STATE.put(id, new LootSaveEntry(lootType, LootStatus.CREATED, targetIdStr, false, payload.values(),
                payload.tags(), payload.actions()));
        SESSION_CREATED_IDS.add(id);
        KubeJSLab.LOGGER.info("[LootService] SAVE_NEW created {} for {}", id, targetIdStr);
    }

    private static void modify(ResourceLocation targetId, LootPayload payload) {
        if (targetId == null) {
            return;
        }
        LootSaveEntry existing = STATE.get(targetId);
        String name = payload.values().targetId().isBlank() && existing != null ? existing.name()
                : payload.values().targetId();
        STATE.put(targetId, new LootSaveEntry(payload.lootType(), LootStatus.MODIFIED, name, true, payload.values(),
                payload.tags(), payload.actions()));
        KubeJSLab.LOGGER.info("[LootService] MODIFY wrote {}", targetId);
    }

    private static void duplicate(ResourceLocation targetId) {
        if (targetId == null) {
            return;
        }
        LootSaveEntry source = STATE.get(targetId);
        if (source == null) {
            throw new IllegalArgumentException("Source loot not found: " + targetId);
        }
        String baseName = UniqueIds.slugify(source.name());
        if (baseName.isBlank()) {
            baseName = UniqueIds.slugify(source.values().targetId());
        }
        if (baseName.isBlank()) {
            baseName = "loot";
        }
        ResourceLocation id = UniqueIds.uniqueId(UniqueIds.labId(baseName + "_copy"),
                existing -> STATE.containsKey(existing) || SESSION_CREATED_IDS.contains(existing));
        STATE.put(id, new LootSaveEntry(source.lootType(), LootStatus.CREATED, source.name(), false,
                source.values(), source.tags(), source.actions()));
        SESSION_CREATED_IDS.add(id);
        KubeJSLab.LOGGER.info("[LootService] DUPLICATE created {}", id);
    }

    private static void disable(ResourceLocation targetId) {
        if (targetId == null) {
            return;
        }
        LootSaveEntry entry = STATE.get(targetId);
        if (entry == null) {
            entry = new LootSaveEntry(LOOT_TYPE_BLOCK, LootStatus.NORMAL, targetId.getPath(), false,
                    LootFieldValues.defaults(), List.of(), List.of());
        }
        List<LootAction> actions = new ArrayList<>(entry.actions());
        if (!actions.contains(LootAction.NO_EXPLOSION_DROP)) {
            actions.add(LootAction.NO_EXPLOSION_DROP);
        }
        STATE.put(targetId, new LootSaveEntry(entry.lootType(), LootStatus.DISABLED, entry.name(),
                entry.wasModified(), entry.values(), entry.tags(), actions));
        KubeJSLab.LOGGER.info("[LootService] DISABLE {}", targetId);
    }

    private static void enable(ResourceLocation targetId) {
        if (targetId == null) {
            return;
        }
        LootSaveEntry entry = STATE.get(targetId);
        if (entry == null) {
            return;
        }
        if (entry.wasModified()) {
            List<LootAction> actions = new ArrayList<>(entry.actions());
            actions.remove(LootAction.NO_EXPLOSION_DROP);
            STATE.put(targetId, new LootSaveEntry(entry.lootType(), LootStatus.MODIFIED, entry.name(), true,
                    entry.values(), entry.tags(), actions));
        } else {
            STATE.remove(targetId);
        }
        KubeJSLab.LOGGER.info("[LootService] ENABLE {}", targetId);
    }

    private static void reset(ResourceLocation targetId) {
        if (targetId == null) {
            return;
        }
        STATE.remove(targetId);
        KubeJSLab.LOGGER.info("[LootService] RESET {}", targetId);
    }

    private static void delete(ResourceLocation targetId) {
        if (targetId == null || !WorkspacePaths.isLabOwned(targetId)) {
            return;
        }
        STATE.remove(targetId);
        SESSION_CREATED_IDS.remove(targetId);
        KubeJSLab.LOGGER.info("[LootService] DELETE {}", targetId);
    }

    private static void writeServerScript() throws IOException {
        if (STATE.isEmpty()) {
            ScriptWriter.write("server_scripts", "loot.js", "");
            return;
        }
        StringBuilder sb = new StringBuilder();
        Map<String, StringBuilder> typeBuffers = new HashMap<>();
        for (Map.Entry<ResourceLocation, LootSaveEntry> entry : STATE.entrySet()) {
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

    private static void writeLootEntry(StringBuilder sb, String type, ResourceLocation id, LootSaveEntry data) {
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
        if (LOOT_TYPE_ENTITY.equals(type)) {
            sb.append("    event.").append(methodName)
                    .append("(Utils.getRegistry('minecraft:entity_type').getValue('").append(js(entryId))
                    .append("'), loot => {\n");
        } else {
            sb.append("    event.").append(methodName).append("('").append(js(entryId)).append("', loot => {\n");
        }
        for (LootPoolValues pool : v.pools()) {
            sb.append("        loot.addPool(pool => {\n");
            writeRolls(sb, pool);
            Set<Integer> emittedGroups = new HashSet<>();
            for (LootEntryValues entry : pool.entries()) {
                int group = Math.max(0, entry.alternativeGroup());
                if (group > 0) {
                    if (!emittedGroups.add(group)) {
                        continue;
                    }
                    writeAlternatives(sb, pool, group);
                } else {
                    writeEntry(sb, entry);
                }
            }
            writePoolFunctions(sb, pool);
            writePoolConditions(sb, pool);
            sb.append("        });\n");
        }
        sb.append("    });\n");
    }

    private static void writeRolls(StringBuilder sb, LootPoolValues p) {
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

    private static void writeEntry(StringBuilder sb, LootEntryValues e) {
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
                String item = e.item();
                if (item == null || item.isBlank()) {
                    return;
                }
                String countType = e.countType() == null ? "constant" : e.countType();
                boolean hasUniform = "uniform".equals(countType) && e.countMin() != e.countMax();
                    sb.append("            pool.addItem(Item.of('").append(js(item));
                    if ("constant".equals(countType) && e.countValue() > 1f) {
                        sb.append("', ").append(fmt(e.countValue())).append(")");
                    } else {
                        sb.append("')");
                    }
                    sb.append(", ").append(e.weight());
                if ("uniform".equals(countType)) {
                    if (hasUniform) {
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

    private static void writeEntryChains(StringBuilder sb, LootEntryValues e) {
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

    private static String bonusCountJson(LootEntryValues e) {
        if (e.lootBonusMin() != e.lootBonusMax()) {
            return "{type: 'minecraft:uniform', min: " + fmt(e.lootBonusMin()) + ", max: "
                    + fmt(e.lootBonusMax()) + "}";
        }
        return fmt(e.lootBonusMax());
    }

    private static void appendRawJson(StringBuilder sb, String method, String raw) {
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

    private static JsonArray parseRawArray(String raw) {
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

    private static void writeAlternatives(StringBuilder sb, LootPoolValues pool, int group) {
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

    private static JsonObject childJson(LootEntryValues e) {
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

    private static JsonArray entryConditionsJson(LootEntryValues e) {
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

    private static void writePoolFunctions(StringBuilder sb, LootPoolValues p) {
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

    private static void writePoolConditions(StringBuilder sb, LootPoolValues p) {
        if (p.killedByPlayer()) {
            sb.append("            pool.killedByPlayer();\n");
        }
    }

    private static String eventNameFor(String type) {
        return switch (type) {
            case LOOT_TYPE_BLOCK -> "blockLootTables";
            case LOOT_TYPE_ENTITY -> "entityLootTables";
            case LOOT_TYPE_CHEST -> "chestLootTables";
            case LOOT_TYPE_FISHING -> "fishingLootTables";
            case LOOT_TYPE_GIFT -> "giftLootTables";
            default -> "genericLootTables";
        };
    }

    private static String methodNameFor(String type) {
        return switch (type) {
            case LOOT_TYPE_BLOCK -> "addBlock";
            case LOOT_TYPE_ENTITY -> "addEntity";
            case LOOT_TYPE_CHEST -> "addChest";
            case LOOT_TYPE_FISHING -> "addFishing";
            case LOOT_TYPE_GIFT -> "addGift";
            default -> "addGeneric";
        };
    }

    private static void loadStateIfNeeded() {
        if (stateLoaded) {
            return;
        }
        stateLoaded = true;
        JsonObject root = JsonStateFile.load(WorkspacePaths.lootStateFile());
        if (root == null) {
            return;
        }
        for (String key : root.keySet()) {
            try {
                JsonObject obj = root.getAsJsonObject(key);
                if (!obj.has("values")) {
                    continue;
                }
                ResourceLocation id = new ResourceLocation(key);
                LootStatus status = LootStatus.valueOf(obj.get("status").getAsString());
                String lootType = obj.has("lootType") ? obj.get("lootType").getAsString() : LOOT_TYPE_BLOCK;
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
                STATE.put(id, new LootSaveEntry(lootType, status, name, wasModified, values, tags, actions));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static LootFieldValues readValues(JsonObject obj) {
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

    private static LootPoolValues readPool(JsonObject obj) {
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

    private static LootPoolValues readLegacyPool(JsonObject obj) {
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

    private static void saveState() throws IOException {
        JsonObject root = new JsonObject();
        for (Map.Entry<ResourceLocation, LootSaveEntry> item : STATE.entrySet()) {
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

    private static void writeValues(JsonObject obj, LootFieldValues v) {
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

    private static List<String> readNotes(JsonObject e) {
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

    private static List<String> readPoolNotes(JsonObject obj) {
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

    private static String fmt(float f) {
        return f == (int) f ? Integer.toString((int) f) : Float.toString(f);
    }

    private static String js(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("'", "\\'");
    }

    private record LootSaveEntry(String lootType, LootStatus status, String name, boolean wasModified,
            LootFieldValues values, List<String> tags, List<LootAction> actions) {
    }
}
