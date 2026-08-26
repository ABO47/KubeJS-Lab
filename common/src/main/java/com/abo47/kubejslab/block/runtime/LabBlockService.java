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
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.kubejslab.KubeJSLab;
import com.abo47.kubejslab.block.model.LabBlockAction;
import com.abo47.kubejslab.block.model.LabBlockEditAction;
import com.abo47.kubejslab.block.model.LabBlockFieldValues;
import com.abo47.kubejslab.block.model.LabBlockPayload;
import com.abo47.kubejslab.block.model.LabBlockState;
import com.abo47.kubejslab.block.model.LabBlockStatus;
import com.abo47.kubejslab.client.ui.base.LabGuiKeys;
import com.abo47.kubejslab.lab.LabPathResolver;
import com.abo47.kubejslab.lab.LabScriptWriter;
import com.abo47.kubejslab.lab.LabServerCommands;
import com.abo47.kubejslab.lab.LabStateFile;
import com.abo47.kubejslab.lab.LabUniqueNames;
import com.abo47.kubejslab.network.ModNetwork;
import com.abo47.kubejslab.network.block.S2CBlockStatePacket;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


public final class LabBlockService {
    private static final Map<ResourceLocation, LabBlockSaveEntry> STATE = new LinkedHashMap<>();
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

    private LabBlockService() {
    }

    public static void handle(ServerPlayer player, LabBlockEditAction action, ResourceLocation targetId,
            LabBlockPayload payload) {
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
            LabServerCommands.kubejsStartupReload(server);
            LabServerCommands.reload(server);
            if (copyTextures()) {
                LabServerCommands.kubejsTextureReload(server);
            }
            ModNetwork.sendBlockState(player, statePacket());
            if (!PENDING.isEmpty()) {
                player.sendSystemMessage(Component.translatable(LabGuiKeys.LAB_CHAT_RESTART_REQUIRED));
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
        Map<ResourceLocation, LabBlockState> states = new HashMap<>();
        for (Map.Entry<ResourceLocation, LabBlockSaveEntry> entry : STATE.entrySet()) {
            LabBlockSaveEntry e = entry.getValue();
            states.put(entry.getKey(), new LabBlockState(entry.getKey(), e.type(), e.status(),
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

    private static void saveNew(LabBlockPayload payload) {
        if (payload.type().isBlank()) {
            throw new IllegalArgumentException("Block type is required");
        }
        String baseName = LabUniqueNames.slugify(payload.values().displayName());
        if (baseName.isBlank()) {
            throw new IllegalArgumentException("Block display name is required");
        }
        ResourceLocation id = LabUniqueNames.uniqueId(LabUniqueNames.labId(baseName),
                existing -> STATE.containsKey(existing) || SESSION_CREATED_IDS.contains(existing));
        STATE.put(id, new LabBlockSaveEntry(payload.type(), LabBlockStatus.CREATED,
                payload.values().displayName(), false, payload.values(), payload.tags(), payload.actions()));
        SESSION_CREATED_IDS.add(id);
        PENDING.add(id);
    }

    private static void modify(ResourceLocation targetId, LabBlockPayload payload) {
        if (targetId == null) {
            return;
        }
        LabBlockSaveEntry existing = STATE.get(targetId);
        String name = payload.values().displayName().isBlank() && existing != null ? existing.name()
                : payload.values().displayName();
        STATE.put(targetId, new LabBlockSaveEntry(payload.type(), LabBlockStatus.MODIFIED, name, true,
                payload.values(), payload.tags(), payload.actions()));
        PENDING.add(targetId);
    }

    private static void duplicate(ResourceLocation targetId) {
        if (targetId == null) {
            return;
        }
        LabBlockSaveEntry source = STATE.get(targetId);
        if (source == null) {
            throw new IllegalArgumentException("Source block not found: " + targetId);
        }
        String base = targetId.getPath().substring("lab/".length()) + "_copy";
        ResourceLocation id = LabUniqueNames.uniqueId(LabUniqueNames.labId(base),
                existing -> STATE.containsKey(existing) || SESSION_CREATED_IDS.contains(existing));
        STATE.put(id, new LabBlockSaveEntry(source.type(), LabBlockStatus.CREATED, source.name(), false,
                source.values(), source.tags(), source.actions()));
        SESSION_CREATED_IDS.add(id);
        PENDING.add(id);
    }

    private static void disable(ResourceLocation targetId) {
        if (targetId == null) {
            return;
        }
        LabBlockSaveEntry entry = STATE.get(targetId);
        if (entry == null) {
            entry = new LabBlockSaveEntry("basic", LabBlockStatus.NORMAL, targetId.getPath(), false,
                    LabBlockFieldValues.defaults(), List.of(), List.of());
        }
        STATE.put(targetId, new LabBlockSaveEntry(entry.type(), LabBlockStatus.DISABLED, entry.name(),
                entry.wasModified(), entry.values(), entry.tags(), withHideActions(entry.actions(), true)));
        PENDING.add(targetId);
    }

    private static void enable(ResourceLocation targetId) {
        if (targetId == null) {
            return;
        }
        LabBlockSaveEntry entry = STATE.get(targetId);
        if (entry == null) {
            return;
        }
        if (entry.wasModified()) {
            STATE.put(targetId, new LabBlockSaveEntry(entry.type(), LabBlockStatus.MODIFIED, entry.name(),
                    true, entry.values(), entry.tags(), withHideActions(entry.actions(), false)));
        } else {
            STATE.remove(targetId);
        }
        PENDING.add(targetId);
    }

    private static List<LabBlockAction> withHideActions(List<LabBlockAction> source, boolean set) {
        List<LabBlockAction> actions = new ArrayList<>(source);
        for (LabBlockAction action : List.of(LabBlockAction.HIDE_CREATIVE_TAB, LabBlockAction.REMOVE_RECIPES,
                LabBlockAction.HIDE_VIEWER)) {
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
        if (!LabPathResolver.isLabOwned(targetId)) {
            deleteCopiedTextures(targetId.getPath());
        }
    }

    private static void delete(ResourceLocation targetId) throws IOException {
        if (targetId == null || !LabPathResolver.isLabOwned(targetId)) {
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
            if (LabPathResolver.isLabOwned(id)) {
                any = true;
                break;
            }
        }
        if (!any) {
            LabScriptWriter.write("startup_scripts", "blocks.js", sb.toString());
            return;
        }
        sb.append("StartupEvents.registry('block', event => {\n");
        for (Map.Entry<ResourceLocation, LabBlockSaveEntry> entry : STATE.entrySet()) {
            ResourceLocation id = entry.getKey();
            if (!LabPathResolver.isLabOwned(id)) {
                continue;
            }
            appendCreatedBlock(sb, id.getPath(), entry.getValue());
        }
        sb.append("});\n\n");
        appendCreativeTabAdds(sb);
        LabScriptWriter.write("startup_scripts", "blocks.js", sb.toString());
    }

    private static void appendCreativeTabAdds(StringBuilder sb) {
        Map<String, List<String>> adds = new LinkedHashMap<>();
        for (Map.Entry<ResourceLocation, LabBlockSaveEntry> entry : STATE.entrySet()) {
            ResourceLocation id = entry.getKey();
            String tab = entry.getValue().values().creativeTab();
            if (!LabPathResolver.isLabOwned(id) || tab.isBlank()) {
                continue;
            }
            adds.computeIfAbsent(tab, key -> new ArrayList<>()).add(id.toString());
        }
        for (Map.Entry<String, List<String>> add : adds.entrySet()) {
            sb.append("StartupEvents.modifyCreativeTab('").append(js(add.getKey())).append("', event => {\n");
            for (String blockId : add.getValue()) {
                sb.append("    event.add('").append(js(blockId)).append("');\n");
            }
            sb.append("});\n");
        }
        sb.append("\n");
    }

    private static void appendCreatedBlock(StringBuilder sb, String path, LabBlockSaveEntry entry) {
        LabBlockFieldValues v = entry.values();
        sb.append("    event.create('").append(js(path)).append("', '").append(js(entry.type())).append("')\n");
        if (!v.displayName().isBlank()) {
            sb.append("        .displayName('").append(js(v.displayName())).append("')\n");
        }
        appendTextures(sb, path, entry.type(), v);
        if (v.unbreakable()) {
            sb.append("        .unbreakable()\n");
        } else {
            if (v.hardness() != LabBlockFieldValues.DEFAULT_HARDNESS) {
                sb.append("        .hardness(").append(fmt(v.hardness())).append(")\n");
            }
            if (v.resistance() != LabBlockFieldValues.DEFAULT_RESISTANCE) {
                sb.append("        .resistance(").append(fmt(v.resistance())).append(")\n");
            }
        }
        if (v.lightLevel() > 0) {
            sb.append("        .lightLevel(").append(v.lightLevel()).append(")\n");
        }
        if (!v.soundType().isBlank() && !"wood".equals(v.soundType())) {
            sb.append("        .soundType('").append(js(v.soundType())).append("')\n");
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
            sb.append("        .behaviour('").append(js(v.blockSetType())).append("')\n");
        }
        if ("fence_gate".equals(entry.type()) && !v.woodType().isBlank()) {
            sb.append("        .behaviour('").append(js(v.woodType())).append("')\n");
        }
        if (v.slipperiness() > 0) {
            sb.append("        .slipperiness(").append(fmt(v.slipperiness())).append(")\n");
        }
        if (v.speedFactor() > 0) {
            sb.append("        .speedFactor(").append(fmt(v.speedFactor())).append(")\n");
        }
        if (v.jumpFactor() > 0) {
            sb.append("        .jumpFactor(").append(fmt(v.jumpFactor())).append(")\n");
        }
        for (String tag : entry.tags()) {
            sb.append("        .tagBoth('").append(js(tag)).append("')\n");
        }
        sb.append("        ;\n\n");
    }

    private static void appendTextures(StringBuilder sb, String path, String type, LabBlockFieldValues v) {
        String base = "kubejs:block/" + path;
        switch (type) {
            case "detector" -> {
            }
            case "crop" -> {
                for (int age = 0; age < CROP_AGES; age++) {
                    sb.append("        .texture('").append(age).append("', '").append(js(base + age)).append("')\n");
                }
            }
            case "cardinal" -> {
                if (!v.textureAll().isBlank()) {
                    sb.append("        .textureAll('").append(js(base)).append("')\n");
                    sb.append("        .texture('front', '").append(js(base)).append("')\n");
                }
                if (!v.textureTop().isBlank()) {
                    sb.append("        .texture('top', '").append(js(base + "_top")).append("')\n");
                }
                if (!v.textureBottom().isBlank()) {
                    sb.append("        .texture('bottom', '").append(js(base + "_bottom")).append("')\n");
                }
                if (!v.textureSides().isBlank()) {
                    sb.append("        .texture('side', '").append(js(base + "_side")).append("')\n");
                }
            }
            default -> {
                if (!v.textureAll().isBlank()) {
                    sb.append("        .textureAll('").append(js(base)).append("')\n");
                }
                if ("basic".equals(type)) {
                    if (!v.textureTop().isBlank()) {
                        sb.append("        .texture('up', '").append(js(base + "_top")).append("')\n");
                    }
                    if (!v.textureBottom().isBlank()) {
                        sb.append("        .texture('down', '").append(js(base + "_bottom")).append("')\n");
                    }
                    if (!v.textureSides().isBlank()) {
                        String side = js(base + "_side");
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
        for (Map.Entry<ResourceLocation, LabBlockSaveEntry> entry : STATE.entrySet()) {
            LabBlockSaveEntry e = entry.getValue();
            if (!LabPathResolver.isLabOwned(entry.getKey())
                    && (e.status() == LabBlockStatus.MODIFIED || e.status() == LabBlockStatus.DISABLED)) {
                any = true;
                break;
            }
        }
        if (!any) {
            LabScriptWriter.write("startup_scripts", "modified_blocks.js", "");
            return;
        }
        StringBuilder sb = new StringBuilder("BlockEvents.modification(event => {\n");
        for (Map.Entry<ResourceLocation, LabBlockSaveEntry> item : STATE.entrySet()) {
            ResourceLocation id = item.getKey();
            LabBlockSaveEntry entry = item.getValue();
            if (LabPathResolver.isLabOwned(id)
                    || (entry.status() != LabBlockStatus.MODIFIED && entry.status() != LabBlockStatus.DISABLED)) {
                continue;
            }
            LabBlockFieldValues v = entry.values();
            sb.append("    event.modify('").append(id).append("', block => {\n");
            if (v.unbreakable()) {
                sb.append("        block.setDestroySpeed(-1);\n");
            } else if (v.hardness() != LabBlockFieldValues.DEFAULT_HARDNESS) {
                sb.append("        block.setDestroySpeed(").append(fmt(v.hardness())).append(");\n");
            }
            if (v.resistance() != LabBlockFieldValues.DEFAULT_RESISTANCE) {
                sb.append("        block.setExplosionResistance(").append(fmt(v.resistance())).append(");\n");
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
                sb.append("        block.setFriction(").append(fmt(v.slipperiness())).append(");\n");
            }
            if (v.speedFactor() > 0) {
                sb.append("        block.setSpeedFactor(").append(fmt(v.speedFactor())).append(");\n");
            }
            if (v.jumpFactor() > 0) {
                sb.append("        block.setJumpFactor(").append(fmt(v.jumpFactor())).append(");\n");
            }
            sb.append("    });\n");
        }
        sb.append("});\n");
        LabScriptWriter.write("startup_scripts", "modified_blocks.js", sb.toString());
    }

    private static void writeServerScript() throws IOException {
        StringBuilder sb = new StringBuilder("ServerEvents.recipes(event => {\n");
        STATE.entrySet().stream().filter(e -> e.getValue().actions().contains(LabBlockAction.REMOVE_RECIPES))
                .map(e -> e.getKey().toString()).sorted()
                .forEach(id -> sb.append("    event.remove({ output: '").append(id).append("' });\n"));
        sb.append("});\n\n");
        appendLootHandlers(sb);
        LabScriptWriter.write("server_scripts", "disabled_blocks.js", sb.toString());
    }

    private static void appendLootHandlers(StringBuilder sb) {
        for (Map.Entry<ResourceLocation, LabBlockSaveEntry> entry : STATE.entrySet()) {
            ResourceLocation id = entry.getKey();
            LabBlockFieldValues v = entry.getValue().values();
            if (!LabPathResolver.isLabOwned(id) || v.lootItem().isBlank() || v.noDrops()) {
                continue;
            }
            int min = Math.max(0, Math.min(64, v.lootCountMin()));
            int max = Math.max(min, Math.min(64, v.lootCountMax()));
            float chance = Math.max(0f, Math.min(100f, v.lootChance())) / 100f;
            sb.append("BlockEvents.broken('").append(id).append("', event => {\n");
            if (chance < 1f) {
                sb.append("    if (Math.random() >= ").append(fmt(chance)).append(") return;\n");
            }
            sb.append("    const count = ").append(min).append(" + Math.floor(Math.random() * ")
                    .append(max - min + 1).append(");\n");
            sb.append("    if (count > 0) event.block.popItem('").append(js(v.lootItem())).append("', count);\n");
            sb.append("});\n\n");
        }
    }

    private static void writeCreativeHideScript() throws IOException {
        boolean any = false;
        for (LabBlockSaveEntry entry : STATE.values()) {
            if (entry.actions().contains(LabBlockAction.HIDE_CREATIVE_TAB)) {
                any = true;
                break;
            }
        }
        if (!any) {
            LabScriptWriter.write("startup_scripts", "hidden_blocks.js", "");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (String tab : CREATIVE_TABS) {
            sb.append("StartupEvents.modifyCreativeTab('").append(tab).append("', event => {\n");
            for (Map.Entry<ResourceLocation, LabBlockSaveEntry> item : STATE.entrySet()) {
                if (item.getValue().actions().contains(LabBlockAction.HIDE_CREATIVE_TAB)) {
                    sb.append("    event.remove('").append(item.getKey()).append("');\n");
                }
            }
            sb.append("});\n");
        }
        sb.append("\n");
        LabScriptWriter.write("startup_scripts", "hidden_blocks.js", sb.toString());
    }

    private static void writeClientScript() throws IOException {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<ResourceLocation, LabBlockSaveEntry> item : STATE.entrySet()) {
            if (item.getValue().actions().contains(LabBlockAction.HIDE_VIEWER)) {
                sb.append("JEIEvents.hideItems(event => {\n    event.hide('").append(item.getKey())
                        .append("');\n});\n");
                sb.append("REIEvents.hide(event => {\n    event.hide('").append(item.getKey()).append("');\n});\n");
            }
        }
        LabScriptWriter.write("client_scripts", "blocks.js", sb.toString());
    }

    private static boolean copyTextures() throws IOException {
        boolean copied = false;
        Path root = LabPathResolver.kubejsDir().resolve("assets").resolve("kubejs").resolve("textures");
        for (Map.Entry<ResourceLocation, LabBlockSaveEntry> item : STATE.entrySet()) {
            LabBlockSaveEntry entry = item.getValue();
            LabBlockFieldValues v = entry.values();
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
        return LabPathResolver.kubejsDir().resolve("assets").resolve("kubejs").resolve("textures").resolve("block");
    }

    private static String js(String s) {
        return s.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n");
    }

    private static String fmt(float f) {
        return f == (int) f ? Integer.toString((int) f) : Float.toString(f);
    }

    private static boolean hasCustomLoot(LabBlockFieldValues v) {
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
        JsonObject root = LabStateFile.load(LabPathResolver.blockStateFile());
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
                LabBlockStatus status = LabBlockStatus.valueOf(obj.get("status").getAsString());
                String type = obj.has("type") ? obj.get("type").getAsString() : "basic";
                String name = obj.has("name") ? obj.get("name").getAsString() : "";
                boolean wasModified = obj.has("wasModified") && obj.get("wasModified").getAsBoolean();
                LabBlockFieldValues values = readValues(obj.getAsJsonObject("values"));
                List<String> tags = new ArrayList<>();
                if (obj.has("tags")) {
                    for (JsonElement el : obj.getAsJsonArray("tags")) {
                        tags.add(el.getAsString());
                    }
                }
                List<LabBlockAction> actions = new ArrayList<>();
                if (obj.has("actions")) {
                    for (JsonElement el : obj.getAsJsonArray("actions")) {
                        actions.add(LabBlockAction.valueOf(el.getAsString()));
                    }
                }
                STATE.put(id, new LabBlockSaveEntry(type, status, name, wasModified, values, tags, actions));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static LabBlockFieldValues readValues(JsonObject obj) {
        return new LabBlockFieldValues(obj.get("displayName").getAsString(), obj.get("textureAll").getAsString(),
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
        for (Map.Entry<ResourceLocation, LabBlockSaveEntry> item : STATE.entrySet()) {
            LabBlockSaveEntry entry = item.getValue();
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
            for (LabBlockAction action : entry.actions()) {
                actions.add(action.name());
            }
            obj.add("actions", actions);
            root.add(item.getKey().toString(), obj);
        }
        LabStateFile.save(LabPathResolver.blockStateFile(), root);
    }

    private static void writeValues(JsonObject obj, LabBlockFieldValues v) {
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

    private record LabBlockSaveEntry(String type, LabBlockStatus status, String name, boolean wasModified,
            LabBlockFieldValues values, List<String> tags, List<LabBlockAction> actions) {
    }
}
