package com.abo47.kubejslab.loot.model;

import net.minecraft.network.FriendlyByteBuf;

public record LabLootEntryValues(String type, String item, String tag, String lootTable, String countType,
        float countValue, float countMin, float countMax, int weight, int quality) {

    public LabLootEntryValues {
        type = type == null ? "" : type;
        item = item == null ? "" : item;
        tag = tag == null ? "" : tag;
        lootTable = lootTable == null ? "" : lootTable;
        countType = countType == null ? "" : countType;
        countValue = Math.max(0f, countValue);
        countMin = Math.max(0f, countMin);
        countMax = Math.max(0f, countMax);
        weight = Math.max(1, weight);
        quality = Math.max(0, quality);
    }

    public static LabLootEntryValues defaults() {
        return new LabLootEntryValues("item", "", "", "", "constant", 1f, 0f, 0f, 1, 0);
    }

    public static void write(FriendlyByteBuf buf, LabLootEntryValues v) {
        buf.writeUtf(v.type(), 32767);
        buf.writeUtf(v.item(), 32767);
        buf.writeUtf(v.tag(), 32767);
        buf.writeUtf(v.lootTable(), 32767);
        buf.writeUtf(v.countType(), 32767);
        buf.writeFloat(v.countValue());
        buf.writeFloat(v.countMin());
        buf.writeFloat(v.countMax());
        buf.writeVarInt(v.weight());
        buf.writeVarInt(v.quality());
    }

    public static LabLootEntryValues read(FriendlyByteBuf buf) {
        return new LabLootEntryValues(buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readUtf(),
                buf.readFloat(), buf.readFloat(), buf.readFloat(),
                Math.max(1, Math.min(1000, buf.readVarInt())),
                Math.max(0, Math.min(1000, buf.readVarInt())));
    }
}
