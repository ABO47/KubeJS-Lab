package com.abo47.kubejslab.item.runtime;

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
import java.util.TreeMap;

import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;

import com.abo47.kubejslab.KubeJSLab;
import com.abo47.kubejslab.client.ui.base.LabGuiKeys;
import com.abo47.kubejslab.item.model.LabCustomTier;
import com.abo47.kubejslab.item.model.LabItemAction;
import com.abo47.kubejslab.item.model.LabItemEditAction;
import com.abo47.kubejslab.item.model.LabItemFieldValues;
import com.abo47.kubejslab.item.model.LabItemPayload;
import com.abo47.kubejslab.item.model.LabItemState;
import com.abo47.kubejslab.item.model.LabItemStatus;
import com.abo47.kubejslab.lab.LabPathResolver;
import com.abo47.kubejslab.lab.LabScriptWriter;
import com.abo47.kubejslab.lab.LabServerCommands;
import com.abo47.kubejslab.lab.LabStateFile;
import com.abo47.kubejslab.lab.LabUniqueNames;
import com.abo47.kubejslab.network.ModNetwork;
import com.abo47.kubejslab.network.item.S2CItemStatePacket;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class LabItemService {
    private static final Map<ResourceLocation, LabItemSaveEntry> STATE = new LinkedHashMap<>();
    private static final Set<ResourceLocation> SESSION_CREATED_IDS = new HashSet<>();
    private static final Set<ResourceLocation> PENDING = new HashSet<>();
    private static boolean stateLoaded;

    private static final Set<String> TOOL_TYPES = Set.of("sword", "pickaxe", "axe", "shovel", "hoe", "shears");
    private static final Set<String> ARMOR_TYPES = Set.of("helmet", "chestplate", "leggings", "boots");
    private static final String[] CREATIVE_TABS = {
            "minecraft:building_blocks", "minecraft:colored_blocks", "minecraft:natural_blocks",
            "minecraft:functional_blocks", "minecraft:redstone_blocks", "minecraft:hotbar",
            "minecraft:search", "minecraft:tools_and_utilities", "minecraft:combat",
            "minecraft:food_and_drinks", "minecraft:ingredients", "minecraft:spawn_eggs",
            "minecraft:op_blocks", "minecraft:misc"
    };

    private LabItemService() {
    }

    public static void handle(ServerPlayer player, LabItemEditAction action, ResourceLocation targetId,
            LabItemPayload payload) {
        KubeJSLab.LOGGER.info(
                "[LabItemService] handle: action={}, targetId={}, type={}, displayName={}, values={}, tags={}, actions={}",
                action, targetId, payload.type(), payload.values().displayName(), payload.values(),
                payload.tags().size(), payload.actions().size());
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
            writeModelOverrides();
            writeServerScript();
            writeClientScript();
            MinecraftServer server = player.getServer();
            LabServerCommands.kubejsStartupReload(server);
            LabServerCommands.reload(server);
            if (copyTextures()) {
                LabServerCommands.kubejsTextureReload(server);
            }
            KubeJSLab.LOGGER.info("[LabItemService] sent /kubejs reload startup_scripts and /reload after {}", action);
            ModNetwork.sendItemState(player, statePacket());
            if (!PENDING.isEmpty()) {
                player.sendSystemMessage(Component.translatable(LabGuiKeys.LAB_CHAT_RESTART_REQUIRED));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
            player.sendSystemMessage(Component.literal("Failed to save item: " + e.getMessage()));
        }
    }

    public static S2CItemStatePacket statePacket() {
        loadStateIfNeeded();
        Map<ResourceLocation, LabItemState> states = new HashMap<>();
        for (Map.Entry<ResourceLocation, LabItemSaveEntry> entry : STATE.entrySet()) {
            LabItemSaveEntry e = entry.getValue();
            states.put(entry.getKey(), new LabItemState(entry.getKey(), e.type(), e.status(), PENDING.contains(entry.getKey()),
                    e.name(), e.wasModified(), e.customTier(), e.values(), e.tags(), e.actions()));
        }
        List<ResourceLocation> pendingOnly = new ArrayList<>();
        for (ResourceLocation id : PENDING) {
            if (!states.containsKey(id)) {
                pendingOnly.add(id);
            }
        }
        return new S2CItemStatePacket(states, pendingOnly);
    }

    private static void saveNew(LabItemPayload payload) throws IOException {
        if (payload.type().isBlank()) {
            throw new IllegalArgumentException("Item type is required");
        }
        String baseName = LabUniqueNames.slugify(payload.values().displayName());
        if (baseName.isBlank()) {
            throw new IllegalArgumentException("Item display name is required");
        }
        ResourceLocation id = LabUniqueNames.uniqueId(LabUniqueNames.labId(baseName),
                existing -> STATE.containsKey(existing) || SESSION_CREATED_IDS.contains(existing));
        LabCustomTier tier = tierFor(payload, id);
        STATE.put(id, new LabItemSaveEntry(payload.type(), LabItemStatus.CREATED, payload.values().displayName(),
                false, tier, payload.values(), payload.tags(), payload.actions()));
        SESSION_CREATED_IDS.add(id);
        PENDING.add(id);
        KubeJSLab.LOGGER.info("[LabItemService] SAVE_NEW created {} with displayName={}", id,
                payload.values().displayName());
    }

    private static void modify(ResourceLocation targetId, LabItemPayload payload) {
        if (targetId == null) {
            return;
        }
        LabItemSaveEntry existing = STATE.get(targetId);
        String name = payload.values().displayName().isBlank() && existing != null ? existing.name()
                : payload.values().displayName();
        LabCustomTier tier = existing != null && existing.customTier() != null ? existing.customTier()
                : tierFor(payload, targetId);
        STATE.put(targetId, new LabItemSaveEntry(payload.type(), LabItemStatus.MODIFIED, name, true, tier,
                payload.values(), payload.tags(), payload.actions()));
        PENDING.add(targetId);
        KubeJSLab.LOGGER.info("[LabItemService] MODIFY wrote {}", targetId);
    }

    private static void duplicate(ResourceLocation targetId) {
        if (targetId == null) {
            return;
        }
        LabItemSaveEntry source = STATE.get(targetId);
        if (source == null) {
            throw new IllegalArgumentException("Source item not found: " + targetId);
        }
        String base = targetId.getPath().substring("lab/".length()) + "_copy";
        ResourceLocation id = LabUniqueNames.uniqueId(LabUniqueNames.labId(base),
                existing -> STATE.containsKey(existing) || SESSION_CREATED_IDS.contains(existing));
        STATE.put(id, new LabItemSaveEntry(source.type(), LabItemStatus.CREATED, source.name(), false,
                source.customTier(), source.values(), source.tags(), source.actions()));
        SESSION_CREATED_IDS.add(id);
        PENDING.add(id);
        KubeJSLab.LOGGER.info("[LabItemService] DUPLICATE created {}", id);
    }

    private static void disable(ResourceLocation targetId) {
        if (targetId == null) {
            return;
        }
        LabItemSaveEntry entry = STATE.get(targetId);
        if (entry == null) {
            entry = new LabItemSaveEntry("basic", LabItemStatus.NORMAL, targetId.getPath(), false, null,
                    LabItemFieldValues.defaults(), List.of(), List.of());
        }
        STATE.put(targetId, new LabItemSaveEntry(entry.type(), LabItemStatus.DISABLED, entry.name(),
                entry.wasModified(), entry.customTier(), entry.values(), entry.tags(),
                withHideActions(entry.actions(), true)));
        PENDING.add(targetId);
        KubeJSLab.LOGGER.info("[LabItemService] DISABLE {}", targetId);
    }

    private static void enable(ResourceLocation targetId) {
        if (targetId == null) {
            return;
        }
        LabItemSaveEntry entry = STATE.get(targetId);
        if (entry == null) {
            return;
        }
        if (entry.wasModified()) {
            STATE.put(targetId, new LabItemSaveEntry(entry.type(), LabItemStatus.MODIFIED, entry.name(),
                    true, entry.customTier(), entry.values(), entry.tags(),
                    withHideActions(entry.actions(), false)));
        } else {
            STATE.remove(targetId);
        }
        PENDING.add(targetId);
        KubeJSLab.LOGGER.info("[LabItemService] ENABLE {}", targetId);
    }

    private static List<LabItemAction> withHideActions(List<LabItemAction> source, boolean set) {
        List<LabItemAction> actions = new ArrayList<>(source);
        for (LabItemAction action : List.of(LabItemAction.HIDE_CREATIVE_TAB, LabItemAction.REMOVE_RECIPES,
                LabItemAction.HIDE_VIEWER)) {
            if (set && !actions.contains(action)) {
                actions.add(action);
            } else if (!set) {
                actions.remove(action);
            }
        }
        return actions;
    }

    private static void reset(ResourceLocation targetId) {
        if (targetId == null) {
            return;
        }
        STATE.remove(targetId);
        PENDING.add(targetId);
        if (!LabPathResolver.isLabOwned(targetId)) {
            try {
                Files.deleteIfExists(textureFile(targetId));
            } catch (IOException e) {
                e.printStackTrace();
            }
            Path modelFile = LabPathResolver.kubejsDir().resolve("assets").resolve("minecraft").resolve("models")
                    .resolve("item").resolve(targetId.getPath() + ".json");
            try {
                Files.deleteIfExists(modelFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        KubeJSLab.LOGGER.info("[LabItemService] RESET {}", targetId);
    }

    private static void delete(ResourceLocation targetId) throws IOException {
        if (targetId == null || !LabPathResolver.isLabOwned(targetId)) {
            return;
        }
        STATE.remove(targetId);
        SESSION_CREATED_IDS.remove(targetId);
        PENDING.remove(targetId);
        try {
            Files.deleteIfExists(textureFile(targetId));
        } catch (IOException e) {
            e.printStackTrace();
        }
        KubeJSLab.LOGGER.info("[LabItemService] DELETE {}", targetId);
    }

    private static void writeStartupScript() throws IOException {
        StringBuilder sb = new StringBuilder();
        writeTierBlocks(sb);
        writeRegistrySection(sb);
        writeModificationSection(sb);
        writeBehaviorSection(sb);
        writeCreativeHideSection(sb);
        LabScriptWriter.write("startup_scripts", "items.js", sb.toString());
    }

    private static void writeTierBlocks(StringBuilder sb) {
        Map<String, LabCustomTier> tiers = new TreeMap<>();
        for (LabItemSaveEntry entry : STATE.values()) {
            if (entry.customTier() != null) {
                tiers.putIfAbsent(entry.customTier().id(), entry.customTier());
            }
        }
        if (tiers.isEmpty()) {
            return;
        }
        for (LabCustomTier tier : tiers.values()) {
            if (tier.armor()) {
                sb.append("ItemEvents.armorTierRegistry(event => {\n");
                sb.append("    event.add('").append(js(tier.id())).append("', tier => {\n");
                if (tier.durabilityMultiplier() > 0) {
                    sb.append("        tier.setDurabilityMultiplier(").append(fmt(tier.durabilityMultiplier()))
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
                    sb.append("        tier.setEquipSound('").append(js(tier.equipSound())).append("');\n");
                }
                if (!tier.repairIngredient().isBlank()) {
                    sb.append("        tier.setRepairIngredient('").append(js(tier.repairIngredient())).append("');\n");
                }
                if (tier.toughness() > 0) {
                    sb.append("        tier.setToughness(").append(fmt(tier.toughness())).append(");\n");
                }
                if (tier.knockbackResistance() > 0) {
                    sb.append("        tier.setKnockbackResistance(").append(fmt(tier.knockbackResistance()))
                            .append(");\n");
                }
                sb.append("    });\n");
                sb.append("});\n\n");
            } else {
                sb.append("ItemEvents.toolTierRegistry(event => {\n");
                sb.append("    event.add('").append(js(tier.id())).append("', tier => {\n");
                if (tier.uses() > 0) {
                    sb.append("        tier.setUses(").append(tier.uses()).append(");\n");
                }
                if (tier.speed() > 0) {
                    sb.append("        tier.setSpeed(").append(fmt(tier.speed())).append(");\n");
                }
                if (tier.attackDamageBonus() != 0) {
                    sb.append("        tier.setAttackDamageBonus(").append(fmt(tier.attackDamageBonus())).append(");\n");
                }
                if (tier.level() > 0) {
                    sb.append("        tier.setLevel(").append(tier.level()).append(");\n");
                }
                if (tier.enchantValue() > 0) {
                    sb.append("        tier.setEnchantmentValue(").append(tier.enchantValue()).append(");\n");
                }
                if (!tier.repairIngredient().isBlank()) {
                    sb.append("        tier.setRepairIngredient('").append(js(tier.repairIngredient())).append("');\n");
                }
                sb.append("    });\n");
                sb.append("});\n\n");
            }
        }
    }

    private static void writeRegistrySection(StringBuilder sb) {
        boolean any = false;
        for (ResourceLocation id : STATE.keySet()) {
            if (LabPathResolver.isLabOwned(id)) {
                any = true;
                break;
            }
        }
        if (!any) {
            return;
        }
        sb.append("StartupEvents.registry('item', event => {\n");
        for (Map.Entry<ResourceLocation, LabItemSaveEntry> item : STATE.entrySet()) {
            ResourceLocation id = item.getKey();
            if (!LabPathResolver.isLabOwned(id)) {
                continue;
            }
            LabItemSaveEntry entry = item.getValue();
            LabItemFieldValues v = entry.values();
            sb.append("    event.create('").append(js(id.getPath())).append("', '").append(js(entry.type()))
                    .append("')\n");
            if (!v.texture().isBlank()) {
                sb.append("        .texture('kubejs:item/").append(id.getPath()).append("')\n");
            }
            if (!v.displayName().isBlank()) {
                sb.append("        .displayName('").append(js(v.displayName())).append("')\n");
            }
            if (TOOL_TYPES.contains(entry.type())) {
                if (!v.toolTier().isBlank()) {
                    sb.append("        .tier('").append(js(v.toolTier())).append("')\n");
                }
                if (v.attackDamageBaseline() != 0) {
                    sb.append("        .attackDamageBaseline(").append(fmt(v.attackDamageBaseline())).append(")\n");
                }
                if (v.speedBaseline() != 0) {
                    sb.append("        .speedBaseline(").append(fmt(v.speedBaseline())).append(")\n");
                }
            } else if (ARMOR_TYPES.contains(entry.type()) && !v.armorTier().isBlank()) {
                sb.append("        .tier('").append(js(v.armorTier())).append("')\n");
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
                sb.append("        .containerItem('").append(js(v.containerItem())).append("')\n");
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
                sb.append("        .tooltip('").append(js(v.tooltip())).append("')\n");
            }
            for (String tag : entry.tags()) {
                sb.append("        .tag('").append(js(tag)).append("')\n");
            }
            if (!v.attributeId().isBlank()) {
                sb.append("        .modifyAttribute('").append(js(v.attributeId())).append("', '")
                        .append(js(v.attributeName())).append("', ").append(fmt(v.attributeAmount())).append(", '")
                        .append(operationName(v.attributeOperation())).append("')\n");
            }
            sb.append("        ;\n");
        }
        sb.append("});\n\n");
    }

    private static void writeModificationSection(StringBuilder sb) {
        boolean any = false;
        for (Map.Entry<ResourceLocation, LabItemSaveEntry> item : STATE.entrySet()) {
            ResourceLocation id = item.getKey();
            LabItemSaveEntry entry = item.getValue();
            if (!LabPathResolver.isLabOwned(id)
                    && (entry.status() == LabItemStatus.MODIFIED || entry.status() == LabItemStatus.DISABLED)) {
                any = true;
                break;
            }
        }
        if (!any) {
            return;
        }
        sb.append("ItemEvents.modification(event => {\n");
        for (Map.Entry<ResourceLocation, LabItemSaveEntry> item : STATE.entrySet()) {
            ResourceLocation id = item.getKey();
            LabItemSaveEntry entry = item.getValue();
            if (LabPathResolver.isLabOwned(id)
                    || (entry.status() != LabItemStatus.MODIFIED && entry.status() != LabItemStatus.DISABLED)) {
                continue;
            }
            LabItemFieldValues v = entry.values();
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
                sb.append("        item.setCraftingRemainder('").append(js(v.containerItem())).append("');\n");
            }
            if (hasFood(v)) {
                sb.append("        item.setFoodProperties(");
                appendFoodInner(sb, "", v);
                sb.append(");\n");
            }
            if (v.attackDamageBaseline() != 0) {
                sb.append("        item.setAttackDamage(").append(fmt(v.attackDamageBaseline())).append(");\n");
            }
            if (v.speedBaseline() != 0) {
                sb.append("        item.setAttackSpeed(").append(fmt(v.speedBaseline())).append(");\n");
            }
            if (v.digSpeed() > 0) {
                sb.append("        item.setDigSpeed(").append(fmt(v.digSpeed())).append(");\n");
            }
            if (v.armorProtection() > 0) {
                sb.append("        item.setArmorProtection(").append(v.armorProtection()).append(");\n");
            }
            if (v.armorToughness() > 0) {
                sb.append("        item.setArmorToughness(").append(fmt(v.armorToughness())).append(");\n");
            }
            if (v.armorKnockback() > 0) {
                sb.append("        item.setArmorKnockbackResistance(").append(fmt(v.armorKnockback())).append(");\n");
            }
            if (v.tierUses() > 0 || v.tierSpeed() > 0 || v.tierAttackDamageBonus() != 0 || v.tierLevel() > 0
                    || v.tierEnchantValue() > 0 || !v.tierRepairIngredient().isBlank()) {
                sb.append("        item.setTier(tier => {\n");
                if (v.tierUses() > 0) {
                    sb.append("            tier.setUses(").append(v.tierUses()).append(");\n");
                }
                if (v.tierSpeed() > 0) {
                    sb.append("            tier.setSpeed(").append(fmt(v.tierSpeed())).append(");\n");
                }
                if (v.tierAttackDamageBonus() != 0) {
                    sb.append("            tier.setAttackDamageBonus(").append(fmt(v.tierAttackDamageBonus()))
                            .append(");\n");
                }
                if (v.tierLevel() > 0) {
                    sb.append("            tier.setLevel(").append(v.tierLevel()).append(");\n");
                }
                if (v.tierEnchantValue() > 0) {
                    sb.append("            tier.setEnchantmentValue(").append(v.tierEnchantValue()).append(");\n");
                }
                if (!v.tierRepairIngredient().isBlank()) {
                    sb.append("            tier.setRepairIngredient('").append(js(v.tierRepairIngredient()))
                            .append("');\n");
                }
                sb.append("        });\n");
            }
            sb.append("    });\n");
        }
        sb.append("});\n\n");
    }

    private static void writeBehaviorSection(StringBuilder sb) {
        boolean any = false;
        for (LabItemSaveEntry entry : STATE.values()) {
            if (entry.actions().contains(LabItemAction.CANCEL_USE)
                    || entry.actions().contains(LabItemAction.GIVE_ITEM)
                    || entry.actions().contains(LabItemAction.DAMAGE_ITEM)) {
                any = true;
                break;
            }
        }
        if (!any) {
            return;
        }
        for (Map.Entry<ResourceLocation, LabItemSaveEntry> item : STATE.entrySet()) {
            ResourceLocation id = item.getKey();
            LabItemSaveEntry entry = item.getValue();
            boolean cancel = entry.actions().contains(LabItemAction.CANCEL_USE);
            boolean give = entry.actions().contains(LabItemAction.GIVE_ITEM)
                    && !entry.values().behaviorItem().isBlank();
            boolean damage = entry.actions().contains(LabItemAction.DAMAGE_ITEM)
                    && entry.values().behaviorDamage() > 0;
            if (!cancel && !give && !damage) {
                continue;
            }
            sb.append("ItemEvents.rightClicked('").append(id).append("', event => {\n");
            if (cancel) {
                sb.append("    event.cancel();\n");
            }
            if (give) {
                sb.append("    event.player.give('").append(js(entry.values().behaviorItem())).append("');\n");
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

    private static void writeCreativeHideSection(StringBuilder sb) {
        boolean any = false;
        for (LabItemSaveEntry entry : STATE.values()) {
            if (entry.actions().contains(LabItemAction.HIDE_CREATIVE_TAB)) {
                any = true;
                break;
            }
        }
        if (!any) {
            return;
        }
        for (String tab : CREATIVE_TABS) {
            sb.append("StartupEvents.modifyCreativeTab('").append(tab).append("', event => {\n");
            for (Map.Entry<ResourceLocation, LabItemSaveEntry> item : STATE.entrySet()) {
                if (item.getValue().actions().contains(LabItemAction.HIDE_CREATIVE_TAB)) {
                    sb.append("    event.remove('").append(item.getKey()).append("');\n");
                }
            }
            sb.append("});\n");
        }
        sb.append("\n");
    }

    private static void appendFoodInner(StringBuilder sb, String indent, LabItemFieldValues v) {
        sb.append(indent).append("food => {\n");
        if (v.foodHunger() > 0) {
            sb.append(indent).append("    food.hunger(").append(v.foodHunger()).append(");\n");
        }
        if (v.foodSaturation() > 0) {
            sb.append(indent).append("    food.saturation(").append(fmt(v.foodSaturation())).append(");\n");
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
            sb.append(indent).append("    food.effect('").append(js(v.foodEffect())).append("', ")
                    .append(v.foodEffectDuration()).append(", ").append(v.foodEffectAmplifier()).append(", ")
                    .append(fmt(v.foodEffectChance())).append(");\n");
        }
        sb.append(indent).append("}");
    }

    private static boolean hasFood(LabItemFieldValues v) {
        return v.foodHunger() > 0 || v.foodSaturation() > 0 || v.foodMeat() || v.foodFastToEat()
                || v.foodAlwaysEdible() || !v.foodEffect().isBlank();
    }

    private static void writeModelOverrides() throws IOException {
        Path modelsDir = LabPathResolver.kubejsDir().resolve("assets").resolve("minecraft").resolve("models")
                .resolve("item");
        boolean any = false;
        for (Map.Entry<ResourceLocation, LabItemSaveEntry> item : STATE.entrySet()) {
            LabItemSaveEntry entry = item.getValue();
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
            KubeJSLab.LOGGER.info("[LabItemService] wrote model overrides");
        }
    }

    private static void writeServerScript() throws IOException {
        StringBuilder sb = new StringBuilder("ServerEvents.recipes(event => {\n");
        STATE.entrySet().stream().filter(e -> e.getValue().actions().contains(LabItemAction.REMOVE_RECIPES))
                .map(e -> e.getKey().toString()).sorted()
                .forEach(id -> sb.append("    event.remove({ output: '").append(id).append("' });\n"));
        sb.append("});\n");
        LabScriptWriter.write("server_scripts", "disabled_items.js", sb.toString());
    }

    private static void writeClientScript() throws IOException {
        StringBuilder sb = new StringBuilder();
        boolean any = false;
        for (Map.Entry<ResourceLocation, LabItemSaveEntry> item : STATE.entrySet()) {
            if (item.getValue().actions().contains(LabItemAction.HIDE_VIEWER)) {
                sb.append("JEIEvents.hideItems(event => {\n    event.hide('").append(item.getKey())
                        .append("');\n});\n");
                sb.append("REIEvents.hide(event => {\n    event.hide('").append(item.getKey()).append("');\n});\n");
                any = true;
            }
        }
        if (any) {
            LabScriptWriter.write("client_scripts", "items.js", sb.toString());
        }
    }

    private static boolean copyTextures() throws IOException {
        boolean copied = false;
        Path root = LabPathResolver.kubejsDir().resolve("assets").resolve("kubejs").resolve("textures");
        for (Map.Entry<ResourceLocation, LabItemSaveEntry> item : STATE.entrySet()) {
            LabItemSaveEntry entry = item.getValue();
            String rel = entry.values().texture();
            if (rel.isBlank()) {
                continue;
            }
            Path source = root.resolve(rel).normalize();
            Path dest = textureFile(item.getKey());
            if (source.equals(dest) || !source.startsWith(root.normalize()) || !Files.isRegularFile(source)
                    || !extension(source.getFileName().toString()).equals("png")) {
                continue;
            }
            Files.createDirectories(dest.getParent());
            Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
            copied = true;
        }
        return copied;
    }

    private static String extension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase();
    }

    private static Path textureFile(ResourceLocation id) {
        return LabPathResolver.kubejsDir().resolve("assets").resolve("kubejs").resolve("textures").resolve("item")
                .resolve(id.getPath() + ".png");
    }

    private static LabCustomTier tierFor(LabItemPayload payload, ResourceLocation id) {
        LabItemFieldValues v = payload.values();
        boolean armor = ARMOR_TYPES.contains(payload.type());
        if (armor) {
            boolean any = v.tierDurabilityMultiplier() > 0 || v.tierEnchantValue() > 0
                    || !v.tierRepairIngredient().isBlank() || !v.tierEquipSound().isBlank()
                    || v.tierToughness() > 0 || v.tierKnockbackResistance() > 0 || !v.tierProtections().isBlank();
            if (!any) {
                return null;
            }
            return new LabCustomTier("lab/" + id.getPath().substring("lab/".length()) + "_tier", true, 0, 0, 0, 0,
                    v.tierEnchantValue(), v.tierRepairIngredient(), v.tierDurabilityMultiplier(),
                    parseProtections(v.tierProtections()), v.tierEquipSound(), v.tierToughness(),
                    v.tierKnockbackResistance());
        }
        boolean any = v.tierUses() > 0 || v.tierSpeed() > 0 || v.tierAttackDamageBonus() != 0 || v.tierLevel() > 0
                || v.tierEnchantValue() > 0 || !v.tierRepairIngredient().isBlank();
        if (!any) {
            return null;
        }
        return new LabCustomTier("lab/" + id.getPath().substring("lab/".length()) + "_tier", false, v.tierUses(),
                v.tierSpeed(), v.tierAttackDamageBonus(), v.tierLevel(), v.tierEnchantValue(),
                v.tierRepairIngredient(), 0, new int[4], "", 0, 0);
    }

    private static int[] parseProtections(String value) {
        int[] result = new int[4];
        if (value.isBlank()) {
            return result;
        }
        String[] parts = value.split(",");
        for (int i = 0; i < Math.min(4, parts.length); i++) {
            try {
                result[i] = Integer.parseInt(parts[i].trim());
            } catch (NumberFormatException e) {
                result[i] = 0;
            }
        }
        return result;
    }

    private static String operationName(String value) {
        return switch (value) {
            case "MULTIPLY_BASE" -> "MULTIPLY_BASE";
            case "MULTIPLY_TOTAL" -> "MULTIPLY_TOTAL";
            default -> "ADDITION";
        };
    }

    private static String js(String s) {
        return s.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n");
    }

    private static String fmt(float f) {
        return f == (int) f ? Integer.toString((int) f) : Float.toString(f);
    }

    private static void loadStateIfNeeded() {
        if (stateLoaded) {
            return;
        }
        stateLoaded = true;
        JsonObject root = LabStateFile.load(LabPathResolver.itemStateFile());
        if (root == null) {
            root = LabStateFile.load(LabPathResolver.legacyStateFile());
        }
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
                LabItemStatus status = LabItemStatus.valueOf(obj.get("status").getAsString());
                String type = obj.has("type") ? obj.get("type").getAsString() : "basic";
                String name = obj.has("name") ? obj.get("name").getAsString() : "";
                boolean wasModified = obj.has("wasModified") && obj.get("wasModified").getAsBoolean();
                LabCustomTier tier = obj.has("tier") ? readTier(obj.getAsJsonObject("tier")) : null;
                LabItemFieldValues values = obj.has("values") ? readValues(obj.getAsJsonObject("values"))
                        : LabItemFieldValues.defaults();
                List<String> tags = new ArrayList<>();
                if (obj.has("tags")) {
                    for (JsonElement el : obj.getAsJsonArray("tags")) {
                        tags.add(el.getAsString());
                    }
                }
                List<LabItemAction> actions = new ArrayList<>();
                if (obj.has("actions")) {
                    for (JsonElement el : obj.getAsJsonArray("actions")) {
                        actions.add(LabItemAction.valueOf(el.getAsString()));
                    }
                }
                STATE.put(id, new LabItemSaveEntry(type, status, name, wasModified, tier, values, tags, actions));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static LabCustomTier readTier(JsonObject obj) {
        int[] proto = new int[4];
        if (obj.has("protections")) {
            JsonArray protections = obj.getAsJsonArray("protections");
            for (int i = 0; i < Math.min(4, protections.size()); i++) {
                proto[i] = protections.get(i).getAsInt();
            }
        }
        return new LabCustomTier(obj.get("id").getAsString(), obj.get("armor").getAsBoolean(),
                obj.get("uses").getAsInt(), obj.get("speed").getAsFloat(), obj.get("attackDamageBonus").getAsFloat(),
                obj.get("level").getAsInt(), obj.get("enchantValue").getAsInt(),
                obj.get("repairIngredient").getAsString(), obj.get("durabilityMultiplier").getAsFloat(), proto,
                obj.get("equipSound").getAsString(), obj.get("toughness").getAsFloat(),
                obj.get("knockbackResistance").getAsFloat());
    }

    private static LabItemFieldValues readValues(JsonObject obj) {
        return new LabItemFieldValues(obj.get("displayName").getAsString(), obj.get("texture").getAsString(),
                obj.get("rarity").getAsString(), obj.get("maxStack").getAsInt(), obj.get("maxDamage").getAsInt(),
                obj.get("burnTime").getAsInt(), obj.get("glow").getAsBoolean(),
                obj.get("fireResistant").getAsBoolean(), obj.get("containerItem").getAsString(),
                obj.get("tooltip").getAsString(), obj.get("tags").getAsString(), obj.get("foodHunger").getAsInt(),
                obj.get("foodSaturation").getAsFloat(), obj.get("foodMeat").getAsBoolean(),
                obj.get("foodFastToEat").getAsBoolean(), obj.get("foodAlwaysEdible").getAsBoolean(),
                obj.get("foodEffect").getAsString(), obj.get("foodEffectDuration").getAsInt(),
                obj.get("foodEffectAmplifier").getAsInt(), obj.get("foodEffectChance").getAsFloat(),
                obj.get("toolTier").getAsString(), obj.get("attackDamageBaseline").getAsFloat(),
                obj.get("speedBaseline").getAsFloat(), obj.get("digSpeed").getAsFloat(),
                obj.get("armorTier").getAsString(), obj.get("armorProtection").getAsInt(),
                obj.get("armorToughness").getAsFloat(), obj.get("armorKnockback").getAsFloat(),
                obj.get("tierUses").getAsInt(), obj.get("tierSpeed").getAsFloat(),
                obj.get("tierAttackDamageBonus").getAsFloat(), obj.get("tierLevel").getAsInt(),
                obj.get("tierEnchantValue").getAsInt(), obj.get("tierRepairIngredient").getAsString(),
                obj.get("tierDurabilityMultiplier").getAsFloat(), obj.get("tierProtections").getAsString(),
                obj.get("tierEquipSound").getAsString(), obj.get("tierToughness").getAsFloat(),
                obj.get("tierKnockbackResistance").getAsFloat(), obj.get("attributeId").getAsString(),
                obj.get("attributeName").getAsString(), obj.get("attributeAmount").getAsFloat(),
                obj.get("attributeOperation").getAsString(), obj.get("behaviorItem").getAsString(),
                obj.get("behaviorDamage").getAsInt());
    }

    private static void saveState() throws IOException {
        JsonObject root = new JsonObject();
        for (Map.Entry<ResourceLocation, LabItemSaveEntry> item : STATE.entrySet()) {
            LabItemSaveEntry entry = item.getValue();
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
            for (LabItemAction action : entry.actions()) {
                actions.add(action.name());
            }
            obj.add("actions", actions);
            if (entry.customTier() != null) {
                obj.add("tier", writeTier(entry.customTier()));
            }
            root.add(item.getKey().toString(), obj);
        }
        LabStateFile.save(LabPathResolver.itemStateFile(), root);
    }

    private static void writeValues(JsonObject obj, LabItemFieldValues v) {
        obj.addProperty("displayName", v.displayName());
        obj.addProperty("texture", v.texture());
        obj.addProperty("rarity", v.rarity());
        obj.addProperty("maxStack", v.maxStack());
        obj.addProperty("maxDamage", v.maxDamage());
        obj.addProperty("burnTime", v.burnTime());
        obj.addProperty("glow", v.glow());
        obj.addProperty("fireResistant", v.fireResistant());
        obj.addProperty("containerItem", v.containerItem());
        obj.addProperty("tooltip", v.tooltip());
        obj.addProperty("tags", v.tags());
        obj.addProperty("foodHunger", v.foodHunger());
        obj.addProperty("foodSaturation", v.foodSaturation());
        obj.addProperty("foodMeat", v.foodMeat());
        obj.addProperty("foodFastToEat", v.foodFastToEat());
        obj.addProperty("foodAlwaysEdible", v.foodAlwaysEdible());
        obj.addProperty("foodEffect", v.foodEffect());
        obj.addProperty("foodEffectDuration", v.foodEffectDuration());
        obj.addProperty("foodEffectAmplifier", v.foodEffectAmplifier());
        obj.addProperty("foodEffectChance", v.foodEffectChance());
        obj.addProperty("toolTier", v.toolTier());
        obj.addProperty("attackDamageBaseline", v.attackDamageBaseline());
        obj.addProperty("speedBaseline", v.speedBaseline());
        obj.addProperty("digSpeed", v.digSpeed());
        obj.addProperty("armorTier", v.armorTier());
        obj.addProperty("armorProtection", v.armorProtection());
        obj.addProperty("armorToughness", v.armorToughness());
        obj.addProperty("armorKnockback", v.armorKnockback());
        obj.addProperty("tierUses", v.tierUses());
        obj.addProperty("tierSpeed", v.tierSpeed());
        obj.addProperty("tierAttackDamageBonus", v.tierAttackDamageBonus());
        obj.addProperty("tierLevel", v.tierLevel());
        obj.addProperty("tierEnchantValue", v.tierEnchantValue());
        obj.addProperty("tierRepairIngredient", v.tierRepairIngredient());
        obj.addProperty("tierDurabilityMultiplier", v.tierDurabilityMultiplier());
        obj.addProperty("tierProtections", v.tierProtections());
        obj.addProperty("tierEquipSound", v.tierEquipSound());
        obj.addProperty("tierToughness", v.tierToughness());
        obj.addProperty("tierKnockbackResistance", v.tierKnockbackResistance());
        obj.addProperty("attributeId", v.attributeId());
        obj.addProperty("attributeName", v.attributeName());
        obj.addProperty("attributeAmount", v.attributeAmount());
        obj.addProperty("attributeOperation", v.attributeOperation());
        obj.addProperty("behaviorItem", v.behaviorItem());
        obj.addProperty("behaviorDamage", v.behaviorDamage());
    }

    private static JsonObject writeTier(LabCustomTier tier) {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", tier.id());
        obj.addProperty("armor", tier.armor());
        obj.addProperty("uses", tier.uses());
        obj.addProperty("speed", tier.speed());
        obj.addProperty("attackDamageBonus", tier.attackDamageBonus());
        obj.addProperty("level", tier.level());
        obj.addProperty("enchantValue", tier.enchantValue());
        obj.addProperty("repairIngredient", tier.repairIngredient());
        obj.addProperty("durabilityMultiplier", tier.durabilityMultiplier());
        JsonArray proto = new JsonArray();
        for (int p : tier.protections()) {
            proto.add(p);
        }
        obj.add("protections", proto);
        obj.addProperty("equipSound", tier.equipSound());
        obj.addProperty("toughness", tier.toughness());
        obj.addProperty("knockbackResistance", tier.knockbackResistance());
        return obj;
    }

    private record LabItemSaveEntry(String type, LabItemStatus status, String name, boolean wasModified,
            LabCustomTier customTier, LabItemFieldValues values, List<String> tags, List<LabItemAction> actions) {
    }
}