package com.abo47.kubejslab.loot.model;

import net.minecraft.network.FriendlyByteBuf;

public record LabLootFieldValues(String targetId, String customId, String poolRollsType, float poolRollsValue,
        float poolRollsMin, float poolRollsMax, int poolRollsN, float poolRollsP,
        String entryType, String entryItem, String entryTag, String entryLootTable, String entryCountType,
        float entryCountValue, float entryCountMin, float entryCountMax, int entryWeight, int entryQuality,
        boolean poolSurvivesExplosion, float poolRandomChance, boolean poolKilledByPlayer,
        boolean poolFurnaceSmelt, boolean poolLootingEnchant, float poolLootingCount, int poolLootingLimit) {

    public LabLootFieldValues {
        targetId = targetId == null ? "" : targetId;
        customId = customId == null ? "" : customId;
        poolRollsType = poolRollsType == null ? "" : poolRollsType;
        entryType = entryType == null ? "" : entryType;
        entryItem = entryItem == null ? "" : entryItem;
        entryTag = entryTag == null ? "" : entryTag;
        entryLootTable = entryLootTable == null ? "" : entryLootTable;
        entryCountType = entryCountType == null ? "" : entryCountType;
        poolRollsValue = Math.max(0f, poolRollsValue);
        poolRollsMin = Math.max(0f, poolRollsMin);
        poolRollsMax = Math.max(0f, poolRollsMax);
        poolRollsN = Math.max(0, poolRollsN);
        poolRollsP = Math.max(0f, Math.min(1f, poolRollsP));
        entryCountValue = Math.max(0f, entryCountValue);
        entryCountMin = Math.max(0f, entryCountMin);
        entryCountMax = Math.max(0f, entryCountMax);
        entryWeight = Math.max(1, entryWeight);
        entryQuality = Math.max(0, entryQuality);
        poolRandomChance = Math.max(0f, Math.min(1f, poolRandomChance));
        poolLootingCount = Math.max(0f, poolLootingCount);
        poolLootingLimit = Math.max(0, poolLootingLimit);
    }

    public static LabLootFieldValues defaults() {
        return new LabLootFieldValues("", "", "constant", 1f, 0f, 0f, 0, 0.5f, "item", "", "", "",
                "constant", 1f, 0f, 0f, 1, 0, true, 1f, false, false, false, 0f, 0);
    }

    public static void write(FriendlyByteBuf buf, LabLootFieldValues v) {
        buf.writeUtf(v.targetId(), 32767);
        buf.writeUtf(v.customId(), 32767);
        buf.writeUtf(v.poolRollsType(), 32767);
        buf.writeFloat(v.poolRollsValue());
        buf.writeFloat(v.poolRollsMin());
        buf.writeFloat(v.poolRollsMax());
        buf.writeVarInt(v.poolRollsN());
        buf.writeFloat(v.poolRollsP());
        buf.writeUtf(v.entryType(), 32767);
        buf.writeUtf(v.entryItem(), 32767);
        buf.writeUtf(v.entryTag(), 32767);
        buf.writeUtf(v.entryLootTable(), 32767);
        buf.writeUtf(v.entryCountType(), 32767);
        buf.writeFloat(v.entryCountValue());
        buf.writeFloat(v.entryCountMin());
        buf.writeFloat(v.entryCountMax());
        buf.writeVarInt(v.entryWeight());
        buf.writeVarInt(v.entryQuality());
        buf.writeBoolean(v.poolSurvivesExplosion());
        buf.writeFloat(v.poolRandomChance());
        buf.writeBoolean(v.poolKilledByPlayer());
        buf.writeBoolean(v.poolFurnaceSmelt());
        buf.writeBoolean(v.poolLootingEnchant());
        buf.writeFloat(v.poolLootingCount());
        buf.writeVarInt(v.poolLootingLimit());
    }

    public static LabLootFieldValues read(FriendlyByteBuf buf) {
        return new LabLootFieldValues(buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readFloat(), buf.readFloat(),
                buf.readFloat(), Math.max(0, Math.min(1000, buf.readVarInt())), buf.readFloat(),
                buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readFloat(),
                buf.readFloat(), buf.readFloat(), Math.max(1, Math.min(1000, buf.readVarInt())),
                Math.max(0, Math.min(1000, buf.readVarInt())), buf.readBoolean(), buf.readFloat(),
                buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readFloat(),
                Math.max(0, Math.min(1000, buf.readVarInt())));
    }
}