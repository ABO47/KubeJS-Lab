package com.abo47.kubejslab.loot.model;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.FriendlyByteBuf;

public record LootFieldValues(String targetId, String customId, List<LootPoolValues> pools,
        int droppedPools, int droppedEntries) {

    public LootFieldValues {
        targetId = targetId == null ? "" : targetId;
        customId = customId == null ? "" : customId;
        pools = pools == null ? List.of() : List.copyOf(pools);
        droppedPools = Math.max(0, droppedPools);
        droppedEntries = Math.max(0, droppedEntries);
    }

    public static LootFieldValues defaults() {
        return new LootFieldValues("", "", List.of(LootPoolValues.defaults()), 0, 0);
    }

    public static void write(FriendlyByteBuf buf, LootFieldValues v) {
        buf.writeUtf(v.targetId(), 32767);
        buf.writeUtf(v.customId(), 32767);
        buf.writeVarInt(v.pools().size());
        for (LootPoolValues p : v.pools()) {
            LootPoolValues.write(buf, p);
        }
        buf.writeVarInt(v.droppedPools());
        buf.writeVarInt(v.droppedEntries());
    }

    public static LootFieldValues read(FriendlyByteBuf buf) {
        String targetId = buf.readUtf();
        String customId = buf.readUtf();
        int poolCount = Math.min(buf.readVarInt(), 64);
        List<LootPoolValues> pools = new ArrayList<>(poolCount);
        for (int i = 0; i < poolCount; i++) {
            pools.add(LootPoolValues.read(buf));
        }
        if (pools.isEmpty()) {
            pools.add(LootPoolValues.defaults());
        }
        int droppedPools = Math.max(0, Math.min(1024, buf.readVarInt()));
        int droppedEntries = Math.max(0, Math.min(4096, buf.readVarInt()));
        return new LootFieldValues(targetId, customId, pools, droppedPools, droppedEntries);
    }
}
