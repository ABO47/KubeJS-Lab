package com.abo47.kubejslab.loot.runtime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.kubejslab.KubeJSLab;
import com.abo47.kubejslab.lab.LabPathResolver;
import com.abo47.kubejslab.lab.LabScriptWriter;
import com.abo47.kubejslab.lab.LabServerCommands;
import com.abo47.kubejslab.lab.LabStateFile;
import com.abo47.kubejslab.lab.LabUniqueNames;
import com.abo47.kubejslab.loot.model.LabLootAction;
import com.abo47.kubejslab.loot.model.LabLootEditAction;
import com.abo47.kubejslab.loot.model.LabLootFieldValues;
import com.abo47.kubejslab.loot.model.LabLootPayload;
import com.abo47.kubejslab.loot.model.LabLootState;
import com.abo47.kubejslab.loot.model.LabLootStatus;
import com.abo47.kubejslab.network.ModNetwork;
import com.abo47.kubejslab.network.loot.S2CLootStatePacket;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


public final class LabLootService {
    private static final Map<ResourceLocation, LabLootSaveEntry> STATE = new LinkedHashMap<>();
    private static final Set<ResourceLocation> SESSION_CREATED_IDS = new HashSet<>();
    private static final Set<ResourceLocation> PENDING = new HashSet<>();
    private static boolean stateLoaded;

    public static final String LOOT_TYPE_BLOCK = "block";
    public static final String LOOT_TYPE_ENTITY = "entity";
    public static final String LOOT_TYPE_CHEST = "chest";
    public static final String LOOT_TYPE_FISHING = "fishing";
    public static final String LOOT_TYPE_GIFT = "gift";
    public static final String LOOT_TYPE_GENERIC = "generic";

    private LabLootService() {
    }

    public static void handle(ServerPlayer player, LabLootEditAction action, ResourceLocation targetId,
            LabLootPayload payload) {
        KubeJSLab.LOGGER.info(
                "[LabLootService] handle: action={}, targetId={}, lootType={}, target={}, tags={}, actions={}",
                action, targetId, payload.lootType(), payload.values().targetId(), payload.tags().size(),
                payload.actions().size());
        loadStateIfNeeded();
        try {
            switch (action) {
                case SAVE_NEW -> saveNew(payload);
                case MODIFY -> modify(targetId, payload);
                case DISABLE -> disable(targetId);
                case ENABLE -> enable(targetId);
                case RESET -> reset(targetId);
                case DELETE -> delete(targetId);
            }
            saveState();
            writeServerScript();
            MinecraftServer server = player.getServer();
            LabServerCommands.kubejsStartupReload(server);
            LabServerCommands.reload(server);
            KubeJSLab.LOGGER.info("[LabLootService] sent /kubejs reload startup_scripts and /reload after {}", action);
            ModNetwork.sendLootState(player, statePacket());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Failed to save loot: " + e.getMessage()));
        }
    }

    public static S2CLootStatePacket statePacket() {
        loadStateIfNeeded();
        Map<ResourceLocation, LabLootState> states = new HashMap<>();
        for (Map.Entry<ResourceLocation, LabLootSaveEntry> entry : STATE.entrySet()) {
            LabLootSaveEntry e = entry.getValue();
            states.put(entry.getKey(), new LabLootState(entry.getKey(), e.lootType(), e.status(),
                    PENDING.contains(entry.getKey()), e.name(), e.wasModified(), e.values(), e.tags(), e.actions()));
        }
        List<ResourceLocation> pendingOnly = new ArrayList<>();
        for (ResourceLocation id : PENDING) {
            if (!states.containsKey(id)) {
                pendingOnly.add(id);
            }
        }
        return new S2CLootStatePacket(states, pendingOnly);
    }

    private static void saveNew(LabLootPayload payload) throws IOException {
        String lootType = payload.lootType();
        if (lootType == null || lootType.isBlank()) {
            throw new IllegalArgumentException("Loot type is required");
        }
        String targetIdStr = payload.values().targetId();
        if (targetIdStr == null || targetIdStr.isBlank()) {
            throw new IllegalArgumentException("Target ID is required");
        }
        String baseName = LabUniqueNames.slugify(targetIdStr);
        if (baseName.isBlank()) {
            throw new IllegalArgumentException("Target ID is required");
        }
        ResourceLocation id = LabUniqueNames.uniqueId(LabUniqueNames.labId(baseName),
                existing -> STATE.containsKey(existing) || SESSION_CREATED_IDS.contains(existing));
        STATE.put(id, new LabLootSaveEntry(lootType, LabLootStatus.CREATED, targetIdStr, false, payload.values(),
                payload.tags(), payload.actions()));
        SESSION_CREATED_IDS.add(id);
        PENDING.add(id);
        KubeJSLab.LOGGER.info("[LabLootService] SAVE_NEW created {} for {}", id, targetIdStr);
    }

    private static void modify(ResourceLocation targetId, LabLootPayload payload) {
        if (targetId == null) {
            return;
        }
        LabLootSaveEntry existing = STATE.get(targetId);
        String name = payload.values().targetId().isBlank() && existing != null ? existing.name()
                : payload.values().targetId();
        STATE.put(targetId, new LabLootSaveEntry(payload.lootType(), LabLootStatus.MODIFIED, name, true, payload.values(),
                payload.tags(), payload.actions()));
        PENDING.add(targetId);
        KubeJSLab.LOGGER.info("[LabLootService] MODIFY wrote {}", targetId);
    }

    private static void disable(ResourceLocation targetId) {
        if (targetId == null) {
            return;
        }
        LabLootSaveEntry entry = STATE.get(targetId);
        if (entry == null) {
            entry = new LabLootSaveEntry(LOOT_TYPE_BLOCK, LabLootStatus.NORMAL, targetId.getPath(), false,
                    LabLootFieldValues.defaults(), List.of(), List.of());
        }
        List<LabLootAction> actions = new ArrayList<>(entry.actions());
        if (!actions.contains(LabLootAction.NO_EXPLOSION_DROP)) {
            actions.add(LabLootAction.NO_EXPLOSION_DROP);
        }
        STATE.put(targetId, new LabLootSaveEntry(entry.lootType(), LabLootStatus.DISABLED, entry.name(),
                entry.wasModified(), entry.values(), entry.tags(), actions));
        PENDING.add(targetId);
        KubeJSLab.LOGGER.info("[LabLootService] DISABLE {}", targetId);
    }

    private static void enable(ResourceLocation targetId) {
        if (targetId == null) {
            return;
        }
        LabLootSaveEntry entry = STATE.get(targetId);
        if (entry == null) {
            return;
        }
        if (entry.wasModified()) {
            List<LabLootAction> actions = new ArrayList<>(entry.actions());
            actions.remove(LabLootAction.NO_EXPLOSION_DROP);
            STATE.put(targetId, new LabLootSaveEntry(entry.lootType(), LabLootStatus.MODIFIED, entry.name(), true,
                    entry.values(), entry.tags(), actions));
        } else {
            STATE.remove(targetId);
        }
        PENDING.add(targetId);
        KubeJSLab.LOGGER.info("[LabLootService] ENABLE {}", targetId);
    }

    private static void reset(ResourceLocation targetId) {
        if (targetId == null) {
            return;
        }
        STATE.remove(targetId);
        PENDING.add(targetId);
        KubeJSLab.LOGGER.info("[LabLootService] RESET {}", targetId);
    }

    private static void delete(ResourceLocation targetId) {
        if (targetId == null || !LabPathResolver.isLabOwned(targetId)) {
            return;
        }
        STATE.remove(targetId);
        SESSION_CREATED_IDS.remove(targetId);
        PENDING.remove(targetId);
        KubeJSLab.LOGGER.info("[LabLootService] DELETE {}", targetId);
    }

    private static void writeServerScript() throws IOException {
        if (STATE.isEmpty()) {
            LabScriptWriter.write("server_scripts", "loot.js", "");
            return;
        }
        StringBuilder sb = new StringBuilder();
        Map<String, StringBuilder> typeBuffers = new HashMap<>();
        for (Map.Entry<ResourceLocation, LabLootSaveEntry> entry : STATE.entrySet()) {
            ResourceLocation id = entry.getKey();
            LabLootSaveEntry data = entry.getValue();
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
        LabScriptWriter.write("server_scripts", "loot.js", sb.toString());
    }

    private static void writeLootEntry(StringBuilder sb, String type, ResourceLocation id, LabLootSaveEntry data) {
        if (data.status() == LabLootStatus.DISABLED) {
            return;
        }
        LabLootFieldValues v = data.values();
        String targetId = v.targetId();
        if (targetId == null || targetId.isBlank()) {
            return;
        }
        String customId = v.customId();
        String entryId = customId == null || customId.isBlank() ? targetId : customId;
        String methodName = methodNameFor(type);
        if (LOOT_TYPE_ENTITY.equals(type)) {
            sb.append("    event.").append(methodName)
                    .append("(Utils.getRegistry('minecraft:entity_type').getValue('").append(js(entryId))
                    .append("'), loot => {\n");
        } else {
            sb.append("    event.").append(methodName).append("('").append(js(entryId)).append("', loot => {\n");
        }
        sb.append("        loot.addPool(pool => {\n");
        writeRolls(sb, v);
        writeEntry(sb, v);
        writePoolFunctions(sb, v);
        writePoolConditions(sb, v);
        sb.append("        });\n");
        sb.append("    });\n");
    }

    private static void writeRolls(StringBuilder sb, LabLootFieldValues v) {
        String type = v.poolRollsType() == null ? "constant" : v.poolRollsType();
        switch (type) {
            case "uniform" -> sb.append("            pool.setUniformRolls(").append(fmt(v.poolRollsMin())).append(", ")
                    .append(fmt(v.poolRollsMax())).append(");\n");
            case "binomial" -> sb.append("            pool.setBinomialRolls(").append(v.poolRollsN()).append(", ")
                    .append(fmt(v.poolRollsP())).append(");\n");
            default -> {
                if (v.poolRollsValue() != 1f) {
                    sb.append("            pool.setUniformRolls(").append(fmt(v.poolRollsValue())).append(", ")
                            .append(fmt(v.poolRollsValue())).append(");\n");
                }
            }
        }
    }

    private static void writeEntry(StringBuilder sb, LabLootFieldValues v) {
        String type = v.entryType() == null ? "item" : v.entryType();
        switch (type) {
            case "empty" -> {
                sb.append("            pool.addEmpty(").append(v.entryWeight()).append(");\n");
            }
            case "tag" -> {
                String tag = v.entryTag();
                if (tag != null && !tag.isBlank()) {
                    sb.append("            pool.addTag('").append(js(tag)).append("', true);\n");
                }
            }
            case "loot_table" -> {
                String lootTable = v.entryLootTable();
                if (lootTable != null && !lootTable.isBlank()) {
                    sb.append("            pool.addLootTable('").append(js(lootTable)).append("');\n");
                }
            }
            default -> {
                String item = v.entryItem();
                if (item != null && !item.isBlank()) {
                    String countType = v.entryCountType() == null ? "constant" : v.entryCountType();
                    boolean hasUniform = "uniform".equals(countType) && v.entryCountMin() != v.entryCountMax();
                    sb.append("            pool.addItem(Item.of('").append(js(item));
                    if ("constant".equals(countType) && v.entryCountValue() > 1f) {
                        sb.append("', ").append(fmt(v.entryCountValue()));
                    }
                    sb.append("'), ").append(v.entryWeight());
                    if ("uniform".equals(countType)) {
                        if (hasUniform) {
                            sb.append(", [").append(fmt(v.entryCountMin())).append(", ").append(fmt(v.entryCountMax()))
                                    .append("]");
                        } else {
                            sb.append(", ").append(fmt(v.entryCountMin()));
                        }
                    }
                    if (v.entryQuality() > 0) {
                        sb.append(").quality(").append(v.entryQuality());
                    }
                    sb.append(");\n");
                }
            }
        }
    }

    private static void writePoolFunctions(StringBuilder sb, LabLootFieldValues v) {
        if (v.poolSurvivesExplosion()) {
            sb.append("            pool.survivesExplosion();\n");
        }
        if (v.poolFurnaceSmelt()) {
            sb.append("            pool.furnaceSmelt();\n");
        }
        if (v.poolRandomChance() < 1f) {
            sb.append("            pool.randomChance(").append(fmt(v.poolRandomChance())).append(");\n");
        }
        if (v.poolLootingEnchant()) {
            sb.append("            pool.lootingEnchant(").append(fmt(v.poolLootingCount())).append(", ")
                    .append(v.poolLootingLimit()).append(");\n");
        }
    }

    private static void writePoolConditions(StringBuilder sb, LabLootFieldValues v) {
        if (v.poolKilledByPlayer()) {
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
        JsonObject root = LabStateFile.load(LabPathResolver.lootStateFile());
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
                LabLootStatus status = LabLootStatus.valueOf(obj.get("status").getAsString());
                String lootType = obj.has("lootType") ? obj.get("lootType").getAsString() : LOOT_TYPE_BLOCK;
                String name = obj.has("name") ? obj.get("name").getAsString() : "";
                boolean wasModified = obj.has("wasModified") && obj.get("wasModified").getAsBoolean();
                LabLootFieldValues values = obj.has("values") ? readValues(obj.getAsJsonObject("values"))
                        : LabLootFieldValues.defaults();
                List<String> tags = new ArrayList<>();
                if (obj.has("tags")) {
                    for (JsonElement el : obj.getAsJsonArray("tags")) {
                        tags.add(el.getAsString());
                    }
                }
                List<LabLootAction> actions = new ArrayList<>();
                if (obj.has("actions")) {
                    for (JsonElement el : obj.getAsJsonArray("actions")) {
                        actions.add(LabLootAction.valueOf(el.getAsString()));
                    }
                }
                STATE.put(id, new LabLootSaveEntry(lootType, status, name, wasModified, values, tags, actions));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static LabLootFieldValues readValues(JsonObject obj) {
        return new LabLootFieldValues(
                obj.get("targetId").getAsString(),
                obj.get("customId").getAsString(),
                obj.get("poolRollsType").getAsString(),
                obj.get("poolRollsValue").getAsFloat(),
                obj.get("poolRollsMin").getAsFloat(),
                obj.get("poolRollsMax").getAsFloat(),
                obj.get("poolRollsN").getAsInt(),
                obj.get("poolRollsP").getAsFloat(),
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
                obj.get("poolSurvivesExplosion").getAsBoolean(),
                obj.get("poolRandomChance").getAsFloat(),
                obj.get("poolKilledByPlayer").getAsBoolean(),
                obj.get("poolFurnaceSmelt").getAsBoolean(),
                obj.get("poolLootingEnchant").getAsBoolean(),
                obj.get("poolLootingCount").getAsFloat(),
                obj.get("poolLootingLimit").getAsInt());
    }

    private static void saveState() throws IOException {
        JsonObject root = new JsonObject();
        for (Map.Entry<ResourceLocation, LabLootSaveEntry> item : STATE.entrySet()) {
            LabLootSaveEntry entry = item.getValue();
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
            for (LabLootAction action : entry.actions()) {
                actions.add(action.name());
            }
            obj.add("actions", actions);
            root.add(item.getKey().toString(), obj);
        }
        LabStateFile.save(LabPathResolver.lootStateFile(), root);
    }

    private static void writeValues(JsonObject obj, LabLootFieldValues v) {
        obj.addProperty("targetId", v.targetId());
        obj.addProperty("customId", v.customId());
        obj.addProperty("poolRollsType", v.poolRollsType());
        obj.addProperty("poolRollsValue", v.poolRollsValue());
        obj.addProperty("poolRollsMin", v.poolRollsMin());
        obj.addProperty("poolRollsMax", v.poolRollsMax());
        obj.addProperty("poolRollsN", v.poolRollsN());
        obj.addProperty("poolRollsP", v.poolRollsP());
        obj.addProperty("entryType", v.entryType());
        obj.addProperty("entryItem", v.entryItem());
        obj.addProperty("entryTag", v.entryTag());
        obj.addProperty("entryLootTable", v.entryLootTable());
        obj.addProperty("entryCountType", v.entryCountType());
        obj.addProperty("entryCountValue", v.entryCountValue());
        obj.addProperty("entryCountMin", v.entryCountMin());
        obj.addProperty("entryCountMax", v.entryCountMax());
        obj.addProperty("entryWeight", v.entryWeight());
        obj.addProperty("entryQuality", v.entryQuality());
        obj.addProperty("poolSurvivesExplosion", v.poolSurvivesExplosion());
        obj.addProperty("poolRandomChance", v.poolRandomChance());
        obj.addProperty("poolKilledByPlayer", v.poolKilledByPlayer());
        obj.addProperty("poolFurnaceSmelt", v.poolFurnaceSmelt());
        obj.addProperty("poolLootingEnchant", v.poolLootingEnchant());
        obj.addProperty("poolLootingCount", v.poolLootingCount());
        obj.addProperty("poolLootingLimit", v.poolLootingLimit());
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

    private record LabLootSaveEntry(String lootType, LabLootStatus status, String name, boolean wasModified,
            LabLootFieldValues values, List<String> tags, List<LabLootAction> actions) {
    }
}
