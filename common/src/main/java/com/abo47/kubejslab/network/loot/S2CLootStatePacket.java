package com.abo47.kubejslab.network.loot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import com.abo47.kubejslab.KubeJSLab;
import com.abo47.kubejslab.client.ui.loot.LootStates;
import com.abo47.kubejslab.client.ui.shell.ScreenFactory;
import com.abo47.kubejslab.loot.model.LootAction;
import com.abo47.kubejslab.loot.model.LootFieldValues;
import com.abo47.kubejslab.loot.model.LootState;
import com.abo47.kubejslab.loot.model.LootStatus;


public record S2CLootStatePacket(Map<ResourceLocation, LootState> states) {

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(states.size());
        for (LootState entry : states.values()) {
            buf.writeUtf(entry.id().toString());
            buf.writeUtf(entry.lootType(), 32767);
            buf.writeVarInt(entry.status().ordinal());
            buf.writeUtf(entry.name());
            buf.writeBoolean(entry.wasModified());
            LootFieldValues.write(buf, entry.values());
            buf.writeVarInt(entry.tags().size());
            for (String tag : entry.tags()) {
                buf.writeUtf(tag, 32767);
            }
            buf.writeVarInt(entry.actions().size());
            for (LootAction action : entry.actions()) {
                buf.writeVarInt(action.ordinal());
            }
        }
        KubeJSLab.LOGGER.info("[Net] S2CLootStatePacket write: {} entries", states.size());
    }

    public static S2CLootStatePacket read(FriendlyByteBuf buf) {
        int size = Math.min(buf.readVarInt(), 65536);
        Map<ResourceLocation, LootState> states = new HashMap<>();
        for (int i = 0; i < size; i++) {
            ResourceLocation id = new ResourceLocation(buf.readUtf());
            String lootType = buf.readUtf();
            int statusOrdinal = Math.min(buf.readVarInt(), LootStatus.values().length - 1);
            String name = buf.readUtf();
            boolean wasModified = buf.readBoolean();
            LootFieldValues values = LootFieldValues.read(buf);
            int tagCount = Math.min(buf.readVarInt(), 32);
            List<String> tagList = new ArrayList<>(tagCount);
            for (int t = 0; t < tagCount; t++) {
                tagList.add(buf.readUtf());
            }
            int actionCount = Math.min(buf.readVarInt(), 16);
            List<LootAction> actions = new ArrayList<>(actionCount);
            for (int a = 0; a < actionCount; a++) {
                int ordinal = buf.readVarInt();
                if (ordinal < LootAction.values().length) {
                    actions.add(LootAction.values()[ordinal]);
                }
            }
            states.put(id, new LootState(id, lootType, LootStatus.values()[statusOrdinal], name,
                    wasModified, values, tagList, actions));
        }
        return new S2CLootStatePacket(states);
    }

    public void handleClient() {
        LootStates.apply(states);
        ScreenFactory.refreshOpen();
    }
}
