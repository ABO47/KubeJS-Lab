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
import com.abo47.kubejslab.workspace.ScriptEscaping;
import com.abo47.kubejslab.workspace.ScriptWriter;
import com.abo47.kubejslab.workspace.WorkspacePaths;

public final class BlockScriptWriter {

    private BlockScriptWriter() {
    }

    static void writeStartupScript(Map<ResourceLocation, BlockSaveEntry> states) throws IOException {
        StringBuilder sb = new StringBuilder();
        boolean any = false;
        for (ResourceLocation id : states.keySet()) {
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
        for (Map.Entry<ResourceLocation, BlockSaveEntry> entry : states.entrySet()) {
            ResourceLocation id = entry.getKey();
            if (!WorkspacePaths.isLabOwned(id)) {
                continue;
            }
            appendCreatedBlock(sb, id.getPath(), entry.getValue());
        }
        sb.append("});\n\n");
        appendCreativeTabAdds(states, sb);
        ScriptWriter.write("startup_scripts", "blocks.js", sb.toString());
    }

    static void appendCreativeTabAdds(Map<ResourceLocation, BlockSaveEntry> states, StringBuilder sb) {
        Map<String, List<String>> adds = new LinkedHashMap<>();
        for (Map.Entry<ResourceLocation, BlockSaveEntry> entry : states.entrySet()) {
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

    static void appendCreatedBlock(StringBuilder sb, String path, BlockSaveEntry entry) {
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

    static void appendTextures(StringBuilder sb, String path, String type, BlockFieldValues v) {
        String base = "kubejs:block/" + path;
        switch (type) {
            case "detector" -> {
            }
            case "crop" -> {
                for (int age = 0; age < BlockService.CROP_AGES; age++) {
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

    static void writeModificationScript(Map<ResourceLocation, BlockSaveEntry> states) throws IOException {
        boolean any = false;
        for (Map.Entry<ResourceLocation, BlockSaveEntry> entry : states.entrySet()) {
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
        for (Map.Entry<ResourceLocation, BlockSaveEntry> item : states.entrySet()) {
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

    static void writeServerScript(Map<ResourceLocation, BlockSaveEntry> states) throws IOException {
        StringBuilder sb = new StringBuilder("ServerEvents.recipes(event => {\n");
        states.entrySet().stream().filter(e -> e.getValue().actions().contains(BlockAction.REMOVE_RECIPES))
                .map(e -> e.getKey().toString()).sorted()
                .forEach(id -> sb.append("    event.remove({ output: '").append(id).append("' });\n"));
        sb.append("});\n\n");
        appendLootHandlers(states, sb);
        ScriptWriter.write("server_scripts", "disabled_blocks.js", sb.toString());
    }

    static void appendLootHandlers(Map<ResourceLocation, BlockSaveEntry> states, StringBuilder sb) {
        for (Map.Entry<ResourceLocation, BlockSaveEntry> entry : states.entrySet()) {
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

    static void writeCreativeHideScript(Map<ResourceLocation, BlockSaveEntry> states) throws IOException {
        boolean any = false;
        for (BlockSaveEntry entry : states.values()) {
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
        for (String tab : BlockService.CREATIVE_TABS) {
            sb.append("StartupEvents.modifyCreativeTab('").append(tab).append("', event => {\n");
            for (Map.Entry<ResourceLocation, BlockSaveEntry> item : states.entrySet()) {
                if (item.getValue().actions().contains(BlockAction.HIDE_CREATIVE_TAB)) {
                    sb.append("    event.remove('").append(item.getKey()).append("');\n");
                }
            }
            sb.append("});\n");
        }
        sb.append("\n");
        ScriptWriter.write("startup_scripts", "hidden_blocks.js", sb.toString());
    }

    static void writeClientScript(Map<ResourceLocation, BlockSaveEntry> states) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<ResourceLocation, BlockSaveEntry> item : states.entrySet()) {
            if (item.getValue().actions().contains(BlockAction.HIDE_VIEWER)) {
                sb.append("JEIEvents.hideItems(event => {\n    event.hide('").append(item.getKey())
                        .append("');\n});\n");
                sb.append("REIEvents.hide(event => {\n    event.hide('").append(item.getKey()).append("');\n});\n");
            }
        }
        ScriptWriter.write("client_scripts", "blocks.js", sb.toString());
    }

    static boolean hasCustomLoot(BlockFieldValues v) {
        return !v.lootItem().isBlank() && v.lootCountMax() > 0;
    }

    static Long parseHex(String value) {
        try {
            String cleaned = value.trim().replace("#", "").replace("0x", "").replace("0X", "");
            long parsed = Long.parseLong(cleaned, 16);
            return Math.max(0, Math.min(0xFFFFFF, parsed));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
