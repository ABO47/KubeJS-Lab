package com.abo47.kubejslab.network.item;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.kubejslab.KubeJSLab;
import com.abo47.kubejslab.item.model.LabItemAction;
import com.abo47.kubejslab.item.model.LabItemEditAction;
import com.abo47.kubejslab.item.model.LabItemFieldValues;
import com.abo47.kubejslab.item.model.LabItemPayload;
import com.abo47.kubejslab.item.runtime.LabItemService;


public record C2SItemEditPacket(LabItemEditAction action, @Nullable ResourceLocation targetId,
        LabItemPayload payload) {

    public void write(FriendlyByteBuf buf) {
        int start = buf.writerIndex();
        buf.writeVarInt(action.ordinal());
        buf.writeBoolean(targetId != null);
        if (targetId != null) {
            buf.writeUtf(targetId.toString(), 32767);
        }
        int afterIds = buf.writerIndex() - start;
        LabItemFieldValues values = payload.values();
        buf.writeUtf(values.displayName(), 32767);
        buf.writeUtf(values.texture(), 32767);
        buf.writeUtf(values.rarity(), 32767);
        buf.writeVarInt(values.maxStack());
        buf.writeVarInt(values.maxDamage());
        buf.writeVarInt(values.burnTime());
        buf.writeBoolean(values.glow());
        buf.writeBoolean(values.fireResistant());
        buf.writeUtf(values.containerItem(), 32767);
        buf.writeUtf(values.tooltip(), 32767);
        buf.writeUtf(values.tags(), 32767);
        buf.writeVarInt(values.foodHunger());
        buf.writeFloat(values.foodSaturation());
        buf.writeBoolean(values.foodMeat());
        buf.writeBoolean(values.foodFastToEat());
        buf.writeBoolean(values.foodAlwaysEdible());
        buf.writeUtf(values.foodEffect(), 32767);
        buf.writeVarInt(values.foodEffectDuration());
        buf.writeVarInt(values.foodEffectAmplifier());
        buf.writeFloat(values.foodEffectChance());
        buf.writeUtf(values.toolTier(), 32767);
        buf.writeFloat(values.attackDamageBaseline());
        buf.writeFloat(values.speedBaseline());
        buf.writeFloat(values.digSpeed());
        buf.writeUtf(values.armorTier(), 32767);
        buf.writeVarInt(values.armorProtection());
        buf.writeFloat(values.armorToughness());
        buf.writeFloat(values.armorKnockback());
        buf.writeVarInt(values.tierUses());
        buf.writeFloat(values.tierSpeed());
        buf.writeFloat(values.tierAttackDamageBonus());
        buf.writeVarInt(values.tierLevel());
        buf.writeVarInt(values.tierEnchantValue());
        buf.writeUtf(values.tierRepairIngredient(), 32767);
        buf.writeFloat(values.tierDurabilityMultiplier());
        buf.writeUtf(values.tierProtections(), 32767);
        buf.writeUtf(values.tierEquipSound(), 32767);
        buf.writeFloat(values.tierToughness());
        buf.writeFloat(values.tierKnockbackResistance());
        buf.writeUtf(values.attributeId(), 32767);
        buf.writeUtf(values.attributeName(), 32767);
        buf.writeFloat(values.attributeAmount());
        buf.writeUtf(values.attributeOperation(), 32767);
        buf.writeUtf(values.behaviorItem(), 32767);
        buf.writeVarInt(values.behaviorDamage());
        int afterValues = buf.writerIndex() - start;
        buf.writeVarInt(payload.tags().size());
        for (String tag : payload.tags()) {
            buf.writeUtf(tag, 32767);
        }
        buf.writeVarInt(payload.actions().size());
        for (LabItemAction itemAction : payload.actions()) {
            buf.writeVarInt(itemAction.ordinal());
        }
        int total = buf.writerIndex() - start;
        KubeJSLab.LOGGER.info(
                "[Net] C2SItemEditPacket encoded: ids={}b, values={}b, tags+actions={}b, total={}b (type={}, {} tags, {} actions)",
                afterIds, afterValues - afterIds, total - afterValues, total, payload.type(), payload.tags().size(),
                payload.actions().size());
    }

    public static C2SItemEditPacket read(FriendlyByteBuf buf) {
        LabItemEditAction action = LabItemEditAction.values()[buf.readVarInt()];
        ResourceLocation targetId = buf.readBoolean() ? new ResourceLocation(buf.readUtf()) : null;
        String type = buf.readUtf();
        String displayName = buf.readUtf();
        String texture = buf.readUtf();
        String rarity = buf.readUtf();
        int maxStack = Math.max(1, Math.min(64, buf.readVarInt()));
        int maxDamage = Math.max(0, Math.min(1000000, buf.readVarInt()));
        int burnTime = Math.max(0, Math.min(100000, buf.readVarInt()));
        boolean glow = buf.readBoolean();
        boolean fireResistant = buf.readBoolean();
        String containerItem = buf.readUtf();
        String tooltip = buf.readUtf();
        String tags = buf.readUtf();
        int foodHunger = Math.max(0, Math.min(100000, buf.readVarInt()));
        float foodSaturation = buf.readFloat();
        boolean foodMeat = buf.readBoolean();
        boolean foodFastToEat = buf.readBoolean();
        boolean foodAlwaysEdible = buf.readBoolean();
        String foodEffect = buf.readUtf();
        int foodEffectDuration = Math.max(0, Math.min(1000000, buf.readVarInt()));
        int foodEffectAmplifier = Math.max(0, Math.min(1000, buf.readVarInt()));
        float foodEffectChance = buf.readFloat();
        String toolTier = buf.readUtf();
        float attackDamageBaseline = buf.readFloat();
        float speedBaseline = buf.readFloat();
        float digSpeed = buf.readFloat();
        String armorTier = buf.readUtf();
        int armorProtection = Math.max(0, Math.min(1000, buf.readVarInt()));
        float armorToughness = buf.readFloat();
        float armorKnockback = buf.readFloat();
        int tierUses = Math.max(0, Math.min(1000000, buf.readVarInt()));
        float tierSpeed = buf.readFloat();
        float tierAttackDamageBonus = buf.readFloat();
        int tierLevel = Math.max(0, Math.min(1000, buf.readVarInt()));
        int tierEnchantValue = Math.max(0, Math.min(1000, buf.readVarInt()));
        String tierRepairIngredient = buf.readUtf();
        float tierDurabilityMultiplier = buf.readFloat();
        String tierProtections = buf.readUtf();
        String tierEquipSound = buf.readUtf();
        float tierToughness = buf.readFloat();
        float tierKnockbackResistance = buf.readFloat();
        String attributeId = buf.readUtf();
        String attributeName = buf.readUtf();
        float attributeAmount = buf.readFloat();
        String attributeOperation = buf.readUtf();
        String behaviorItem = buf.readUtf();
        int behaviorDamage = Math.max(0, Math.min(1000000, buf.readVarInt()));
        int tagCount = Math.min(buf.readVarInt(), 32);
        List<String> tagList = new ArrayList<>(tagCount);
        for (int i = 0; i < tagCount; i++) {
            tagList.add(buf.readUtf());
        }
        int actionCount = Math.min(buf.readVarInt(), 16);
        List<LabItemAction> actions = new ArrayList<>(actionCount);
        for (int i = 0; i < actionCount; i++) {
            int ordinal = buf.readVarInt();
            if (ordinal < LabItemAction.values().length) {
                actions.add(LabItemAction.values()[ordinal]);
            }
        }
        LabItemFieldValues values = new LabItemFieldValues(displayName, texture, rarity, maxStack, maxDamage, burnTime,
                glow, fireResistant, containerItem, tooltip, tags, foodHunger, foodSaturation, foodMeat,
                foodFastToEat, foodAlwaysEdible, foodEffect, foodEffectDuration, foodEffectAmplifier, foodEffectChance,
                toolTier, attackDamageBaseline, speedBaseline, digSpeed, armorTier, armorProtection, armorToughness,
                armorKnockback, tierUses, tierSpeed, tierAttackDamageBonus, tierLevel, tierEnchantValue,
                tierRepairIngredient, tierDurabilityMultiplier, tierProtections, tierEquipSound, tierToughness,
                tierKnockbackResistance, attributeId, attributeName, attributeAmount, attributeOperation, behaviorItem,
                behaviorDamage);
        return new C2SItemEditPacket(action, targetId, new LabItemPayload(targetId, type, values, tagList, actions));
    }

    public void handle(ServerPlayer player) {
        KubeJSLab.LOGGER.info("[Net] C2SItemEditPacket received from {}: action={}, targetId={}, type={}, tags={}, actions={}",
                player.getName().getString(), action, targetId, payload.type(), payload.tags().size(),
                payload.actions().size());
        if (!player.hasPermissions(2)) {
            KubeJSLab.LOGGER.warn("[Net] C2SItemEditPacket rejected: {} lacks permission level 2", player.getName().getString());
            return;
        }
        LabItemService.handle(player, action, targetId, payload);
    }
}