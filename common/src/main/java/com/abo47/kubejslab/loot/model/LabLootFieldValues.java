package com.abo47.kubejslab.loot.model;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.FriendlyByteBuf;

public record LabLootFieldValues(String targetId, String customId, List<LabLootPoolValues> pools,
        int droppedPools, int droppedEntries) {

    public LabLootFieldValues {
        targetId = targetId == null ? "" : targetId;
        customId = customId == null ? "" : customId;
        pools = pools == null ? List.of() : List.copyOf(pools);
        droppedPools = Math.max(0, droppedPools);
        droppedEntries = Math.max(0, droppedEntries);
    }

    public static LabLootFieldValues defaults() {
        return new LabLootFieldValues("", "", List.of(LabLootPoolValues.defaults()), 0, 0);
    }

    public static void write(FriendlyByteBuf buf, LabLootFieldValues v) {
        buf.writeUtf(v.targetId(), 32767);
        buf.writeUtf(v.customId(), 32767);
        buf.writeVarInt(v.pools().size());
        for (LabLootPoolValues p : v.pools()) {
            LabLootPoolValues.write(buf, p);
        }
        buf.writeVarInt(v.droppedPools());
        buf.writeVarInt(v.droppedEntries());
    }

    public static LabLootFieldValues read(FriendlyByteBuf buf) {
        String targetId = buf.readUtf();
        String customId = buf.readUtf();
        int poolCount = Math.min(buf.readVarInt(), 64);
        List<LabLootPoolValues> pools = new ArrayList<>(poolCount);
        for (int i = 0; i < poolCount; i++) {
            pools.add(LabLootPoolValues.read(buf));
        }
        if (pools.isEmpty()) {
            pools.add(LabLootPoolValues.defaults());
        }
        int droppedPools = Math.max(0, Math.min(1024, buf.readVarInt()));
        int droppedEntries = Math.max(0, Math.min(4096, buf.readVarInt()));
        return new LabLootFieldValues(targetId, customId, pools, droppedPools, droppedEntries);
    }
}
