package com.abo47.kubejslab.item.runtime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.item.model.CustomTier;
import com.abo47.kubejslab.item.model.ItemAction;
import com.abo47.kubejslab.item.model.ItemFieldValues;
import com.abo47.kubejslab.item.model.ItemStatus;
import com.abo47.kubejslab.workspace.JsonStateFile;
import com.abo47.kubejslab.workspace.WorkspacePaths;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class ItemStateIo {

    private ItemStateIo() {
    }

    static Map<ResourceLocation, ItemSaveEntry> load() {
        Map<ResourceLocation, ItemSaveEntry> loaded = new LinkedHashMap<>();

        JsonObject root = JsonStateFile.load(WorkspacePaths.itemStateFile());
        if (root == null) {
            root = JsonStateFile.load(WorkspacePaths.legacyStateFile());
        }
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
                ItemStatus status = ItemStatus.valueOf(obj.get("status").getAsString());
                String type = obj.has("type") ? obj.get("type").getAsString() : "basic";
                String name = obj.has("name") ? obj.get("name").getAsString() : "";
                boolean wasModified = obj.has("wasModified") && obj.get("wasModified").getAsBoolean();
                CustomTier tier = obj.has("tier") ? readTier(obj.getAsJsonObject("tier")) : null;
                ItemFieldValues values = obj.has("values") ? readValues(obj.getAsJsonObject("values"))
                        : ItemFieldValues.defaults();
                List<String> tags = new ArrayList<>();
                if (obj.has("tags")) {
                    for (JsonElement el : obj.getAsJsonArray("tags")) {
                        tags.add(el.getAsString());
                    }
                }
                List<ItemAction> actions = new ArrayList<>();
                if (obj.has("actions")) {
                    for (JsonElement el : obj.getAsJsonArray("actions")) {
                        actions.add(ItemAction.valueOf(el.getAsString()));
                    }
                }
                loaded.put(id, new ItemSaveEntry(type, status, name, wasModified, tier, values, tags, actions));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
            return loaded;
    }

    static void save(Map<ResourceLocation, ItemSaveEntry> states) throws IOException {
        JsonObject root = new JsonObject();
        for (Map.Entry<ResourceLocation, ItemSaveEntry> item : states.entrySet()) {
            ItemSaveEntry entry = item.getValue();
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
            for (ItemAction action : entry.actions()) {
                actions.add(action.name());
            }
            obj.add("actions", actions);
            if (entry.customTier() != null) {
                obj.add("tier", writeTier(entry.customTier()));
            }
            root.add(item.getKey().toString(), obj);
        }
        JsonStateFile.save(WorkspacePaths.itemStateFile(), root);
    }

    static CustomTier readTier(JsonObject obj) {
        int[] proto = new int[4];
        if (obj.has("protections")) {
            JsonArray protections = obj.getAsJsonArray("protections");
            for (int i = 0; i < Math.min(4, protections.size()); i++) {
                proto[i] = protections.get(i).getAsInt();
            }
        }
        return new CustomTier(obj.get("id").getAsString(), obj.get("armor").getAsBoolean(),
                obj.get("uses").getAsInt(), obj.get("speed").getAsFloat(), obj.get("attackDamageBonus").getAsFloat(),
                obj.get("level").getAsInt(), obj.get("enchantValue").getAsInt(),
                obj.get("repairIngredient").getAsString(), obj.get("durabilityMultiplier").getAsFloat(), proto,
                obj.get("equipSound").getAsString(), obj.get("toughness").getAsFloat(),
                obj.get("knockbackResistance").getAsFloat());
    }

    static ItemFieldValues readValues(JsonObject obj) {
        return new ItemFieldValues(obj.get("displayName").getAsString(), obj.get("texture").getAsString(),
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

    static void writeValues(JsonObject obj, ItemFieldValues v) {
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

    static JsonObject writeTier(CustomTier tier) {
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
}
