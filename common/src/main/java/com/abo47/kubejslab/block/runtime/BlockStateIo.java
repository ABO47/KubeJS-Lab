package com.abo47.kubejslab.block.runtime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.block.model.BlockAction;
import com.abo47.kubejslab.block.model.BlockFieldValues;
import com.abo47.kubejslab.block.model.BlockStatus;
import com.abo47.kubejslab.workspace.JsonStateFile;
import com.abo47.kubejslab.workspace.WorkspacePaths;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class BlockStateIo {

    private BlockStateIo() {
    }

    static Map<ResourceLocation, BlockSaveEntry> load() {
        Map<ResourceLocation, BlockSaveEntry> loaded = new LinkedHashMap<>();

        JsonObject root = JsonStateFile.load(WorkspacePaths.blockStateFile());
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
                BlockStatus status = BlockStatus.valueOf(obj.get("status").getAsString());
                String type = obj.has("type") ? obj.get("type").getAsString() : "basic";
                String name = obj.has("name") ? obj.get("name").getAsString() : "";
                boolean wasModified = obj.has("wasModified") && obj.get("wasModified").getAsBoolean();
                BlockFieldValues values = readValues(obj.getAsJsonObject("values"));
                List<String> tags = new ArrayList<>();
                if (obj.has("tags")) {
                    for (JsonElement el : obj.getAsJsonArray("tags")) {
                        tags.add(el.getAsString());
                    }
                }
                List<BlockAction> actions = new ArrayList<>();
                if (obj.has("actions")) {
                    for (JsonElement el : obj.getAsJsonArray("actions")) {
                        actions.add(BlockAction.valueOf(el.getAsString()));
                    }
                }
                loaded.put(id, new BlockSaveEntry(type, status, name, wasModified, values, tags, actions));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
            return loaded;
    }

    static void save(Map<ResourceLocation, BlockSaveEntry> states) throws IOException {
        JsonObject root = new JsonObject();
        for (Map.Entry<ResourceLocation, BlockSaveEntry> item : states.entrySet()) {
            BlockSaveEntry entry = item.getValue();
            JsonObject obj = new JsonObject();
            obj.addProperty("status", entry.status().name());
            obj.addProperty("type", entry.type());
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
            for (BlockAction action : entry.actions()) {
                actions.add(action.name());
            }
            obj.add("actions", actions);
            root.add(item.getKey().toString(), obj);
        }
        JsonStateFile.save(WorkspacePaths.blockStateFile(), root);
    }

    static BlockFieldValues readValues(JsonObject obj) {
        return new BlockFieldValues(obj.get("displayName").getAsString(), obj.get("textureAll").getAsString(),
                obj.get("textureTop").getAsString(), obj.get("textureBottom").getAsString(),
                obj.get("textureSides").getAsString(), obj.get("hardness").getAsFloat(),
                obj.get("resistance").getAsFloat(), obj.get("unbreakable").getAsBoolean(),
                obj.get("lightLevel").getAsInt(), obj.get("soundType").getAsString(),
                obj.get("requiresTool").getAsBoolean(), obj.get("noCollision").getAsBoolean(),
                obj.get("waterlogged").getAsBoolean(), obj.get("noDrops").getAsBoolean(),
                obj.get("notSolid").getAsBoolean(), obj.get("opaque").getAsBoolean(),
                obj.get("slipperiness").getAsFloat(), obj.get("speedFactor").getAsFloat(),
                obj.get("jumpFactor").getAsFloat(), obj.get("tags").getAsString(),
                stringOr(obj, "creativeTab"), stringOr(obj, "lootItem"),
                intOr(obj, "lootCountMin"), intOr(obj, "lootCountMax"),
                floatOr(obj, "lootChance"), stringOr(obj, "dustColor"),
                stringOr(obj, "blockSetType"), stringOr(obj, "woodType"));
    }

    static String stringOr(JsonObject obj, String key) {
        return obj.has(key) ? obj.get(key).getAsString() : "";
    }

    static int intOr(JsonObject obj, String key) {
        return obj.has(key) ? obj.get(key).getAsInt() : 0;
    }

    static float floatOr(JsonObject obj, String key) {
        return obj.has(key) ? obj.get(key).getAsFloat() : 0f;
    }

    static void writeValues(JsonObject obj, BlockFieldValues v) {
        obj.addProperty("displayName", v.displayName());
        obj.addProperty("textureAll", v.textureAll());
        obj.addProperty("textureTop", v.textureTop());
        obj.addProperty("textureBottom", v.textureBottom());
        obj.addProperty("textureSides", v.textureSides());
        obj.addProperty("hardness", v.hardness());
        obj.addProperty("resistance", v.resistance());
        obj.addProperty("unbreakable", v.unbreakable());
        obj.addProperty("lightLevel", v.lightLevel());
        obj.addProperty("soundType", v.soundType());
        obj.addProperty("requiresTool", v.requiresTool());
        obj.addProperty("noCollision", v.noCollision());
        obj.addProperty("waterlogged", v.waterlogged());
        obj.addProperty("noDrops", v.noDrops());
        obj.addProperty("notSolid", v.notSolid());
        obj.addProperty("opaque", v.opaque());
        obj.addProperty("slipperiness", v.slipperiness());
        obj.addProperty("speedFactor", v.speedFactor());
        obj.addProperty("jumpFactor", v.jumpFactor());
        obj.addProperty("tags", v.tags());
        obj.addProperty("creativeTab", v.creativeTab());
        obj.addProperty("lootItem", v.lootItem());
        obj.addProperty("lootCountMin", v.lootCountMin());
        obj.addProperty("lootCountMax", v.lootCountMax());
        obj.addProperty("lootChance", v.lootChance());
        obj.addProperty("dustColor", v.dustColor());
        obj.addProperty("blockSetType", v.blockSetType());
        obj.addProperty("woodType", v.woodType());
    }
}
