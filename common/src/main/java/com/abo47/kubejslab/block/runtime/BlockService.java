package com.abo47.kubejslab.block.runtime;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;

import com.abo47.kubejslab.block.model.BlockAction;
import com.abo47.kubejslab.block.model.BlockEditAction;
import com.abo47.kubejslab.block.model.BlockFieldValues;
import com.abo47.kubejslab.block.model.BlockPayload;
import com.abo47.kubejslab.block.model.BlockState;
import com.abo47.kubejslab.block.model.BlockStatus;
import com.abo47.kubejslab.client.ui.shell.UiKeys;
import com.abo47.kubejslab.network.NetworkRegistry;
import com.abo47.kubejslab.network.block.S2CBlockStatePacket;
import com.abo47.kubejslab.workspace.JsonStateFile;
import com.abo47.kubejslab.workspace.ScriptEscaping;
import com.abo47.kubejslab.workspace.ScriptWriter;
import com.abo47.kubejslab.workspace.ServerCommands;
import com.abo47.kubejslab.workspace.UniqueIds;
import com.abo47.kubejslab.workspace.WorkspacePaths;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


public final class BlockService {
    private static final Map<ResourceLocation, BlockSaveEntry> STATE = new LinkedHashMap<>();
    private static final Set<ResourceLocation> SESSION_CREATED_IDS = new HashSet<>();
    private static final Set<ResourceLocation> PENDING = new HashSet<>();
    private static boolean stateLoaded;

    public static final List<String> TYPES = List.of("basic", "detector", "slab", "stairs", "fence", "wall",
            "fence_gate", "pressure_plate", "button", "falling", "crop", "cardinal", "carpet");
    private static final int CROP_AGES = 8;
    private static final String[] SOUND_TYPES = {
            "", "wood", "stone", "metal", "gravel", "grass", "sand", "glass", "wool", "snow", "crop",
            "slime", "anvil", "ladder", "honey", "amethyst", "deepslate", "netherrack", "candle", "sculk"
    };
    private static final String[] BLOCK_SET_TYPES = {"", "oak", "stone", "iron", "gold", "diamond", "netherite",
            "polished_blackstone"};
    private static final String[] WOOD_TYPES = {"oak", "spruce", "birch", "jungle", "acacia", "dark_oak",
            "mangrove", "cherry", "bamboo", "crimson", "warped"};
    private static final String[] CREATIVE_TABS = {
            "minecraft:building_blocks", "minecraft:colored_blocks", "minecraft:natural_blocks",
            "minecraft:functional_blocks", "minecraft:redstone_blocks", "minecraft:hotbar",
            "minecraft:search", "minecraft:tools_and_utilities", "minecraft:combat",
            "minecraft:food_and_drinks", "minecraft:ingredients", "minecraft:spawn_eggs",
            "minecraft:op_blocks", "minecraft:misc"
    };

    private BlockService() {
    }

    public static void handle(ServerPlayer player, BlockEditAction action, ResourceLocation targetId,
            BlockPayload payload) {
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
            writeStartupScript();
            writeModificationScript();
            writeServerScript();
            writeCreativeHideScript();
            writeClientScript();
            MinecraftServer server = player.getServer();
            ServerCommands.kubejsStartupReload(server);
            ServerCommands.reload(server);
            if (copyTextures()) {
                ServerCommands.kubejsTextureReload(server);
            }
            NetworkRegistry.sendBlockState(player, statePacket());
            if (!PENDING.isEmpty()) {
                player.sendSystemMessage(Component.translatable(UiKeys.CHAT_RESTART_REQUIRED));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
            player.sendSystemMessage(Component.literal("Failed to save block: " + e.getMessage()));
        }
    }

    public static S2CBlockStatePacket statePacket() {
        loadStateIfNeeded();
        Map<ResourceLocation, BlockState> states = new HashMap<>();
        for (Map.Entry<ResourceLocation, BlockSaveEntry> entry : STATE.entrySet()) {
            BlockSaveEntry e = entry.getValue();
            states.put(entry.getKey(), new BlockState(entry.getKey(), e.type(), e.status(),
                    PENDING.contains(entry.getKey()), e.name(), e.wasModified(), e.values(), e.tags(), e.actions()));
        }
        List<ResourceLocation> pendingOnly = new ArrayList<>();
        for (ResourceLocation id : PENDING) {
            if (!states.containsKey(id)) {
                pendingOnly.add(id);
            }
        }
        return new S2CBlockStatePacket(states, pendingOnly);
    }

    private static void saveNew(BlockPayload payload) {
        if (payload.type().isBlank()) {
            throw new IllegalArgumentException("Block type is required");
        }
        String baseName = UniqueIds.slugify(payload.values().displayName());
        if (baseName.isBlank()) {
            throw new IllegalArgumentException("Block display name is required");
        }
        ResourceLocation id = UniqueIds.uniqueId(UniqueIds.labId(baseName),
                existing -> STATE.containsKey(existing) || SESSION_CREATED_IDS.contains(existing));
        STATE.put(id, new BlockSaveEntry(payload.type(), BlockStatus.CREATED,
                payload.values().displayName(), false, payload.values(), payload.tags(), payload.actions()));
        SESSION_CREATED_IDS.add(id);
        PENDING.add(id);
    }

    private static void modify(ResourceLocation targetId, BlockPayload payload) {
        if (targetId == null) {
            return;
        }
        BlockSaveEntry existing = STATE.get(targetId);
        String name = payload.values().displayName().isBlank() && existing != null ? existing.name()
                : payload.values().displayName();
        STATE.put(targetId, new BlockSaveEntry(payload.type(), BlockStatus.MODIFIED, name, true,
                payload.values(), payload.tags(), payload.actions()));
        PENDING.add(targetId);
    }

    private static void duplicate(ResourceLocation targetId) {
        if (targetId == null) {
            return;
        }
        BlockSaveEntry source = STATE.get(targetId);
        if (source == null) {
            throw new IllegalArgumentException("Source block not found: " + targetId);
        }
        String base = targetId.getPath().substring("lab/".length()) + "_copy";
        ResourceLocation id = UniqueIds.uniqueId(UniqueIds.labId(base),
                existing -> STATE.containsKey(existing) || SESSION_CREATED_IDS.contains(existing));
        STATE.put(id, new BlockSaveEntry(source.type(), BlockStatus.CREATED, source.name(), false,
                source.values(), source.tags(), source.actions()));
        SESSION_CREATED_IDS.add(id);
        PENDING.add(id);
    }

    private static void disable(ResourceLocation targetId) {
        if (targetId == null) {
            return;
        }
        BlockSaveEntry entry = STATE.get(targetId);
        if (entry == null) {
            entry = new BlockSaveEntry("basic", BlockStatus.NORMAL, targetId.getPath(), false,
                    BlockFieldValues.defaults(), List.of(), List.of());
        }
        STATE.put(targetId, new BlockSaveEntry(entry.type(), BlockStatus.DISABLED, entry.name(),
                entry.wasModified(), entry.values(), entry.tags(), withHideActions(entry.actions(), true)));
        PENDING.add(targetId);
    }

    private static void enable(ResourceLocation targetId) {
        if (targetId == null) {
            return;
        }
        BlockSaveEntry entry = STATE.get(targetId);
        if (entry == null) {
            return;
        }
        if (entry.wasModified()) {
            STATE.put(targetId, new BlockSaveEntry(entry.type(), BlockStatus.MODIFIED, entry.name(),
                    true, entry.values(), entry.tags(), withHideActions(entry.actions(), false)));
        } else {
            STATE.remove(targetId);
        }
        PENDING.add(targetId);
    }

    private static List<BlockAction> withHideActions(List<BlockAction> source, boolean set) {
        List<BlockAction> actions = new ArrayList<>(source);
        for (BlockAction action : List.of(BlockAction.HIDE_CREATIVE_TAB, BlockAction.REMOVE_RECIPES,
                BlockAction.HIDE_VIEWER)) {
            if (set && !actions.contains(action)) {
                actions.add(action);
            } else if (!set) {
                actions.remove(action);
            }
        }
        return actions;
    }

    private static void reset(ResourceLocation targetId) throws IOException {
        if (targetId == null) {
            return;
        }
        STATE.remove(targetId);
        PENDING.add(targetId);
        if (!WorkspacePaths.isLabOwned(targetId)) {
            deleteCopiedTextures(targetId.getPath());
        }
    }

    private static void delete(ResourceLocation targetId) throws IOException {
        if (targetId == null || !WorkspacePaths.isLabOwned(targetId)) {
            return;
        }
        STATE.remove(targetId);
        SESSION_CREATED_IDS.remove(targetId);
        PENDING.remove(targetId);
        deleteCopiedTextures(targetId.getPath());
    }

    private static void deleteCopiedTextures(String path) throws IOException {
        Path dir = texturesDir();
        Files.deleteIfExists(dir.resolve(path + ".png"));
        Files.deleteIfExists(dir.resolve(path + "_top.png"));
        Files.deleteIfExists(dir.resolve(path + "_bottom.png"));
        Files.deleteIfExists(dir.resolve(path + "_side.png"));
        for (int age = 0; age < CROP_AGES; age++) {
            Files.deleteIfExists(dir.resolve(path + age + ".png"));
        }
    }

    private static void writeStartupScript() throws IOException {
        StringBuilder sb = new StringBuilder();
        boolean any = false;
        for (ResourceLocation id : STATE.keySet()) {
            if (WorkspacePaths.isLabOwned(id)) {
                any = true;
                break;
            }
        }
        if (!any) {
            ScriptWriter.write("startup_scripts", "blocks.js", sb.toString());
            return;
        }
        sb.append("StartupEvents.registry('block', event => {\n");
        for (Map.Entry<ResourceLocation, BlockSaveEntry> entry : STATE.entrySet()) {
            ResourceLocation id = entry.getKey();
            if (!WorkspacePaths.isLabOwned(id)) {
                continue;
            }
            appendCreatedBlock(sb, id.getPath(), entry.getValue());
        }
        sb.append("});\n\n");
        appendCreativeTabAdds(sb);
        ScriptWriter.write("startup_scripts", "blocks.js", sb.toString());
    }

    private static void appendCreativeTabAdds(StringBuilder sb) {
        Map<String, List<String>> adds = new LinkedHashMap<>();
        for (Map.Entry<ResourceLocation, BlockSaveEntry> entry : STATE.entrySet()) {
            ResourceLocation id = entry.getKey();
            String tab = entry.getValue().values().creativeTab();
            if (!WorkspacePaths.isLabOwned(id) || tab.isBlank()) {
                continue;
            }
            adds.computeIfAbsent(tab, key -> new ArrayList<>()).add(id.toString());
        }
        for (Map.Entry<String, List<String>> add : adds.entrySet()) {
            sb.append("StartupEvents.modifyCreativeTab('").append(ScriptEscaping.js(add.getKey())).append("', event => {\n");
            for (String blockId : add.getValue()) {
                sb.append("    event.add('").append(ScriptEscaping.js(blockId)).append("');\n");
            }
            sb.append("});\n");
        }
        sb.append("\n");
    }

    private static void appendCreatedBlock(StringBuilder sb, String path, BlockSaveEntry entry) {
        BlockFieldValues v = entry.values();
        sb.append("    event.create('").append(ScriptEscaping.js(path)).append("', '").append(ScriptEscaping.js(entry.type())).append("')\n");
        if (!v.displayName().isBlank()) {
            sb.append("        .displayName('").append(ScriptEscaping.js(v.displayName())).append("')\n");
        }
        appendTextures(sb, path, entry.type(), v);
        if (v.unbreakable()) {
            sb.append("        .unbreakable()\n");
        } else {
            if (v.hardness() != BlockFieldValues.DEFAULT_HARDNESS) {
                sb.append("        .hardness(").append(ScriptEscaping.fmt(v.hardness())).append(")\n");
            }
            if (v.resistance() != BlockFieldValues.DEFAULT_RESISTANCE) {
                sb.append("        .resistance(").append(ScriptEscaping.fmt(v.resistance())).append(")\n");
            }
        }
        if (v.lightLevel() > 0) {
            sb.append("        .lightLevel(").append(v.lightLevel()).append(")\n");
        }
        if (!v.soundType().isBlank() && !"wood".equals(v.soundType())) {
            sb.append("        .soundType('").append(ScriptEscaping.js(v.soundType())).append("')\n");
        }
        if (v.requiresTool()) {
            sb.append("        .requiresTool(true)\n");
        }
        if (!v.opaque()) {
            sb.append("        .opaque(false)\n");
        }
        if (v.notSolid()) {
            sb.append("        .notSolid()\n");
        }
        if (v.noCollision()) {
            sb.append("        .noCollision()\n");
        }
        if (v.waterlogged()) {
            sb.append("        .waterlogged()\n");
        }
        if (v.noDrops() || hasCustomLoot(v)) {
            sb.append("        .noDrops()\n");
        }
        if ("falling".equals(entry.type()) && !v.dustColor().isBlank()) {
            Long hex = parseHex(v.dustColor());
            if (hex != null) {
                sb.append("        .dustColor(0x").append(Long.toHexString(hex)).append(")\n");
            }
        }
        if (("button".equals(entry.type()) || "pressure_plate".equals(entry.type()))
                && !v.blockSetType().isBlank()) {
            sb.append("        .behaviour('").append(ScriptEscaping.js(v.blockSetType())).append("')\n");
        }
        if ("fence_gate".equals(entry.type()) && !v.woodType().isBlank()) {
            sb.append("        .behaviour('").append(ScriptEscaping.js(v.woodType())).append("')\n");
        }
        if (v.slipperiness() > 0) {
            sb.append("        .slipperiness(").append(ScriptEscaping.fmt(v.slipperiness())).append(")\n");
        }
        if (v.speedFactor() > 0) {
            sb.append("        .speedFactor(").append(ScriptEscaping.fmt(v.speedFactor())).append(")\n");
        }
        if (v.jumpFactor() > 0) {
            sb.append("        .jumpFactor(").append(ScriptEscaping.fmt(v.jumpFactor())).append(")\n");
        }
        for (String tag : entry.tags()) {
            sb.append("        .tagBoth('").append(ScriptEscaping.js(tag)).append("')\n");
        }
        sb.append("        ;\n\n");
    }

    private static void appendTextures(StringBuilder sb, String path, String type, BlockFieldValues v) {
        String base = "kubejs:block/" + path;
        switch (type) {
            case "detector" -> {
            }
            case "crop" -> {
                for (int age = 0; age < CROP_AGES; age++) {
                    sb.append("        .texture('").append(age).append("', '").append(ScriptEscaping.js(base + age)).append("')\n");
                }
            }
            case "cardinal" -> {
                if (!v.textureAll().isBlank()) {
                    sb.append("        .textureAll('").append(ScriptEscaping.js(base)).append("')\n");
                    sb.append("        .texture('front', '").append(ScriptEscaping.js(base)).append("')\n");
                }
                if (!v.textureTop().isBlank()) {
                    sb.append("        .texture('top', '").append(ScriptEscaping.js(base + "_top")).append("')\n");
                }
                if (!v.textureBottom().isBlank()) {
                    sb.append("        .texture('bottom', '").append(ScriptEscaping.js(base + "_bottom")).append("')\n");
                }
                if (!v.textureSides().isBlank()) {
                    sb.append("        .texture('side', '").append(ScriptEscaping.js(base + "_side")).append("')\n");
                }
            }
            default -> {
                if (!v.textureAll().isBlank()) {
                    sb.append("        .textureAll('").append(ScriptEscaping.js(base)).append("')\n");
                }
                if ("basic".equals(type)) {
                    if (!v.textureTop().isBlank()) {
                        sb.append("        .texture('up', '").append(ScriptEscaping.js(base + "_top")).append("')\n");
                    }
                    if (!v.textureBottom().isBlank()) {
                        sb.append("        .texture('down', '").append(ScriptEscaping.js(base + "_bottom")).append("')\n");
                    }
                    if (!v.textureSides().isBlank()) {
                        String side = ScriptEscaping.js(base + "_side");
                        for (String direction : List.of("north", "south", "east", "west")) {
                            sb.append("        .texture('").append(direction).append("', '").append(side)
                                    .append("')\n");
                        }
                    }
                }
            }
        }
    }

    private static void writeModificationScript() throws IOException {
        boolean any = false;
        for (Map.Entry<ResourceLocation, BlockSaveEntry> entry : STATE.entrySet()) {
            BlockSaveEntry e = entry.getValue();
            if (!WorkspacePaths.isLabOwned(entry.getKey())
                    && (e.status() == BlockStatus.MODIFIED || e.status() == BlockStatus.DISABLED)) {
                any = true;
                break;
            }
        }
        if (!any) {
            ScriptWriter.write("startup_scripts", "modified_blocks.js", "");
            return;
        }
        StringBuilder sb = new StringBuilder("BlockEvents.modification(event => {\n");
        for (Map.Entry<ResourceLocation, BlockSaveEntry> item : STATE.entrySet()) {
            ResourceLocation id = item.getKey();
            BlockSaveEntry entry = item.getValue();
            if (WorkspacePaths.isLabOwned(id)
                    || (entry.status() != BlockStatus.MODIFIED && entry.status() != BlockStatus.DISABLED)) {
                continue;
            }
            BlockFieldValues v = entry.values();
            sb.append("    event.modify('").append(id).append("', block => {\n");
            if (v.unbreakable()) {
                sb.append("        block.setDestroySpeed(-1);\n");
            } else if (v.hardness() != BlockFieldValues.DEFAULT_HARDNESS) {
                sb.append("        block.setDestroySpeed(").append(ScriptEscaping.fmt(v.hardness())).append(");\n");
            }
            if (v.resistance() != BlockFieldValues.DEFAULT_RESISTANCE) {
                sb.append("        block.setExplosionResistance(").append(ScriptEscaping.fmt(v.resistance())).append(");\n");
            }
            if (v.lightLevel() > 0) {
                sb.append("        block.setLightEmission(").append(v.lightLevel()).append(");\n");
            }
            if (v.requiresTool()) {
                sb.append("        block.setRequiresTool(true);\n");
            }
            if (v.noCollision()) {
                sb.append("        block.setHasCollision(false);\n");
            }
            if (v.slipperiness() > 0) {
                sb.append("        block.setFriction(").append(ScriptEscaping.fmt(v.slipperiness())).append(");\n");
            }
            if (v.speedFactor() > 0) {
                sb.append("        block.setSpeedFactor(").append(ScriptEscaping.fmt(v.speedFactor())).append(");\n");
            }
            if (v.jumpFactor() > 0) {
                sb.append("        block.setJumpFactor(").append(ScriptEscaping.fmt(v.jumpFactor())).append(");\n");
            }
            sb.append("    });\n");
        }
        sb.append("});\n");
        ScriptWriter.write("startup_scripts", "modified_blocks.js", sb.toString());
    }

    private static void writeServerScript() throws IOException {
        StringBuilder sb = new StringBuilder("ServerEvents.recipes(event => {\n");
        STATE.entrySet().stream().filter(e -> e.getValue().actions().contains(BlockAction.REMOVE_RECIPES))
                .map(e -> e.getKey().toString()).sorted()
                .forEach(id -> sb.append("    event.remove({ output: '").append(id).append("' });\n"));
        sb.append("});\n\n");
        appendLootHandlers(sb);
        ScriptWriter.write("server_scripts", "disabled_blocks.js", sb.toString());
    }

    private static void appendLootHandlers(StringBuilder sb) {
        for (Map.Entry<ResourceLocation, BlockSaveEntry> entry : STATE.entrySet()) {
            ResourceLocation id = entry.getKey();
            BlockFieldValues v = entry.getValue().values();
            if (!WorkspacePaths.isLabOwned(id) || v.lootItem().isBlank() || v.noDrops()) {
                continue;
            }
            int min = Math.max(0, Math.min(64, v.lootCountMin()));
            int max = Math.max(min, Math.min(64, v.lootCountMax()));
            float chance = Math.max(0f, Math.min(100f, v.lootChance())) / 100f;
            sb.append("BlockEvents.broken('").append(id).append("', event => {\n");
            if (chance < 1f) {
                sb.append("    if (Math.random() >= ").append(ScriptEscaping.fmt(chance)).append(") return;\n");
            }
            sb.append("    const count = ").append(min).append(" + Math.floor(Math.random() * ")
                    .append(max - min + 1).append(");\n");
            sb.append("    if (count > 0) event.block.popItem('").append(ScriptEscaping.js(v.lootItem())).append("', count);\n");
            sb.append("});\n\n");
        }
    }

    private static void writeCreativeHideScript() throws IOException {
        boolean any = false;
        for (BlockSaveEntry entry : STATE.values()) {
            if (entry.actions().contains(BlockAction.HIDE_CREATIVE_TAB)) {
                any = true;
                break;
            }
        }
        if (!any) {
            ScriptWriter.write("startup_scripts", "hidden_blocks.js", "");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (String tab : CREATIVE_TABS) {
            sb.append("StartupEvents.modifyCreativeTab('").append(tab).append("', event => {\n");
            for (Map.Entry<ResourceLocation, BlockSaveEntry> item : STATE.entrySet()) {
                if (item.getValue().actions().contains(BlockAction.HIDE_CREATIVE_TAB)) {
                    sb.append("    event.remove('").append(item.getKey()).append("');\n");
                }
            }
            sb.append("});\n");
        }
        sb.append("\n");
        ScriptWriter.write("startup_scripts", "hidden_blocks.js", sb.toString());
    }

    private static void writeClientScript() throws IOException {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<ResourceLocation, BlockSaveEntry> item : STATE.entrySet()) {
            if (item.getValue().actions().contains(BlockAction.HIDE_VIEWER)) {
                sb.append("JEIEvents.hideItems(event => {\n    event.hide('").append(item.getKey())
                        .append("');\n});\n");
                sb.append("REIEvents.hide(event => {\n    event.hide('").append(item.getKey()).append("');\n});\n");
            }
        }
        ScriptWriter.write("client_scripts", "blocks.js", sb.toString());
    }

    private static boolean copyTextures() throws IOException {
        boolean copied = false;
        Path root = WorkspacePaths.kubejsDir().resolve("assets").resolve("kubejs").resolve("textures");
        for (Map.Entry<ResourceLocation, BlockSaveEntry> item : STATE.entrySet()) {
            BlockSaveEntry entry = item.getValue();
            BlockFieldValues v = entry.values();
            String path = item.getKey().getPath();
            copied |= copyOne(root, v.textureAll(), texturesDir().resolve(path + ".png"));
            copied |= copyOne(root, v.textureTop(), texturesDir().resolve(path + "_top.png"));
            copied |= copyOne(root, v.textureBottom(), texturesDir().resolve(path + "_bottom.png"));
            copied |= copyOne(root, v.textureSides(), texturesDir().resolve(path + "_side.png"));
            if ("crop".equals(entry.type()) && !v.textureAll().isBlank()) {
                Path source = root.resolve(v.textureAll()).normalize();
                if (Files.isRegularFile(source)) {
                    for (int age = 0; age < CROP_AGES; age++) {
                        Path dest = texturesDir().resolve(path + age + ".png");
                        Files.createDirectories(dest.getParent());
                        Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
                        copied = true;
                    }
                }
            }
        }
        return copied;
    }

    private static boolean copyOne(Path root, String rel, Path dest) throws IOException {
        if (rel.isBlank()) {
            return false;
        }
        Path source = root.resolve(rel).normalize();
        if (source.equals(dest) || !source.startsWith(root.normalize()) || !Files.isRegularFile(source)
                || !"png".equals(extension(source.getFileName().toString()))) {
            return false;
        }
        Files.createDirectories(dest.getParent());
        Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
        return true;
    }

    private static String extension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase();
    }

    private static Path texturesDir() {
        return WorkspacePaths.kubejsDir().resolve("assets").resolve("kubejs").resolve("textures").resolve("block");
    }

    private static boolean hasCustomLoot(BlockFieldValues v) {
        return !v.lootItem().isBlank() && v.lootCountMax() > 0;
    }

    private static Long parseHex(String value) {
        try {
            String cleaned = value.trim().replace("#", "").replace("0x", "").replace("0X", "");
            long parsed = Long.parseLong(cleaned, 16);
            return Math.max(0, Math.min(0xFFFFFF, parsed));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static void loadStateIfNeeded() {
        if (stateLoaded) {
            return;
        }
        stateLoaded = true;
        JsonObject root = JsonStateFile.load(WorkspacePaths.blockStateFile());
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
                STATE.put(id, new BlockSaveEntry(type, status, name, wasModified, values, tags, actions));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static BlockFieldValues readValues(JsonObject obj) {
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

    private static String stringOr(JsonObject obj, String key) {
        return obj.has(key) ? obj.get(key).getAsString() : "";
    }

    private static int intOr(JsonObject obj, String key) {
        return obj.has(key) ? obj.get(key).getAsInt() : 0;
    }

    private static float floatOr(JsonObject obj, String key) {
        return obj.has(key) ? obj.get(key).getAsFloat() : 0f;
    }

    private static void saveState() throws IOException {
        JsonObject root = new JsonObject();
        for (Map.Entry<ResourceLocation, BlockSaveEntry> item : STATE.entrySet()) {
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

    private static void writeValues(JsonObject obj, BlockFieldValues v) {
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

    public static String[] soundTypes() {
        return SOUND_TYPES;
    }

    public static String[] blockSetTypes() {
        return BLOCK_SET_TYPES;
    }

    public static String[] woodTypes() {
        return WOOD_TYPES;
    }

    public static String[] creativeTabs() {
        return CREATIVE_TABS;
    }

    private record BlockSaveEntry(String type, BlockStatus status, String name, boolean wasModified,
            BlockFieldValues values, List<String> tags, List<BlockAction> actions) {
    }
}
