package com.abo47.kubejslab.loot.model;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.FriendlyByteBuf;

public record LabLootFieldValues(String targetId, String customId, List<LabLootPoolValues> pools) {

    public LabLootFieldValues {
        targetId = targetId == null ? "" : targetId;
        customId = customId == null ? "" : customId;
        pools = pools == null ? List.of() : List.copyOf(pools);
    }

    public static LabLootFieldValues defaults() {
        return new LabLootFieldValues("", "", List.of(LabLootPoolValues.defaults()));
    }

    public static void write(FriendlyByteBuf buf, LabLootFieldValues v) {
        buf.writeUtf(v.targetId(), 32767);
        buf.writeUtf(v.customId(), 32767);
        buf.writeVarInt(v.pools().size());
        for (LabLootPoolValues p : v.pools()) {
            LabLootPoolValues.write(buf, p);
        }
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
        return new LabLootFieldValues(targetId, customId, pools);
    }
}
