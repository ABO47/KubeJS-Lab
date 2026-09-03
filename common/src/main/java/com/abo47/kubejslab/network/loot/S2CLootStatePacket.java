package com.abo47.kubejslab.network.loot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.KubeJSLab;
import com.abo47.kubejslab.client.ui.LabScreen;
import com.abo47.kubejslab.client.ui.loot.LabLootStates;
import com.abo47.kubejslab.loot.model.LabLootAction;
import com.abo47.kubejslab.loot.model.LabLootFieldValues;
import com.abo47.kubejslab.loot.model.LabLootState;
import com.abo47.kubejslab.loot.model.LabLootStatus;


public record S2CLootStatePacket(Map<ResourceLocation, LabLootState> states) {

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(states.size());
        for (LabLootState entry : states.values()) {
            buf.writeUtf(entry.id().toString());
            buf.writeUtf(entry.lootType(), 32767);
            buf.writeVarInt(entry.status().ordinal());
            buf.writeUtf(entry.name());
            buf.writeBoolean(entry.wasModified());
            LabLootFieldValues.write(buf, entry.values());
            buf.writeVarInt(entry.tags().size());
            for (String tag : entry.tags()) {
                buf.writeUtf(tag, 32767);
            }
            buf.writeVarInt(entry.actions().size());
            for (LabLootAction action : entry.actions()) {
                buf.writeVarInt(action.ordinal());
            }
        }
        KubeJSLab.LOGGER.info("[Net] S2CLootStatePacket write: {} entries", states.size());
    }

    public static S2CLootStatePacket read(FriendlyByteBuf buf) {
        int size = Math.min(buf.readVarInt(), 65536);
        Map<ResourceLocation, LabLootState> states = new HashMap<>();
        for (int i = 0; i < size; i++) {
            ResourceLocation id = new ResourceLocation(buf.readUtf());
            String lootType = buf.readUtf();
            int statusOrdinal = Math.min(buf.readVarInt(), LabLootStatus.values().length - 1);
            String name = buf.readUtf();
            boolean wasModified = buf.readBoolean();
            LabLootFieldValues values = LabLootFieldValues.read(buf);
            int tagCount = Math.min(buf.readVarInt(), 32);
            List<String> tagList = new ArrayList<>(tagCount);
            for (int t = 0; t < tagCount; t++) {
                tagList.add(buf.readUtf());
            }
            int actionCount = Math.min(buf.readVarInt(), 16);
            List<LabLootAction> actions = new ArrayList<>(actionCount);
            for (int a = 0; a < actionCount; a++) {
                int ordinal = buf.readVarInt();
                if (ordinal < LabLootAction.values().length) {
                    actions.add(LabLootAction.values()[ordinal]);
                }
            }
            states.put(id, new LabLootState(id, lootType, LabLootStatus.values()[statusOrdinal], name,
                    wasModified, values, tagList, actions));
        }
        return new S2CLootStatePacket(states);
    }

    public void handleClient() {
        LabLootStates.apply(states);
        LabScreen.refreshOpen();
    }
}
