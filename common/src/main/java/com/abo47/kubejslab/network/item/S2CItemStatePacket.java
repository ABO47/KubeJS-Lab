package com.abo47.kubejslab.network.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.KubeJSLab;
import com.abo47.kubejslab.client.ui.LabScreen;
import com.abo47.kubejslab.client.ui.items.LabItemStates;
import com.abo47.kubejslab.item.model.LabCustomTier;
import com.abo47.kubejslab.item.model.LabItemAction;
import com.abo47.kubejslab.item.model.LabItemFieldValues;
import com.abo47.kubejslab.item.model.LabItemState;
import com.abo47.kubejslab.item.model.LabItemStatus;


public record S2CItemStatePacket(Map<ResourceLocation, LabItemState> states, List<ResourceLocation> pendingOnly) {

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(states.size());
        for (LabItemState entry : states.values()) {
            buf.writeUtf(entry.id().toString());
            buf.writeUtf(entry.type(), 32767);
            buf.writeVarInt(entry.status().ordinal());
            buf.writeBoolean(entry.pendingRestart());
            buf.writeUtf(entry.name());
            buf.writeBoolean(entry.wasModified());
            LabCustomTier tier = entry.customTier();
            buf.writeBoolean(tier != null);
            if (tier != null) {
                buf.writeUtf(tier.id());
                buf.writeBoolean(tier.armor());
                buf.writeVarInt(tier.uses());
                buf.writeFloat(tier.speed());
                buf.writeFloat(tier.attackDamageBonus());
                buf.writeVarInt(tier.level());
                buf.writeVarInt(tier.enchantValue());
                buf.writeUtf(tier.repairIngredient());
                buf.writeFloat(tier.durabilityMultiplier());
                buf.writeVarInt(tier.protections().length);
                for (int protection : tier.protections()) {
                    buf.writeVarInt(protection);
                }
                buf.writeUtf(tier.equipSound());
                buf.writeFloat(tier.toughness());
                buf.writeFloat(tier.knockbackResistance());
            }
            LabItemFieldValues.write(buf, entry.values());
            buf.writeVarInt(entry.tags().size());
            for (String tag : entry.tags()) {
                buf.writeUtf(tag, 32767);
            }
            buf.writeVarInt(entry.actions().size());
            for (LabItemAction action : entry.actions()) {
                buf.writeVarInt(action.ordinal());
            }
        }
        buf.writeVarInt(pendingOnly.size());
        for (ResourceLocation id : pendingOnly) {
            buf.writeUtf(id.toString());
        }
        KubeJSLab.LOGGER.info("[Net] S2CItemStatePacket write: {} bytes, {} entries", buf.readableBytes(), states.size());
        if (buf.readableBytes() > 2048) {
            KubeJSLab.LOGGER.warn("S2CItemStatePacket is large ({} bytes, {} entries); may exceed the channel's per-string limit",
                    buf.readableBytes(), states.size());
        }
    }

    public static S2CItemStatePacket read(FriendlyByteBuf buf) {
        int size = Math.min(buf.readVarInt(), 65536);
        Map<ResourceLocation, LabItemState> states = new HashMap<>();
        for (int i = 0; i < size; i++) {
            ResourceLocation id = new ResourceLocation(buf.readUtf());
            String type = buf.readUtf();
            int statusOrdinal = Math.min(buf.readVarInt(), LabItemStatus.values().length - 1);
            boolean pendingRestart = buf.readBoolean();
            String name = buf.readUtf();
            boolean wasModified = buf.readBoolean();
            LabCustomTier tier = null;
            if (buf.readBoolean()) {
                String tierId = buf.readUtf();
                boolean armor = buf.readBoolean();
                int uses = Math.max(0, buf.readVarInt());
                float speed = buf.readFloat();
                float attackDamageBonus = buf.readFloat();
                int level = Math.max(0, buf.readVarInt());
                int enchantValue = Math.max(0, buf.readVarInt());
                String repairIngredient = buf.readUtf();
                float durabilityMultiplier = buf.readFloat();
                int protectionCount = Math.min(buf.readVarInt(), 4);
                int[] protections = new int[protectionCount];
                for (int p = 0; p < protectionCount; p++) {
                    protections[p] = Math.max(0, Math.min(1000, buf.readVarInt()));
                }
                String equipSound = buf.readUtf();
                float toughness = buf.readFloat();
                float knockbackResistance = buf.readFloat();
                tier = new LabCustomTier(tierId, armor, uses, speed, attackDamageBonus, level, enchantValue,
                        repairIngredient, durabilityMultiplier, protections, equipSound, toughness, knockbackResistance);
            }
            LabItemFieldValues values = LabItemFieldValues.read(buf);
            int tagCount = Math.min(buf.readVarInt(), 32);
            List<String> tagList = new ArrayList<>(tagCount);
            for (int t = 0; t < tagCount; t++) {
                tagList.add(buf.readUtf());
            }
            int actionCount = Math.min(buf.readVarInt(), 16);
            List<LabItemAction> actions = new ArrayList<>(actionCount);
            for (int a = 0; a < actionCount; a++) {
                int ordinal = buf.readVarInt();
                if (ordinal < LabItemAction.values().length) {
                    actions.add(LabItemAction.values()[ordinal]);
                }
            }
            states.put(id, new LabItemState(id, type, LabItemStatus.values()[statusOrdinal], pendingRestart, name,
                    wasModified, tier, values, tagList, actions));
        }
        int pendingCount = Math.min(buf.readVarInt(), 65536);
        List<ResourceLocation> pendingOnly = new ArrayList<>(pendingCount);
        for (int i = 0; i < pendingCount; i++) {
            pendingOnly.add(new ResourceLocation(buf.readUtf()));
        }
        return new S2CItemStatePacket(states, pendingOnly);
    }

    public void handleClient() {
        LabItemStates.apply(states, pendingOnly);
        LabScreen.refreshOpen();
    }
}