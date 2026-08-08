package com.abo47.kubejslab.item.model;

import net.minecraft.network.FriendlyByteBuf;


public record LabItemFieldValues(String displayName, String texture, String rarity, int maxStack, int maxDamage,
        int burnTime, boolean glow, boolean fireResistant, String containerItem, String tooltip, String tags,
        int foodHunger, float foodSaturation, boolean foodMeat, boolean foodFastToEat, boolean foodAlwaysEdible,
        String foodEffect, int foodEffectDuration, int foodEffectAmplifier, float foodEffectChance, String toolTier,
        float attackDamageBaseline, float speedBaseline, float digSpeed, String armorTier, int armorProtection,
        float armorToughness, float armorKnockback, int tierUses, float tierSpeed, float tierAttackDamageBonus,
        int tierLevel, int tierEnchantValue, String tierRepairIngredient, float tierDurabilityMultiplier,
        String tierProtections, String tierEquipSound, float tierToughness, float tierKnockbackResistance,
        String attributeId, String attributeName, float attributeAmount, String attributeOperation,
        String behaviorItem, int behaviorDamage) {

    public LabItemFieldValues {
        displayName = displayName == null ? "" : displayName;
        texture = texture == null ? "" : texture;
        rarity = rarity == null ? "" : rarity;
        maxStack = Math.max(1, Math.min(64, maxStack));
        maxDamage = Math.max(0, maxDamage);
        burnTime = Math.max(0, burnTime);
        containerItem = containerItem == null ? "" : containerItem;
        tooltip = tooltip == null ? "" : tooltip;
        tags = tags == null ? "" : tags;
        foodHunger = Math.max(0, foodHunger);
        foodSaturation = Math.max(0f, foodSaturation);
        foodEffect = foodEffect == null ? "" : foodEffect;
        foodEffectDuration = Math.max(0, foodEffectDuration);
        foodEffectAmplifier = Math.max(0, foodEffectAmplifier);
        foodEffectChance = Math.max(0f, Math.min(1f, foodEffectChance));
        toolTier = toolTier == null ? "" : toolTier;
        digSpeed = Math.max(0f, digSpeed);
        armorTier = armorTier == null ? "" : armorTier;
        armorProtection = Math.max(0, armorProtection);
        armorToughness = Math.max(0f, armorToughness);
        armorKnockback = Math.max(0f, armorKnockback);
        tierUses = Math.max(0, tierUses);
        tierSpeed = Math.max(0f, tierSpeed);
        tierAttackDamageBonus = Math.max(0f, tierAttackDamageBonus);
        tierLevel = Math.max(0, tierLevel);
        tierEnchantValue = Math.max(0, tierEnchantValue);
        tierRepairIngredient = tierRepairIngredient == null ? "" : tierRepairIngredient;
        tierDurabilityMultiplier = Math.max(0f, tierDurabilityMultiplier);
        tierProtections = tierProtections == null ? "" : tierProtections;
        tierEquipSound = tierEquipSound == null ? "" : tierEquipSound;
        tierToughness = Math.max(0f, tierToughness);
        tierKnockbackResistance = Math.max(0f, tierKnockbackResistance);
        attributeId = attributeId == null ? "" : attributeId;
        attributeName = attributeName == null ? "" : attributeName;
        attributeOperation = attributeOperation == null ? "" : attributeOperation;
        behaviorItem = behaviorItem == null ? "" : behaviorItem;
        behaviorDamage = Math.max(0, behaviorDamage);
    }

    public static LabItemFieldValues defaults() {
        return new LabItemFieldValues("", "", "", 64, 0, 0, false, false, "", "", "", 0, 0f, false, false,
                false, "", 0, 0, 0f, "", 0f, 0f, 0f, "", 0, 0f, 0f, 0, 0f, 0f, 0, 0, "", 0f, "", "", 0f, 0f,
                "", "", 0f, "", "", 0);
    }

    public static void write(FriendlyByteBuf buf, LabItemFieldValues v) {
        buf.writeUtf(v.displayName(), 32767);
        buf.writeUtf(v.texture(), 32767);
        buf.writeUtf(v.rarity(), 32767);
        buf.writeVarInt(v.maxStack());
        buf.writeVarInt(v.maxDamage());
        buf.writeVarInt(v.burnTime());
        buf.writeBoolean(v.glow());
        buf.writeBoolean(v.fireResistant());
        buf.writeUtf(v.containerItem(), 32767);
        buf.writeUtf(v.tooltip(), 32767);
        buf.writeUtf(v.tags(), 32767);
        buf.writeVarInt(v.foodHunger());
        buf.writeFloat(v.foodSaturation());
        buf.writeBoolean(v.foodMeat());
        buf.writeBoolean(v.foodFastToEat());
        buf.writeBoolean(v.foodAlwaysEdible());
        buf.writeUtf(v.foodEffect(), 32767);
        buf.writeVarInt(v.foodEffectDuration());
        buf.writeVarInt(v.foodEffectAmplifier());
        buf.writeFloat(v.foodEffectChance());
        buf.writeUtf(v.toolTier(), 32767);
        buf.writeFloat(v.attackDamageBaseline());
        buf.writeFloat(v.speedBaseline());
        buf.writeFloat(v.digSpeed());
        buf.writeUtf(v.armorTier(), 32767);
        buf.writeVarInt(v.armorProtection());
        buf.writeFloat(v.armorToughness());
        buf.writeFloat(v.armorKnockback());
        buf.writeVarInt(v.tierUses());
        buf.writeFloat(v.tierSpeed());
        buf.writeFloat(v.tierAttackDamageBonus());
        buf.writeVarInt(v.tierLevel());
        buf.writeVarInt(v.tierEnchantValue());
        buf.writeUtf(v.tierRepairIngredient(), 32767);
        buf.writeFloat(v.tierDurabilityMultiplier());
        buf.writeUtf(v.tierProtections(), 32767);
        buf.writeUtf(v.tierEquipSound(), 32767);
        buf.writeFloat(v.tierToughness());
        buf.writeFloat(v.tierKnockbackResistance());
        buf.writeUtf(v.attributeId(), 32767);
        buf.writeUtf(v.attributeName(), 32767);
        buf.writeFloat(v.attributeAmount());
        buf.writeUtf(v.attributeOperation(), 32767);
        buf.writeUtf(v.behaviorItem(), 32767);
        buf.writeVarInt(Math.max(0, Math.min(1000000, v.behaviorDamage())));
    }

    public static LabItemFieldValues read(FriendlyByteBuf buf) {
        return new LabItemFieldValues(buf.readUtf(), buf.readUtf(), buf.readUtf(),
                Math.max(1, Math.min(64, buf.readVarInt())), Math.max(0, Math.min(1000000, buf.readVarInt())),
                Math.max(0, Math.min(100000, buf.readVarInt())), buf.readBoolean(), buf.readBoolean(), buf.readUtf(),
                buf.readUtf(), buf.readUtf(), Math.max(0, Math.min(100000, buf.readVarInt())), buf.readFloat(),
                buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readUtf(),
                Math.max(0, Math.min(1000000, buf.readVarInt())), Math.max(0, Math.min(1000, buf.readVarInt())),
                buf.readFloat(), buf.readUtf(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readUtf(),
                Math.max(0, Math.min(1000, buf.readVarInt())), buf.readFloat(), buf.readFloat(),
                Math.max(0, Math.min(1000000, buf.readVarInt())), buf.readFloat(), buf.readFloat(),
                Math.max(0, Math.min(1000, buf.readVarInt())), Math.max(0, Math.min(1000, buf.readVarInt())),
                buf.readUtf(), buf.readFloat(), buf.readUtf(), buf.readUtf(), buf.readFloat(), buf.readFloat(),
                buf.readUtf(), buf.readUtf(), buf.readFloat(), buf.readUtf(), buf.readUtf(),
                Math.max(0, Math.min(1000000, buf.readVarInt())));
    }
}
