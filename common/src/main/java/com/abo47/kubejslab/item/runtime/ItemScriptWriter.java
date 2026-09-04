package com.abo47.kubejslab.item.runtime;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;

import com.abo47.kubejslab.KubeJSLab;
import com.abo47.kubejslab.item.model.CustomTier;
import com.abo47.kubejslab.item.model.ItemAction;
import com.abo47.kubejslab.item.model.ItemFieldValues;
import com.abo47.kubejslab.item.model.ItemStatus;
import com.abo47.kubejslab.workspace.ScriptEscaping;
import com.abo47.kubejslab.workspace.ScriptWriter;
import com.abo47.kubejslab.workspace.WorkspacePaths;

public final class ItemScriptWriter {

    private ItemScriptWriter() {
    }

    static void writeStartupScript(Map<ResourceLocation, ItemSaveEntry> states) throws IOException {
        StringBuilder sb = new StringBuilder();
        writeTierBlocks(states, sb);
        writeRegistrySection(states, sb);
        writeModificationSection(states, sb);
        writeBehaviorSection(states, sb);
        writeCreativeHideSection(states, sb);
        ScriptWriter.write("startup_scripts", "items.js", sb.toString());
    }

    static void writeTierBlocks(Map<ResourceLocation, ItemSaveEntry> states, StringBuilder sb) {
        Map<String, CustomTier> tiers = new TreeMap<>();
        for (ItemSaveEntry entry : states.values()) {
            if (entry.customTier() != null) {
                tiers.putIfAbsent(entry.customTier().id(), entry.customTier());
            }
        }
        if (tiers.isEmpty()) {
            return;
        }
        for (CustomTier tier : tiers.values()) {
            if (tier.armor()) {
                sb.append("ItemEvents.armorTierRegistry(event => {\n");
                sb.append("    event.add('").append(ScriptEscaping.js(tier.id())).append("', tier => {\n");
                if (tier.durabilityMultiplier() > 0) {
                    sb.append("        tier.setDurabilityMultiplier(").append(ScriptEscaping.fmt(tier.durabilityMultiplier()))
                            .append(");\n");
                }
                if (tier.protections()[0] > 0 || tier.protections()[1] > 0 || tier.protections()[2] > 0
                        || tier.protections()[3] > 0) {
                    sb.append("        tier.setSlotProtections([").append(tier.protections()[0]).append(", ")
                            .append(tier.protections()[1]).append(", ").append(tier.protections()[2]).append(", ")
                            .append(tier.protections()[3]).append("]);\n");
                }
                if (tier.enchantValue() > 0) {
                    sb.append("        tier.setEnchantmentValue(").append(tier.enchantValue()).append(");\n");
                }
                if (!tier.equipSound().isBlank()) {
                    sb.append("        tier.setEquipSound('").append(ScriptEscaping.js(tier.equipSound())).append("');\n");
                }
                if (!tier.repairIngredient().isBlank()) {
                    sb.append("        tier.setRepairIngredient('").append(ScriptEscaping.js(tier.repairIngredient())).append("');\n");
                }
                if (tier.toughness() > 0) {
                    sb.append("        tier.setToughness(").append(ScriptEscaping.fmt(tier.toughness())).append(");\n");
                }
                if (tier.knockbackResistance() > 0) {
                    sb.append("        tier.setKnockbackResistance(").append(ScriptEscaping.fmt(tier.knockbackResistance()))
                            .append(");\n");
                }
                sb.append("    });\n");
                sb.append("});\n\n");
            } else {
                sb.append("ItemEvents.toolTierRegistry(event => {\n");
                sb.append("    event.add('").append(ScriptEscaping.js(tier.id())).append("', tier => {\n");
                if (tier.uses() > 0) {
                    sb.append("        tier.setUses(").append(tier.uses()).append(");\n");
                }
                if (tier.speed() > 0) {
                    sb.append("        tier.setSpeed(").append(ScriptEscaping.fmt(tier.speed())).append(");\n");
                }
                if (tier.attackDamageBonus() != 0) {
                    sb.append("        tier.setAttackDamageBonus(").append(ScriptEscaping.fmt(tier.attackDamageBonus())).append(");\n");
                }
                if (tier.level() > 0) {
                    sb.append("        tier.setLevel(").append(tier.level()).append(");\n");
                }
                if (tier.enchantValue() > 0) {
                    sb.append("        tier.setEnchantmentValue(").append(tier.enchantValue()).append(");\n");
                }
                if (!tier.repairIngredient().isBlank()) {
                    sb.append("        tier.setRepairIngredient('").append(ScriptEscaping.js(tier.repairIngredient())).append("');\n");
                }
                sb.append("    });\n");
                sb.append("});\n\n");
            }
        }
    }

    static void writeRegistrySection(Map<ResourceLocation, ItemSaveEntry> states, StringBuilder sb) {
        boolean any = false;
        for (ResourceLocation id : states.keySet()) {
            if (WorkspacePaths.isLabOwned(id)) {
                any = true;
                break;
            }
        }
        if (!any) {
            return;
        }
        sb.append("StartupEvents.registry('item', event => {\n");
        for (Map.Entry<ResourceLocation, ItemSaveEntry> item : states.entrySet()) {
            ResourceLocation id = item.getKey();
            if (!WorkspacePaths.isLabOwned(id)) {
                continue;
            }
            ItemSaveEntry entry = item.getValue();
            ItemFieldValues v = entry.values();
            sb.append("    event.create('").append(ScriptEscaping.js(id.getPath())).append("', '").append(ScriptEscaping.js(entry.type()))
                    .append("')\n");
            if (!v.texture().isBlank()) {
                sb.append("        .texture('kubejs:item/").append(id.getPath()).append("')\n");
            }
            if (!v.displayName().isBlank()) {
                sb.append("        .displayName('").append(ScriptEscaping.js(v.displayName())).append("')\n");
            }
            if (ItemService.TOOL_TYPES.contains(entry.type())) {
                if (!v.toolTier().isBlank()) {
                    sb.append("        .tier('").append(ScriptEscaping.js(v.toolTier())).append("')\n");
                }
                if (v.attackDamageBaseline() != 0) {
                    sb.append("        .attackDamageBaseline(").append(ScriptEscaping.fmt(v.attackDamageBaseline())).append(")\n");
                }
                if (v.speedBaseline() != 0) {
                    sb.append("        .speedBaseline(").append(ScriptEscaping.fmt(v.speedBaseline())).append(")\n");
                }
            } else if (ItemService.ARMOR_TYPES.contains(entry.type()) && !v.armorTier().isBlank()) {
                sb.append("        .tier('").append(ScriptEscaping.js(v.armorTier())).append("')\n");
            }
            if (v.maxStack() != 64) {
                sb.append("        .maxStackSize(").append(v.maxStack()).append(")\n");
            }
            if (v.maxDamage() > 0) {
                sb.append("        .maxDamage(").append(v.maxDamage()).append(")\n");
            }
            if (v.burnTime() > 0) {
                sb.append("        .burnTime(").append(v.burnTime()).append(")\n");
            }
            if (!v.containerItem().isBlank()) {
                sb.append("        .containerItem('").append(ScriptEscaping.js(v.containerItem())).append("')\n");
            }
            if (!v.rarity().isBlank()) {
                sb.append("        .rarity('").append(v.rarity().toUpperCase()).append("')\n");
            }
            if (v.glow()) {
                sb.append("        .glow()\n");
            }
            if (v.fireResistant()) {
                sb.append("        .fireResistant()\n");
            }
            if (hasFood(v)) {
                sb.append("        .food(");
                appendFoodInner(sb, "", v);
                sb.append(")\n");
            }
            if (!v.tooltip().isBlank()) {
                sb.append("        .tooltip('").append(ScriptEscaping.js(v.tooltip())).append("')\n");
            }
            for (String tag : entry.tags()) {
                sb.append("        .tag('").append(ScriptEscaping.js(tag)).append("')\n");
            }
            if (!v.attributeId().isBlank()) {
                sb.append("        .modifyAttribute('").append(ScriptEscaping.js(v.attributeId())).append("', '")
                        .append(ScriptEscaping.js(v.attributeName())).append("', ").append(ScriptEscaping.fmt(v.attributeAmount())).append(", '")
                        .append(operationName(v.attributeOperation())).append("')\n");
            }
            sb.append("        ;\n");
        }
        sb.append("});\n\n");
    }

    static void writeModificationSection(Map<ResourceLocation, ItemSaveEntry> states, StringBuilder sb) {
        boolean any = false;
        for (Map.Entry<ResourceLocation, ItemSaveEntry> item : states.entrySet()) {
            ResourceLocation id = item.getKey();
            ItemSaveEntry entry = item.getValue();
            if (!WorkspacePaths.isLabOwned(id)
                    && (entry.status() == ItemStatus.MODIFIED || entry.status() == ItemStatus.DISABLED)) {
                any = true;
                break;
            }
        }
        if (!any) {
            return;
        }
        sb.append("ItemEvents.modification(event => {\n");
        for (Map.Entry<ResourceLocation, ItemSaveEntry> item : states.entrySet()) {
            ResourceLocation id = item.getKey();
            ItemSaveEntry entry = item.getValue();
            if (WorkspacePaths.isLabOwned(id)
                    || (entry.status() != ItemStatus.MODIFIED && entry.status() != ItemStatus.DISABLED)) {
                continue;
            }
            ItemFieldValues v = entry.values();
            sb.append("    event.modify('").append(id).append("', item => {\n");
            if (v.maxStack() != 64) {
                sb.append("        item.setMaxStackSize(").append(v.maxStack()).append(");\n");
            }
            if (v.maxDamage() > 0) {
                sb.append("        item.setMaxDamage(").append(v.maxDamage()).append(");\n");
            }
            if (v.burnTime() > 0) {
                sb.append("        item.setBurnTime(").append(v.burnTime()).append(");\n");
            }
            if (!v.rarity().isBlank()) {
                sb.append("        item.setRarity('").append(v.rarity().toUpperCase()).append("');\n");
            }
            if (v.fireResistant()) {
                sb.append("        item.setFireResistant(true);\n");
            }
            if (!v.containerItem().isBlank()) {
                sb.append("        item.setCraftingRemainder('").append(ScriptEscaping.js(v.containerItem())).append("');\n");
            }
            if (hasFood(v)) {
                sb.append("        item.setFoodProperties(");
                appendFoodInner(sb, "", v);
                sb.append(");\n");
            }
            if (v.attackDamageBaseline() != 0) {
                sb.append("        item.setAttackDamage(").append(ScriptEscaping.fmt(v.attackDamageBaseline())).append(");\n");
            }
            if (v.speedBaseline() != 0) {
                sb.append("        item.setAttackSpeed(").append(ScriptEscaping.fmt(v.speedBaseline())).append(");\n");
            }
            if (v.digSpeed() > 0) {
                sb.append("        item.setDigSpeed(").append(ScriptEscaping.fmt(v.digSpeed())).append(");\n");
            }
            if (v.armorProtection() > 0) {
                sb.append("        item.setArmorProtection(").append(v.armorProtection()).append(");\n");
            }
            if (v.armorToughness() > 0) {
                sb.append("        item.setArmorToughness(").append(ScriptEscaping.fmt(v.armorToughness())).append(");\n");
            }
            if (v.armorKnockback() > 0) {
                sb.append("        item.setArmorKnockbackResistance(").append(ScriptEscaping.fmt(v.armorKnockback())).append(");\n");
            }
            if (v.tierUses() > 0 || v.tierSpeed() > 0 || v.tierAttackDamageBonus() != 0 || v.tierLevel() > 0
                    || v.tierEnchantValue() > 0 || !v.tierRepairIngredient().isBlank()) {
                sb.append("        item.setTier(tier => {\n");
                if (v.tierUses() > 0) {
                    sb.append("            tier.setUses(").append(v.tierUses()).append(");\n");
                }
                if (v.tierSpeed() > 0) {
                    sb.append("            tier.setSpeed(").append(ScriptEscaping.fmt(v.tierSpeed())).append(");\n");
                }
                if (v.tierAttackDamageBonus() != 0) {
                    sb.append("            tier.setAttackDamageBonus(").append(ScriptEscaping.fmt(v.tierAttackDamageBonus()))
                            .append(");\n");
                }
                if (v.tierLevel() > 0) {
                    sb.append("            tier.setLevel(").append(v.tierLevel()).append(");\n");
                }
                if (v.tierEnchantValue() > 0) {
                    sb.append("            tier.setEnchantmentValue(").append(v.tierEnchantValue()).append(");\n");
                }
                if (!v.tierRepairIngredient().isBlank()) {
                    sb.append("            tier.setRepairIngredient('").append(ScriptEscaping.js(v.tierRepairIngredient()))
                            .append("');\n");
                }
                sb.append("        });\n");
            }
            sb.append("    });\n");
        }
        sb.append("});\n\n");
    }

    static void writeBehaviorSection(Map<ResourceLocation, ItemSaveEntry> states, StringBuilder sb) {
        boolean any = false;
        for (ItemSaveEntry entry : states.values()) {
            if (entry.actions().contains(ItemAction.CANCEL_USE)
                    || entry.actions().contains(ItemAction.GIVE_ITEM)
                    || entry.actions().contains(ItemAction.DAMAGE_ITEM)) {
                any = true;
                break;
            }
        }
        if (!any) {
            return;
        }
        for (Map.Entry<ResourceLocation, ItemSaveEntry> item : states.entrySet()) {
            ResourceLocation id = item.getKey();
            ItemSaveEntry entry = item.getValue();
            boolean cancel = entry.actions().contains(ItemAction.CANCEL_USE);
            boolean give = entry.actions().contains(ItemAction.GIVE_ITEM)
                    && !entry.values().behaviorItem().isBlank();
            boolean damage = entry.actions().contains(ItemAction.DAMAGE_ITEM)
                    && entry.values().behaviorDamage() > 0;
            if (!cancel && !give && !damage) {
                continue;
            }
            sb.append("ItemEvents.rightClicked('").append(id).append("', event => {\n");
            if (cancel) {
                sb.append("    event.cancel();\n");
            }
            if (give) {
                sb.append("    event.player.give('").append(ScriptEscaping.js(entry.values().behaviorItem())).append("');\n");
                sb.append("    event.success();\n");
            }
            if (damage) {
                sb.append("    event.item.hurtAndBreak(").append(entry.values().behaviorDamage())
                        .append(", event.player, null);\n");
                sb.append("    event.success();\n");
            }
            sb.append("});\n\n");
        }
    }

    static void writeCreativeHideSection(Map<ResourceLocation, ItemSaveEntry> states, StringBuilder sb) {
        boolean any = false;
        for (ItemSaveEntry entry : states.values()) {
            if (entry.actions().contains(ItemAction.HIDE_CREATIVE_TAB)) {
                any = true;
                break;
            }
        }
        if (!any) {
            return;
        }
        for (String tab : ItemService.CREATIVE_TABS) {
            sb.append("StartupEvents.modifyCreativeTab('").append(tab).append("', event => {\n");
            for (Map.Entry<ResourceLocation, ItemSaveEntry> item : states.entrySet()) {
                if (item.getValue().actions().contains(ItemAction.HIDE_CREATIVE_TAB)) {
                    sb.append("    event.remove('").append(item.getKey()).append("');\n");
                }
            }
            sb.append("});\n");
        }
        sb.append("\n");
    }

    static void appendFoodInner(StringBuilder sb, String indent, ItemFieldValues v) {
        sb.append(indent).append("food => {\n");
        if (v.foodHunger() > 0) {
            sb.append(indent).append("    food.hunger(").append(v.foodHunger()).append(");\n");
        }
        if (v.foodSaturation() > 0) {
            sb.append(indent).append("    food.saturation(").append(ScriptEscaping.fmt(v.foodSaturation())).append(");\n");
        }
        if (v.foodMeat()) {
            sb.append(indent).append("    food.meat();\n");
        }
        if (v.foodAlwaysEdible()) {
            sb.append(indent).append("    food.alwaysEdible();\n");
        }
        if (v.foodFastToEat()) {
            sb.append(indent).append("    food.fastToEat();\n");
        }
        if (!v.foodEffect().isBlank()) {
            sb.append(indent).append("    food.effect('").append(ScriptEscaping.js(v.foodEffect())).append("', ")
                    .append(v.foodEffectDuration()).append(", ").append(v.foodEffectAmplifier()).append(", ")
                    .append(ScriptEscaping.fmt(v.foodEffectChance())).append(");\n");
        }
        sb.append(indent).append("}");
    }

    static boolean hasFood(ItemFieldValues v) {
        return v.foodHunger() > 0 || v.foodSaturation() > 0 || v.foodMeat() || v.foodFastToEat()
                || v.foodAlwaysEdible() || !v.foodEffect().isBlank();
    }

    static void writeModelOverrides(Map<ResourceLocation, ItemSaveEntry> states) throws IOException {
        Path modelsDir = WorkspacePaths.kubejsDir().resolve("assets").resolve("minecraft").resolve("models")
                .resolve("item");
        boolean any = false;
        for (Map.Entry<ResourceLocation, ItemSaveEntry> item : states.entrySet()) {
            ItemSaveEntry entry = item.getValue();
            if (item.getKey().getNamespace().equals("kubejs") || entry.values().texture().isBlank()) {
                continue;
            }
            Path modelFile = modelsDir.resolve(item.getKey().getPath() + ".json");
            if (Files.isRegularFile(modelFile)) {
                continue;
            }
            Files.createDirectories(modelsDir);
            Item registered = BuiltInRegistries.ITEM.get(item.getKey());
            boolean handheld = registered instanceof SwordItem || registered instanceof PickaxeItem
                    || registered instanceof AxeItem || registered instanceof ShovelItem
                    || registered instanceof HoeItem || registered instanceof ShearsItem;
            String parent = handheld ? "minecraft:item/handheld" : "minecraft:item/generated";
            String layer0 = "kubejs:item/" + item.getKey().getPath();
            String content = "{\n  \"parent\": \"" + parent + "\",\n  \"textures\": {\n    \"layer0\": \""
                    + layer0 + "\"\n  }\n}\n";
            Files.writeString(modelFile, content);
            any = true;
        }
        if (any) {
            KubeJSLab.LOGGER.info("[ItemService] wrote model overrides");
        }
    }

    static void writeServerScript(Map<ResourceLocation, ItemSaveEntry> states) throws IOException {
        StringBuilder sb = new StringBuilder("ServerEvents.recipes(event => {\n");
        states.entrySet().stream().filter(e -> e.getValue().actions().contains(ItemAction.REMOVE_RECIPES))
                .map(e -> e.getKey().toString()).sorted()
                .forEach(id -> sb.append("    event.remove({ output: '").append(id).append("' });\n"));
        sb.append("});\n");
        ScriptWriter.write("server_scripts", "disabled_items.js", sb.toString());
    }

    static void writeClientScript(Map<ResourceLocation, ItemSaveEntry> states) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<ResourceLocation, ItemSaveEntry> item : states.entrySet()) {
            if (item.getValue().actions().contains(ItemAction.HIDE_VIEWER)) {
                sb.append("JEIEvents.hideItems(event => {\n    event.hide('").append(item.getKey())
                        .append("');\n});\n");
                sb.append("REIEvents.hide(event => {\n    event.hide('").append(item.getKey()).append("');\n});\n");
            }
        }
        ScriptWriter.write("client_scripts", "items.js", sb.toString());
    }

    static String operationName(String value) {
        return switch (value) {
            case "MULTIPLY_BASE" -> "MULTIPLY_BASE";
            case "MULTIPLY_TOTAL" -> "MULTIPLY_TOTAL";
            default -> "ADDITION";
        };
    }
}
